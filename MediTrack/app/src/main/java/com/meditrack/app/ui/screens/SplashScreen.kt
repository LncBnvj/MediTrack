package com.meditrack.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.ui.components.AnimatedBear
import com.meditrack.app.ui.theme.Background
import com.meditrack.app.ui.theme.Ink
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.theme.PrimaryLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Loading screen: Bruno bounces in, the name fades in, three dots pulse, then [onFinished]. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val bearScale = remember { Animatable(0.5f) }
    val fade = remember { Animatable(0f) }
    val glowTransition = rememberInfiniteTransition(label = "glow")
    val glow by glowTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        launch { fade.animateTo(1f, tween(600)) }
        bearScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        delay(1800) // long enough to see the ears wiggle and a blink
        onFinished()
    }

    Box(
        Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(190.dp).scale(glow).clip(CircleShape).background(PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                AnimatedBear(Modifier.size(140.dp).scale(bearScale.value))
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "MediTrack",
                color = Ink, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(fade.value)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your friendly medicine reminder",
                color = Muted, fontSize = 15.sp,
                modifier = Modifier.alpha(fade.value)
            )
            Spacer(Modifier.height(36.dp))
            LoadingDots()
        }
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) { i ->
            val a by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * 160)
                ),
                label = "dot$i"
            )
            Box(Modifier.size(10.dp).alpha(a).clip(CircleShape).background(Primary))
        }
    }
}
