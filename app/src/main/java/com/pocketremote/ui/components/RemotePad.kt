package com.pocketremote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardReturn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsInputHdmi
import androidx.compose.material.icons.outlined.VolumeDown
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.hypot
import com.pocketremote.protocol.Constants
import com.pocketremote.ui.theme.rememberReducedMotion

/** 圆形方向盘 + 快捷键。 */
@Composable
fun RemotePad(
    onKey: (Int) -> Unit,
    onOpenNumbers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            contentAlignment = Alignment.Center,
        ) {
            val pad = minOf(maxWidth - 24.dp, maxHeight - 8.dp, 280.dp)
            ClickPad(size = pad, onKey = onKey)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RoundKey(Icons.Outlined.VolumeDown, "音量减", repeat = true) { onKey(Constants.KEY_VOL_DOWN) }
            RoundKey(Icons.Outlined.VolumeOff, "静音") { onKey(Constants.KEY_MUTE) }
            RoundKey(Icons.Outlined.VolumeUp, "音量加", repeat = true) { onKey(Constants.KEY_VOL_UP) }
        }
        Spacer(Modifier.height(8.dp))
        RemoteWells(onKey = onKey, onOpenNumbers = onOpenNumbers)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun RemoteWells(onKey: (Int) -> Unit, onOpenNumbers: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RoundKey(Icons.Outlined.KeyboardReturn, "返回") { onKey(Constants.KEY_BACK) }
            RoundKey(Icons.Outlined.Home, "主页") { onKey(Constants.KEY_HOME) }
            RoundKey(Icons.Outlined.Menu, "菜单") { onKey(Constants.KEY_MENU) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RoundKey(Icons.Outlined.Settings, "设置") { onKey(Constants.KEY_SETTINGS) }
            RoundKey(Icons.Outlined.SettingsInputHdmi, "信号源") { onKey(Constants.KEY_TV_INPUT) }
            RoundKey(Icons.Outlined.Dialpad, "数字") { onOpenNumbers() }
        }
    }
}

