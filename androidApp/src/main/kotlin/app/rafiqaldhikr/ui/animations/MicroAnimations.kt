package app.rafiqaldhikr.ui.animations

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import app.rafiqaldhikr.ui.theme.stillableFloat

/**
 * A modifier that adds a micro-animation press effect (scale bounce)
 * on every touch. Apply to any clickable composable for premium feel.
 */
fun Modifier.pressAnimation(
    onClick: () -> Unit = {},
    scaleFactor: Float = 0.96f
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleFactor else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressAnim"
    )

    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                isPressed = true
                tryAwaitRelease()
                isPressed = false
            },
            onTap = { onClick() }
        )
    }
}

/**
 * Adds a subtle float/hover animation — the element gently rises and falls.
 * Good for decorative elements or status indicators.
 */
fun Modifier.floatAnimation(
    amplitude: Float = 4f,
    durationMs: Int = 3000
): Modifier = composed {
    val offsetY by stillableFloat(
        initialValue = -amplitude,
        targetValue  = amplitude,
        durationMs   = durationMs,
        easing       = FastOutSlowInEasing,
        repeatMode   = RepeatMode.Reverse,
        label        = "float",
    )
    graphicsLayer { translationY = offsetY }
}

/**
 * Adds a subtle rotation wobble — good for medallions and decorative icons.
 */
fun Modifier.wobbleAnimation(
    degrees: Float = 2f,
    durationMs: Int = 2500
): Modifier = composed {
    val rotation by stillableFloat(
        initialValue = -degrees,
        targetValue  = degrees,
        durationMs   = durationMs,
        easing       = FastOutSlowInEasing,
        repeatMode   = RepeatMode.Reverse,
        label        = "wobble",
    )
    graphicsLayer { rotationZ = rotation }
}

/**
 * Pulse scale animation — for drawing attention (e.g., notification badge).
 */
fun Modifier.pulseAnimation(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMs: Int = 1500
): Modifier = composed {
    val scale by stillableFloat(
        initialValue = minScale,
        targetValue  = maxScale,
        durationMs   = durationMs,
        easing       = FastOutSlowInEasing,
        repeatMode   = RepeatMode.Reverse,
        label        = "pulse",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
