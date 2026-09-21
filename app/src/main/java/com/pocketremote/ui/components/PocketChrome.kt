package com.pocketremote.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pocketremote.protocol.Constants
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.Route
import com.pocketremote.ui.UiState
import com.pocketremote.ui.theme.rememberReducedMotion

// ─── AppScaffold ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp),
            content = content,
        )
    }
}

// ─── ConnectedShell ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedShell(
    state: UiState,
    viewModel: PhoneViewModel,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleError: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val view = LocalView.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            title,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (subtitleError)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Tab content area — allow content to draw behind the dock
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                content = content,
            )

            // ── Floating Pill Dock ────────────────────────────────────────────
            val scheme = MaterialTheme.colorScheme
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 32.dp, end = 32.dp)
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(32.dp))
                    .background(
                        scheme.surfaceContainerHigh.copy(alpha = 0.92f),
                        RoundedCornerShape(32.dp)
                    )
                    .border(
                        1.dp,
                        scheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(32.dp)
                    )
                    .clip(RoundedCornerShape(32.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillDockItem(
                    icon = if (state.route == Route.Remote) Icons.Filled.Tv else Icons.Outlined.Tv,
                    label = "遥控",
                    selected = state.route == Route.Remote,
                    modifier = Modifier.weight(1f),
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    viewModel.go(Route.Remote)
                }
                PillDockItem(
                    icon = if (state.route == Route.Apps) Icons.Filled.Apps else Icons.Outlined.Apps,
                    label = "应用",
                    selected = state.route == Route.Apps,
                    modifier = Modifier.weight(1f),
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    viewModel.go(Route.Apps)
                }
                PillDockItem(
                    icon = if (state.route == Route.Dashboard) Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "设备",
                    selected = state.route == Route.Dashboard,
                    modifier = Modifier.weight(1f),
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    viewModel.go(Route.Dashboard)
                }
            }
        }
    }
}

// ─── PillDockItem ─────────────────────────────────────────────────────────────

@Composable
private fun PillDockItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val iconTint = if (selected) scheme.primary else scheme.onSurfaceVariant
    val labelColor = if (selected) scheme.primary else scheme.onSurfaceVariant
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Selected indicator dot above icon
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    if (selected) scheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(2.dp)
                )
        )
        Spacer(Modifier.size(4.dp))
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.size(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 10.sp,
        )
    }
}

// ─── KeepAlivePane ───────────────────────────────────────────────────────────

/** Tab 页保活：切走只隐藏，不离开 composition。 */
@Composable
fun KeepAlivePane(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduce = rememberReducedMotion()
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (reduce) {
            snap()
        } else {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        },
        label = "tab-alpha",
    )
    val placed = visible || alpha > 0.01f
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(if (visible) 1f else 0f)
            .graphicsLayer { this.alpha = alpha }
            .then(
                if (placed) {
                    Modifier
                } else {
                    Modifier.layout { _, _ -> layout(0, 0) {} }
                },
            )
            .focusProperties { canFocus = visible }
            .then(if (visible) Modifier else Modifier.clearAndSetSemantics { })
            .pointerInput(visible) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        if (visible) {
                            event.changes.forEach { change ->
                                if (!change.isConsumed) change.consume()
                            }
                        }
                    }
                }
            },
    ) {
        content()
    }
}

// ─── injectLine ──────────────────────────────────────────────────────────────

fun injectLine(state: UiState): String {
    return when (state.injectMode) {
        Constants.INJECT_PLUGIN -> "增强"
        Constants.INJECT_ADB -> "ADB"
        Constants.INJECT_INPUT -> "标准"
        Constants.INJECT_NONE -> "不可用"
        else -> if (state.injectOk) "增强" else "不可用"
    }
}
