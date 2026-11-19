package com.meditrack.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate

private val BearBrown = Color(0xFFA9743F)
private val BearLight = Color(0xFFE8CBA0)
private val BearDark = Color(0xFF2B1B10)

/**
 * Bruno, drawn on a Canvas so the parts can move: he bobs gently, wiggles his ears
 * and blinks. Same artwork as res/drawable/bear_mascot.xml (64 x 64 grid).
 */
@Composable
fun AnimatedBear(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bear")

    // Gentle up and down bob
    val bob by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    // Ear wiggle (degrees), once every 4 seconds
    val earAngle by transition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4000
                0f at 0
                0f at 1200
                -14f at 1300
                10f at 1500
                -8f at 1700
                0f at 1900
                0f at 4000
            }
        ),
        label = "ears"
    )
    // Blink: eye height 1 = open, 0.1 = closed. Two blinks every 4 seconds.
    val eyeOpen by transition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4000
                1f at 0
                1f at 2200
                0.1f at 2280
                1f at 2400
                1f at 3200
                0.1f at 3280
                1f at 3400
                1f at 4000
            }
        ),
        label = "blink"
    )

    Canvas(modifier.aspectRatio(1f)) {
        val u = size.width / 64f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)

        translate(top = -bob * 2f * u) {
            // Ears (rotate around where they meet the head)
            rotate(earAngle, pivot = p(15f, 25f)) {
                drawCircle(BearBrown, 8.5f * u, p(15f, 17f))
                drawCircle(BearLight, 4f * u, p(15f, 17f))
            }
            rotate(-earAngle, pivot = p(49f, 25f)) {
                drawCircle(BearBrown, 8.5f * u, p(49f, 17f))
                drawCircle(BearLight, 4f * u, p(49f, 17f))
            }
            // Head, muzzle, nose
            drawCircle(BearBrown, 22f * u, p(32f, 36f))
            drawOval(BearLight, p(22f, 35f), Size(20f * u, 16f * u))
            drawOval(BearDark, p(28.6f, 37.1f), Size(6.8f * u, 4.8f * u))
            // Eyes (squash vertically to blink)
            scale(1f, eyeOpen, pivot = p(23f, 31f)) { drawCircle(BearDark, 2.6f * u, p(23f, 31f)) }
            scale(1f, eyeOpen, pivot = p(41f, 31f)) { drawCircle(BearDark, 2.6f * u, p(41f, 31f)) }
            // Mouth
            val mouth = Path().apply {
                moveTo(32f * u, 42f * u); lineTo(32f * u, 45f * u)
                moveTo(32f * u, 45f * u); quadraticBezierTo(29f * u, 48f * u, 26f * u, 46f * u)
                moveTo(32f * u, 45f * u); quadraticBezierTo(35f * u, 48f * u, 38f * u, 46f * u)
            }
            drawPath(mouth, BearDark, style = Stroke(width = 2f * u, cap = StrokeCap.Round))
        }
    }
}
