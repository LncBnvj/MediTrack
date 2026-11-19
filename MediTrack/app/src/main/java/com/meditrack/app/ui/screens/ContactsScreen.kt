package com.meditrack.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditrack.app.data.EmergencyContact
import com.meditrack.app.sms.SmsHelper
import com.meditrack.app.ui.components.AppButton
import com.meditrack.app.ui.components.AppCard
import com.meditrack.app.ui.components.Pill
import com.meditrack.app.ui.components.ScreenTitle
import com.meditrack.app.ui.theme.Danger
import com.meditrack.app.ui.theme.Ink
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.theme.PrimaryLight
import com.meditrack.app.ui.theme.Warning
import com.meditrack.app.ui.theme.WarningLight
import com.meditrack.app.ui.viewmodel.ContactsViewModel

@Composable
fun ContactsScreen(vm: ContactsViewModel = viewModel()) {
    val context = LocalContext.current
    val contacts by vm.contacts.collectAsStateWithLifecycle()

    // null = closed, EmergencyContact() with id 0 = new, otherwise editing
    var dialogContact by remember { mutableStateOf<EmergencyContact?>(null) }
    var confirmEmergency by remember { mutableStateOf(false) }
    var pendingEmergencySend by remember { mutableStateOf(false) }

    fun sendEmergency() {
        vm.sendEmergency { count ->
            Toast.makeText(
                context,
                if (count > 0) "Emergency SMS sent to $count contact(s)" else "No SMS sent. Check contacts and signal.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (pendingEmergencySend) {
            pendingEmergencySend = false
            if (granted) sendEmergency()
        } else {
            vm.updateSmsOnMissed(granted)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenTitle("Emergency Contacts") }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarningLight)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.Info, null, tint = Warning, modifier = Modifier.size(18.dp))
                Text(
                    "Trusted contacts can receive an SMS if you miss important doses. Requires mobile signal and SMS permission.",
                    modifier = Modifier.padding(start = 10.dp),
                    fontSize = 12.sp, color = Warning
                )
            }
        }
        items(contacts, key = { it.id }) { contact ->
            ContactCard(
                contact,
                onClick = { dialogContact = contact },
                onCall = {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}")))
                }
            )
        }
        item {
            AppButton(
                "+ Add contact",
                onClick = { dialogContact = EmergencyContact(name = "", phone = "") },
                modifier = Modifier.fillMaxWidth(),
                filled = false
            )
        }
        item {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "My name (shown in emergency SMS)",
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink
                    )
                    OutlinedTextField(
                        value = vm.userName,
                        onValueChange = { vm.updateUserName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("My name") },
                        singleLine = true
                    )
                }
            }
        }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Send SMS after 2 missed doses",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp, color = Ink
                    )
                    Switch(
                        checked = vm.smsOnMissed,
                        onCheckedChange = { on ->
                            when {
                                !on -> vm.updateSmsOnMissed(false)
                                SmsHelper.hasPermission(context) -> vm.updateSmsOnMissed(true)
                                else -> smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                    )
                }
            }
        }
        item {
            AppButton(
                "Send emergency SMS now",
                onClick = { confirmEmergency = true },
                enabled = contacts.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                filled = false,
                contentColor = Danger
            )
        }
    }

    dialogContact?.let { initial ->
        ContactDialog(
            initial = initial,
            onSave = { vm.save(it); dialogContact = null },
            onDelete = { vm.delete(it); dialogContact = null },
            onDismiss = { dialogContact = null }
        )
    }

    if (confirmEmergency) {
        AlertDialog(
            onDismissRequest = { confirmEmergency = false },
            title = { Text("Send emergency SMS?") },
            text = { Text("An SMS asking for help will be sent to all of your saved contacts. Standard SMS charges may apply.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmEmergency = false
                    if (SmsHelper.hasPermission(context)) {
                        sendEmergency()
                    } else {
                        pendingEmergencySend = true
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    }
                }) { Text("Send", color = Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmEmergency = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ContactCard(contact: EmergencyContact, onClick: () -> Unit, onCall: () -> Unit) {
    val initials = contact.name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(contact.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    if (contact.isPrimary) {
                        Pill("Primary", PrimaryLight, Primary, Modifier.padding(start = 8.dp))
                    }
                }
                Text(
                    listOf(contact.relationship, contact.phone).filter { it.isNotBlank() }.joinToString(" · "),
                    fontSize = 12.sp, color = Muted
                )
            }
            IconButton(onClick = onCall) {
                Icon(Icons.Filled.Phone, contentDescription = "Call ${contact.name}", tint = Primary)
            }
        }
    }
}

@Composable
private fun ContactDialog(
    initial: EmergencyContact,
    onSave: (EmergencyContact) -> Unit,
    onDelete: (EmergencyContact) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var phone by remember { mutableStateOf(initial.phone) }
    var relationship by remember { mutableStateOf(initial.relationship) }
    var primary by remember { mutableStateOf(initial.isPrimary) }
    val isEditing = initial.id != 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit contact" else "Add contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    phone, { phone = it },
                    label = { Text("Phone number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    relationship, { relationship = it },
                    label = { Text("Relationship (optional)") }, singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = primary,
                        onCheckedChange = { primary = it },
                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                    )
                    Text("Primary contact", fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && phone.isNotBlank(),
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            relationship = relationship.trim(),
                            isPrimary = primary
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (isEditing) {
                    TextButton(onClick = { onDelete(initial) }) { Text("Delete", color = Danger) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}