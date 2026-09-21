package com.pocketremote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.KeyboardReturn
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.Constants
import kotlin.math.atan2
import kotlin.math.hypot

// ─── RemotePad ────────────────────────────────────────────────────────────────

@Composable
fun RemotePad(
    onKey: (Int) -> Unit,
    onOpenNumbers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Row: Back, Home, Menu
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NeoIconButton(onClick = { onKey(Constants.KEY_BACK) }) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardReturn, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            NeoIconButton(onClick = { onKey(Constants.KEY_HOME) }) {
                Icon(Icons.Outlined.Home, contentDescription = "主页", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            NeoIconButton(onClick = { onKey(Constants.KEY_MENU) }) {
                Icon(Icons.Outlined.Menu, contentDescription = "菜单", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Middle: Large D-Pad
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            val pad = minOf(maxWidth - 16.dp, maxHeight - 32.dp, 320.dp)
            val outerSize = pad + 32.dp
            Box(
                modifier = Modifier
                    .size(outerSize)
                    .shadow(2.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Inner shadow ring — simulates concave crater feel
                val innerShadowColor = Color.Black.copy(alpha = 0.22f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.minDimension / 2f
                    // Radial gradient from edge inward — dark at rim, transparent toward center
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, innerShadowColor),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = r,
                        ),
                        radius = r,
                        style = Fill
                    )
                    // Additional arc highlight at bottom-right (light source simulation)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
                            center = Offset(size.width * 0.35f, size.height * 0.3f),
                            radius = r * 0.7f,
                        ),
                        radius = r,
                        style = Fill
                    )
                }
                ClickPad(size = pad, onKey = onKey)
            }
        }


        // Bottom Row: Volume & Numbers/Mute
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalVolumeRocker(onKey = onKey)
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                NeoIconButton(onClick = { onKey(Constants.KEY_MUTE) }) {
                    Icon(Icons.AutoMirrored.Outlined.VolumeOff, contentDescription = "静音", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                NeoIconButton(onClick = { onOpenNumbers() }) {
                    Icon(Icons.Outlined.Dialpad, contentDescription = "数字", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ─── HorizontalVolumeRocker ───────────────────────────────────────────────────

@Composable
fun HorizontalVolumeRocker(onKey: (Int) -> Unit) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .width(140.dp)
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(28.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onKey(Constants.KEY_VOL_DOWN)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.VolumeDown,
                contentDescription = "音量-",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp))
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onKey(Constants.KEY_VOL_UP)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.VolumeUp,
                contentDescription = "音量+",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─── ClickPad (D-Pad) ─────────────────────────────────────────────────────────

private enum class PadRegion { None, Up, Down, Left, Right, Ok }

@Composable
private fun ClickPad(size: Dp, onKey: (Int) -> Unit) {
    val view = LocalView.current
    val padPx = with(LocalDensity.current) { size.toPx() }
    var held by remember { mutableStateOf(PadRegion.None) }
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(size)
            .shadow(6.dp, CircleShape)
            .background(scheme.surfaceContainer, CircleShape)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startRegion = hitPadRegion(down.position.x, down.position.y, padPx)
                    if (startRegion != PadRegion.None) {
                        held = startRegion
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        firePad(startRegion, onKey)
                    }
                    do {
                        val event = awaitPointerEvent()
                        val pos = event.changes.first().position
                        val currRegion = hitPadRegion(pos.x, pos.y, padPx)
                        if (currRegion != held && startRegion != PadRegion.Ok) {
                            held = currRegion
                            if (currRegion != PadRegion.None) {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                firePad(currRegion, onKey)
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    held = PadRegion.None
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Glowing arc overlay on pressed sector
        val glowColor = scheme.primary.copy(alpha = 0.2f)
        Canvas(Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val cx = canvasW / 2f
            val cy = canvasH / 2f
            fun pie(start: Float) {
                val path = Path().apply {
                    moveTo(cx, cy)
                    arcTo(Rect(0f, 0f, canvasW, canvasH), start, 90f, false)
                    close()
                }
                drawPath(path, glowColor, style = Fill)
            }
            when (held) {
                PadRegion.Right -> pie(-45f)
                PadRegion.Down  -> pie(45f)
                PadRegion.Left  -> pie(135f)
                PadRegion.Up    -> pie(225f)
                else            -> Unit
            }
        }

        // Directional Arrows
        val arrowTint = scheme.onSurfaceVariant
        Icon(Icons.Outlined.KeyboardArrowUp, null, tint = arrowTint, modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp).size(28.dp))
        Icon(Icons.Outlined.KeyboardArrowDown, null, tint = arrowTint, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).size(28.dp))
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = arrowTint, modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp).size(28.dp))
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = arrowTint, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).size(28.dp))

        // Center OK button — with animated LED glow on press
        val okPressed = held == PadRegion.Ok
        val okBg = if (okPressed) scheme.primaryContainer else scheme.surfaceContainerHigh
        val okBorder = if (okPressed) scheme.primary else scheme.outlineVariant.copy(alpha = 0.5f)
        val okText = if (okPressed) scheme.onPrimaryContainer else scheme.onSurface
        val okGlowAlpha by animateFloatAsState(
            targetValue = if (okPressed) 0.45f else 0f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
            label = "okGlow"
        )

        // Glow halo behind OK — simple solid circle with animated alpha
        Canvas(modifier = Modifier.fillMaxSize(0.60f)) {
            val r = minOf(this.size.width, this.size.height) / 2f
            drawCircle(
                color = scheme.primary.copy(alpha = okGlowAlpha),
                radius = r,
                center = androidx.compose.ui.geometry.Offset(this.size.width / 2f, this.size.height / 2f),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(0.40f)
                .shadow(if (okPressed) 2.dp else 8.dp, CircleShape)
                .background(okBg, CircleShape)
                .border(2.dp, okBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "OK",
                color = okText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun firePad(region: PadRegion, onKey: (Int) -> Unit) {
    when (region) {
        PadRegion.Up    -> onKey(Constants.KEY_UP)
        PadRegion.Down  -> onKey(Constants.KEY_DOWN)
        PadRegion.Left  -> onKey(Constants.KEY_LEFT)
        PadRegion.Right -> onKey(Constants.KEY_RIGHT)
        PadRegion.Ok    -> onKey(Constants.KEY_OK)
        PadRegion.None  -> Unit
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
        deg >= -45 && deg < 45   -> PadRegion.Right
        deg >= 45  && deg < 135  -> PadRegion.Down
        deg >= -135 && deg < -45 -> PadRegion.Up
        else                     -> PadRegion.Left
    }
}

// ─── MouseRemoteChrome ────────────────────────────────────────────────────────

@Composable
fun MouseRemoteChrome(
    onKey: (Int) -> Unit,
    trackpad: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Row: Back, Home, Menu
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NeoIconButton(onClick = { onKey(Constants.KEY_BACK) }) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardReturn, "返回", tint = scheme.onSurfaceVariant)
            }
            NeoIconButton(onClick = { onKey(Constants.KEY_HOME) }) {
                Icon(Icons.Outlined.Home, "主页", tint = scheme.onSurfaceVariant)
            }
            NeoIconButton(onClick = { onKey(Constants.KEY_MENU) }) {
                Icon(Icons.Outlined.Menu, "菜单", tint = scheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Huge Rectangular Trackpad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(6.dp, RoundedCornerShape(32.dp))
                .background(scheme.surfaceContainerLow, RoundedCornerShape(32.dp))
                .border(2.dp, scheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
        ) {
            trackpad(Modifier.fillMaxSize())
            
            // Subtle indicator for the trackpad
            Icon(
                Icons.Outlined.TouchApp,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.2f),
                modifier = Modifier.align(Alignment.Center).size(64.dp)
            )
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

// ─── NumberPad ────────────────────────────────────────────────────────────────

@Composable
fun NumberPad(
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { n ->
                    NeoIconButton(
                        onClick = { onDigit(n) },
                        size = 72.dp
                    ) {
                        Text("$n", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NeoIconButton(onClick = { onDelete() }, size = 72.dp) {
                Text("⌫", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            NeoIconButton(onClick = { onDigit(0) }, size = 72.dp) {
                Text("0", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.size(72.dp))
        }
    }
}
