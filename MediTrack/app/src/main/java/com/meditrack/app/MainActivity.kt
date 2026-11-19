package com.meditrack.app

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.meditrack.app.ui.navigation.MediTrackNavHost
import com.meditrack.app.ui.screens.SplashScreen
import com.meditrack.app.ui.theme.MediTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MediTrackTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    PermissionPrompts()
                    MediTrackNavHost()
                }
            }
        }
    }
}

private fun needsExactAlarmPrompt(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    return !alarmManager.canScheduleExactAlarms()
}

/**
 * Asks for POST_NOTIFICATIONS (Android 13+) and, if needed, sends the user to the
 * exact-alarm settings screen so reminders fire on time.
 */
@Composable
private fun PermissionPrompts() {
    val context = LocalContext.current
    var showExactDialog by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { showExactDialog = needsExactAlarmPrompt(context) }

    LaunchedEffect(Unit) {
        val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        if (needsNotificationPermission) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showExactDialog = needsExactAlarmPrompt(context)
        }
    }

    if (showExactDialog) {
        AlertDialog(
            onDismissRequest = { showExactDialog = false },
            title = { Text("Allow exact reminders") },
            text = {
                Text("MediTrack needs permission to set exact alarms so your medicine reminders arrive on time.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showExactDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                }) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { showExactDialog = false }) { Text("Later") }
            }
        )
    }
}
