package app.rafiqaldhikr.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

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
    val transition = rememberInfiniteTransition(label = "float")
    val offsetY by transition.animateFloat(
        initialValue = -amplitude,
        targetValue = amplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
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
    val transition = rememberInfiniteTransition(label = "wobble")
    val rotation by transition.animateFloat(
        initialValue = -degrees,
        targetValue = degrees,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobbleRot"
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
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