@Composable
fun MouseRemoteChrome(
    onKey: (Int) -> Unit,
    trackpad: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            contentAlignment = Alignment.Center,
        ) {
            val pad = minOf(maxWidth - 24.dp, maxHeight - 8.dp, 280.dp)
            Surface(
                modifier = Modifier.size(pad),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 1.dp,
            ) {
                trackpad(Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RoundKey(Icons.Outlined.KeyboardReturn, "返回") { onKey(Constants.KEY_BACK) }
            RoundKey(Icons.Outlined.Home, "主页") { onKey(Constants.KEY_HOME) }
            RoundKey(Icons.Outlined.VolumeOff, "静音") { onKey(Constants.KEY_MUTE) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private enum class PadRegion { None, Up, Down, Left, Right, Ok }

@Composable
private fun ClickPad(size: Dp, onKey: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val view = LocalView.current
    val padPx = with(LocalDensity.current) { size.toPx() }
    var held by remember { mutableStateOf(PadRegion.None) }
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = scheme.surfaceContainerHighest,
        tonalElevation = 2.dp,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "方向键"
                    customActions = listOf(
                        CustomAccessibilityAction("上") { onKey(Constants.KEY_UP); true },
                        CustomAccessibilityAction("下") { onKey(Constants.KEY_DOWN); true },
                        CustomAccessibilityAction("左") { onKey(Constants.KEY_LEFT); true },
                        CustomAccessibilityAction("右") { onKey(Constants.KEY_RIGHT); true },
                        CustomAccessibilityAction("确定") { onKey(Constants.KEY_OK); true },
                    )
                }
                .pointerInput(onKey, padPx) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val region = hitPadRegion(
                            down.position.x,
                            down.position.y,
                            padPx,
                        )
                        if (region == PadRegion.None) return@awaitEachGesture
                        held = region
                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.all { !it.pressed }) break
                            }
                        } finally {
                            held = PadRegion.None
                        }
                    }
                },
        ) {
            PressRepeat(
                pressed = held != PadRegion.None,
                repeat = held != PadRegion.None && held != PadRegion.Ok,
                onFire = { first ->
                    val region = held
                    if (region != PadRegion.None) {
                        view.performHapticFeedback(
                            if (first) HapticFeedbackConstants.KEYBOARD_TAP else HapticFeedbackConstants.CLOCK_TICK,
                        )
                        firePad(region, onKey)
                    }
                },
            )
            Canvas(Modifier.fillMaxSize()) {
                val canvasW = this.size.width
                val canvasH = this.size.height
                val cx = canvasW / 2f
                val cy = canvasH / 2f
                val glow = scheme.primary.copy(alpha = 0.32f)
                fun pie(start: Float) {
                    val path = Path().apply {
                        moveTo(cx, cy)
                        arcTo(Rect(0f, 0f, canvasW, canvasH), start, 90f, false)
                        close()
                    }
                    drawPath(path, glow, style = Fill)
                }
                when (held) {
                    PadRegion.Right -> pie(-45f)
                    PadRegion.Down -> pie(45f)
                    PadRegion.Left -> pie(135f)
                    PadRegion.Up -> pie(225f)
                    else -> Unit
                }
                val okR = this.size.minDimension * 0.22f
                drawCircle(
                    color = if (held == PadRegion.Ok) scheme.primary else scheme.primary.copy(alpha = 0.92f),
                    radius = okR,
                    center = Offset(cx, cy),
                )
            }
            Icon(
                Icons.Outlined.KeyboardArrowUp,
                null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 22.dp).size(28.dp),
            )
            Icon(
                Icons.Outlined.KeyboardArrowDown,
                null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp).size(28.dp),
            )
            Icon(
                Icons.Outlined.KeyboardArrowLeft,
                null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 22.dp).size(28.dp),
            )
            Icon(
                Icons.Outlined.KeyboardArrowRight,
                null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 22.dp).size(28.dp),
            )
            Text(
                "OK",
                color = scheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

private fun firePad(region: PadRegion, onKey: (Int) -> Unit) {
    when (region) {
        PadRegion.Up -> onKey(Constants.KEY_UP)
        PadRegion.Down -> onKey(Constants.KEY_DOWN)
        PadRegion.Left -> onKey(Constants.KEY_LEFT)
        PadRegion.Right -> onKey(Constants.KEY_RIGHT)
        PadRegion.Ok -> onKey(Constants.KEY_OK)
        PadRegion.None -> Unit
    }
}

private fun hitPadRegion(x: Float, y: Float, size: Float): PadRegion {
    val cx = size / 2f
    val cy = size / 2f
    val dx = x - cx
    val dy = y - cy
    val r = hypot(dx.toDouble(), dy.toDouble()).toFloat()
    if (r > size / 2f) return PadRegion.None
    if (r < size * 0.22f) return PadRegion.Ok
    val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
    return when {
        deg >= -45 && deg < 45 -> PadRegion.Right
        deg >= 45 && deg < 135 -> PadRegion.Down
        deg >= -135 && deg < -45 -> PadRegion.Up
        else -> PadRegion.Left
    }
}

@Composable
private fun PadPress(
    modifier: Modifier,
    onClick: () -> Unit,
    contentDescription: String,
    repeat: Boolean = false,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduce = rememberReducedMotion()
    val scale by animateFloatAsState(
        targetValue = if (reduce || !pressed) 1f else 0.9f,
        animationSpec = if (reduce) snap() else spring(),
        label = "pad",
    )
    val view = LocalView.current
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        PressRepeat(pressed = pressed, repeat = repeat, onFire = { first ->
            view.performHapticFeedback(
                if (first) HapticFeedbackConstants.KEYBOARD_TAP else HapticFeedbackConstants.CLOCK_TICK,
            )
            onClick()
        })
        content()
    }
}

@Composable
fun RoundKey(
    icon: ImageVector,
    label: String,
    repeat: Boolean = false,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    FilledTonalIconButton(
        onClick = {},
        modifier = Modifier.size(48.dp),
        interactionSource = interaction,
    ) {
        PressRepeat(pressed = pressed, repeat = repeat, onFire = { first ->
            view.performHapticFeedback(
                if (first) HapticFeedbackConstants.KEYBOARD_TAP else HapticFeedbackConstants.CLOCK_TICK,
            )
            onClick()
        })
        Icon(icon, contentDescription = label)
    }
}

/** 按下立刻发键；repeat 时按住约 400ms 后每 80ms 再发，抬手停止。 */
@Composable
private fun PressRepeat(
    pressed: Boolean,
    repeat: Boolean,
    onFire: (first: Boolean) -> Unit,
) {
    LaunchedEffect(pressed, repeat) {
        if (!pressed) return@LaunchedEffect
        onFire(true)
        if (!repeat) return@LaunchedEffect
        delay(400)
        while (true) {
            onFire(false)
            delay(80)
        }
    }
}

@Composable
fun NumberPad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { n ->
                    FilledTonalIconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onDigit(n)
                        },
                        modifier = Modifier.size(64.dp),
                    ) {
                        Text("$n", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FilledTonalIconButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onDelete()
                },
                modifier = Modifier.size(64.dp),
            ) {
                Icon(Icons.Outlined.Backspace, contentDescription = "删除")
            }
            FilledTonalIconButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onDigit(0)
                },
                modifier = Modifier.size(64.dp),
            ) {
                Text("0", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.size(64.dp))
        }
    }
}
