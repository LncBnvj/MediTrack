package com.meditrack.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.ui.theme.Ink
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Outline
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.theme.PrimaryLight

/** White rounded card; [highlighted] draws the teal border used for the "next dose". */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val clickable = if (onClick != null) Modifier.clip(shape).clickable(onClick = onClick) else Modifier
    Card(
        modifier = modifier.fillMaxWidth().then(clickable),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, if (highlighted) Primary else Outline)
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

/** Small rounded status label ("Taken", "Missed", "refill soon"...). */
@Composable
fun Pill(text: String, background: Color, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Generic chip used for frequency options, reminder times and "+ Add time". */
@Composable
fun AppChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    val shape = RoundedCornerShape(50)
    val bg = if (selected) PrimaryLight else Color.White
    val fg = if (selected) Primary else Muted
    Row(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, if (selected) PrimaryLight else Outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (leadingIcon != null) Icon(leadingIcon, null, tint = fg, modifier = Modifier.size(14.dp))
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        if (trailingIcon != null) Icon(trailingIcon, null, tint = fg, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun IconBubble(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = PrimaryLight,
    tint: Color = Primary,
    size: Dp = 36.dp
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    enabled: Boolean = true,
    contentColor: Color = Primary
) {
    val shape = RoundedCornerShape(12.dp)
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(46.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
        ) { Text(text, fontWeight = FontWeight.SemiBold) }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(46.dp),
            shape = shape,
            border = BorderStroke(1.dp, Outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
        ) { Text(text, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = Muted)
        }
    }
}
