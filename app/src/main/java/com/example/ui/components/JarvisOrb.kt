package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrbState
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisOrb(
    orbState: OrbState,
    micLevel: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    onClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_transition")

    // Outer gear rotation
    val rotOuter = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_outer"
    )

    // Inner counter rotation
    val rotInner = infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_inner"
    )

    // Core pulsing scale
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.THINKING -> 500
                    OrbState.LISTENING -> 750
                    OrbState.SPEAKING -> 900
                    OrbState.ERROR -> 400
                    OrbState.IDLE -> 2200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Ripple wave for speaking / listening
    val rippleRadius = infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    val orbColor = when (orbState) {
        OrbState.IDLE -> primaryColor
        OrbState.LISTENING -> Color(0xFF00F0FF) // Electric cyan
        OrbState.THINKING -> Color(0xFFFFD700) // Arc Reactor Gold
        OrbState.SPEAKING -> Color(0xFF00E676) // Radiant Emerald
        OrbState.ERROR -> Color(0xFFFF1744) // Stark emergency crimson
    }

    val stateText = when (orbState) {
        OrbState.IDLE -> "JARVIS // ONLINE"
        OrbState.LISTENING -> "TRANSDUCER ACTIVE // LISTENING"
        OrbState.THINKING -> "NEURAL PROCESSING..."
        OrbState.SPEAKING -> "AUDIO OUTPUT ENGAGED"
        OrbState.ERROR -> "SYSTEM PROTOCOL ALERT"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .testTag("jarvis_ai_orb")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = this.size.minDimension / 2f * 0.74f

                // 1. Acoustic Ripple waves during LISTENING / SPEAKING
                if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING) {
                    val ripRad = baseRadius * rippleRadius.value
                    val alpha = (1f - (rippleRadius.value - 0.45f) / 0.8f).coerceIn(0f, 0.6f)
                    drawCircle(
                        color = orbColor.copy(alpha = alpha),
                        radius = ripRad,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // 2. Outermost Bezel Ring with Tech Ticks
                val tickCount = 36
                for (i in 0 until tickCount) {
                    val angle = Math.toRadians((i * (360.0 / tickCount) + rotOuter.value * 0.3).toDouble())
                    val isMajor = i % 9 == 0
                    val tickLen = if (isMajor) 10.dp.toPx() else 4.dp.toPx()
                    val rOuter = baseRadius * 1.22f
                    val rInner = rOuter - tickLen

                    val startX = center.x + (rInner * cos(angle)).toFloat()
                    val startY = center.y + (rInner * sin(angle)).toFloat()
                    val endX = center.x + (rOuter * cos(angle)).toFloat()
                    val endY = center.y + (rOuter * sin(angle)).toFloat()

                    drawLine(
                        color = if (isMajor) orbColor else orbColor.copy(alpha = 0.35f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 2.5f else 1.2f
                    )
                }

                // 3. Segmented Titanium Cog Teeth Ring
                val cogCount = 12
                for (i in 0 until cogCount) {
                    val angle = Math.toRadians((i * (360.0 / cogCount) + rotOuter.value).toDouble())
                    val cogRadius = baseRadius * 1.08f
                    val cogX = center.x + (cogRadius * cos(angle)).toFloat()
                    val cogY = center.y + (cogRadius * sin(angle)).toFloat()

                    drawCircle(
                        color = Color(0xFF37474F).copy(alpha = 0.85f),
                        radius = 4.5.dp.toPx(),
                        center = Offset(cogX, cogY)
                    )
                    drawCircle(
                        color = orbColor,
                        radius = 2.dp.toPx(),
                        center = Offset(cogX, cogY)
                    )
                }

                // 4. Rotating Notched Energy Band (Clockwise)
                drawRotatingNotchedRing(
                    center = center,
                    radius = baseRadius * 1.02f,
                    color = orbColor.copy(alpha = 0.65f),
                    rotation = rotOuter.value,
                    dashLength = 36f,
                    gapLength = 22f,
                    strokeWidth = 3f
                )

                // 5. Counter-Rotating Inner Guidance Ring (Counter-Clockwise)
                drawRotatingNotchedRing(
                    center = center,
                    radius = baseRadius * 0.82f,
                    color = secondaryColor.copy(alpha = 0.75f),
                    rotation = rotInner.value,
                    dashLength = 20f,
                    gapLength = 16f,
                    strokeWidth = 2.5f
                )

                // 6. Reactive Central Arc Reactor Chamber
                val micExpand = if (orbState == OrbState.LISTENING) micLevel * 28f else 0f
                val activeRadius = (baseRadius * 0.65f * pulseScale.value) + micExpand

                // Ambient Radial Photonic Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orbColor.copy(alpha = 0.6f),
                            orbColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = activeRadius * 1.6f
                    ),
                    radius = activeRadius * 1.6f,
                    center = center
                )

                // Core Spherical Energy Chamber
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            orbColor,
                            orbColor.copy(alpha = 0.85f),
                            Color(0xFF100204)
                        ),
                        center = center,
                        radius = activeRadius
                    ),
                    radius = activeRadius,
                    center = center
                )

                // 7. Iconic Stark Industries Triangular Core (Inside the Arc Reactor!)
                val triRadius = activeRadius * 0.72f
                val triAngleOffset = Math.toRadians((rotInner.value * 0.5 - 90).toDouble())

                val path = Path().apply {
                    val p1X = center.x + (triRadius * cos(triAngleOffset)).toFloat()
                    val p1Y = center.y + (triRadius * sin(triAngleOffset)).toFloat()

                    val p2X = center.x + (triRadius * cos(triAngleOffset + (2 * Math.PI / 3))).toFloat()
                    val p2Y = center.y + (triRadius * sin(triAngleOffset + (2 * Math.PI / 3))).toFloat()

                    val p3X = center.x + (triRadius * cos(triAngleOffset + (4 * Math.PI / 3))).toFloat()
                    val p3Y = center.y + (triRadius * sin(triAngleOffset + (4 * Math.PI / 3))).toFloat()

                    moveTo(p1X, p1Y)
                    lineTo(p2X, p2Y)
                    lineTo(p3X, p3Y)
                    close()
                }

                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.95f),
                    style = Stroke(width = 3.5.dp.toPx())
                )

                // Inner inverted triangular core glow
                val innerTriRadius = triRadius * 0.45f
                val innerPath = Path().apply {
                    val p1X = center.x + (innerTriRadius * cos(triAngleOffset + Math.PI)).toFloat()
                    val p1Y = center.y + (innerTriRadius * sin(triAngleOffset + Math.PI)).toFloat()

                    val p2X = center.x + (innerTriRadius * cos(triAngleOffset + Math.PI + (2 * Math.PI / 3))).toFloat()
                    val p2Y = center.y + (innerTriRadius * sin(triAngleOffset + Math.PI + (2 * Math.PI / 3))).toFloat()

                    val p3X = center.x + (innerTriRadius * cos(triAngleOffset + Math.PI + (4 * Math.PI / 3))).toFloat()
                    val p3Y = center.y + (innerTriRadius * sin(triAngleOffset + Math.PI + (4 * Math.PI / 3))).toFloat()

                    moveTo(p1X, p1Y)
                    lineTo(p2X, p2Y)
                    lineTo(p3X, p3Y)
                    close()
                }

                drawPath(
                    path = innerPath,
                    color = orbColor.copy(alpha = 0.9f)
                )

                // Center Photon Point
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State indicator pill text with Orbitron font
        Text(
            text = stateText,
            color = orbColor,
            fontFamily = OrbitronFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

private fun DrawScope.drawRotatingNotchedRing(
    center: Offset,
    radius: Float,
    color: Color,
    rotation: Float,
    dashLength: Float,
    gapLength: Float,
    strokeWidth: Float
) {
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), rotation)
        )
    )
}
