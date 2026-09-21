package com.pocketremote.ui.remote
import com.pocketremote.ui.theme.vibrate

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pocketremote.ui.theme.rememberReducedMotion
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** 相对位移鼠标板：光标跟随手指。 */
@Composable
fun MouseTrackpad(
    onMove: (Int, Int) -> Unit,
    onClick: () -> Unit,
    onUp: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tapSlop = with(LocalDensity.current) { 14.dp.toPx() }
    val scale = 4.2f
    val view = LocalView.current
    val reduce = rememberReducedMotion()
    val scope = rememberCoroutineScope()
    var fingerDown by remember { mutableStateOf(false) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val orb = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val orbAlpha by animateFloatAsState(
        targetValue = if (fingerDown) 1f else 0.35f,
        animationSpec = if (reduce) spring(stiffness = 10_000f) else spring(),
        label = "orbAlpha",
    )
    LaunchedEffect(boxSize) {
        if (!fingerDown && boxSize.width > 0) {
            orb.snapTo(Offset(boxSize.width / 2f, boxSize.height / 2f))
        }
    }
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
            .semantics { contentDescription = "触控板。滑动移动光标，点按单击" }
            .background(
                if (fingerDown) scheme.surfaceContainerHighest else Color.Transparent,
            )
            .pointerInput(tapSlop, scale) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fingerDown = true
                    scope.launch {
                        if (reduce) orb.snapTo(down.position) else orb.animateTo(down.position, spring())
                    }
                    var total = Offset.Zero
                    var moved = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                if (!moved) {
                                    view.vibrate(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onClick()
                                }
                                break
                            }
                            val d = change.positionChange()
                            if (d != Offset.Zero) {
                                change.consume()
                                total += d
                                scope.launch { orb.snapTo(change.position) }
                                if (total.getDistance() > tapSlop) {
                                    moved = true
                                    val dx = (d.x * scale).toInt()
                                    val dy = (d.y * scale).toInt()
                                    if (dx != 0 || dy != 0) onMove(dx, dy)
                                }
                            }
                        }
                    } finally {
                        fingerDown = false
                        onUp()
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        scope.launch {
                            if (reduce) orb.snapTo(Offset(cx, cy))
                            else orb.animateTo(Offset(cx, cy), spring(dampingRatio = 0.8f))
                        }
                    }
                }
            },
    ) {
        if (!fingerDown) {
            Text(
                "滑动移动光标，点按单击",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        val pos = orb.value
        val orbPx = with(LocalDensity.current) { 56.dp.toPx() }
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    IntOffset(
                        (pos.x - orbPx / 2f).roundToInt(),
                        (pos.y - orbPx / 2f).roundToInt(),
                    )
                }
                .size(56.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = scheme.primary.copy(alpha = 0.22f * orbAlpha),
            tonalElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(scheme.primary.copy(alpha = orbAlpha)),
                )
            }
        }
    }
}
