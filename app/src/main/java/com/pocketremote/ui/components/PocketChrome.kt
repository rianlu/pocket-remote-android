package com.pocketremote.ui.components

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.pocketremote.protocol.Constants
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.Route
import com.pocketremote.ui.UiState
import com.pocketremote.ui.theme.rememberReducedMotion

/** 未连接页：顶栏 + 内容。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        floatingActionButton = floatingActionButton,
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            content = content,
        )
    }
}

/** 已连接页：顶栏 + 底栏 遥控/键盘/应用/设置。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedShell(
    state: UiState,
    viewModel: PhoneViewModel,
    title: String,
    subtitle: String? = null,
    subtitleError: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    when (state.route) {
        Route.Remote -> Unit
        Route.Info -> BackHandler { viewModel.go(Route.Settings) }
        else -> BackHandler { viewModel.go(Route.Remote) }
    }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        val msg = state.message
        if (msg.isNotBlank()) {
            snackbar.showSnackbar(msg)
            viewModel.consumeMessage()
        }
    }
    val view = LocalView.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = navigationIcon,
                actions = {
                    if (!subtitle.isNullOrBlank()) {
                        StatusChip(text = subtitle, error = subtitleError)
                    }
                    actions()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                NavItem(
                    selected = state.route == Route.Remote,
                    label = "遥控",
                    selectedIcon = Icons.Filled.Tv,
                    icon = Icons.Outlined.Tv,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.go(Route.Remote)
                    },
                )
                NavItem(
                    selected = state.route == Route.Keyboard,
                    label = "键盘",
                    selectedIcon = Icons.Filled.Keyboard,
                    icon = Icons.Outlined.Keyboard,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.go(Route.Keyboard)
                    },
                )
                NavItem(
                    selected = state.route == Route.Apps,
                    label = "应用",
                    selectedIcon = Icons.Filled.Apps,
                    icon = Icons.Outlined.Apps,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.go(Route.Apps)
                    },
                )
                NavItem(
                    selected = state.route == Route.Settings || state.route == Route.Info,
                    label = "设置",
                    selectedIcon = Icons.Filled.Settings,
                    icon = Icons.Outlined.Settings,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.go(Route.Settings)
                    },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            content = content,
        )
    }
}

@Composable
private fun StatusChip(text: String, error: Boolean) {
    val container = if (error) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val content = if (error) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        modifier = Modifier.padding(end = 4.dp),
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}

fun injectLine(state: UiState): String {
    return when (state.injectMode) {
        Constants.INJECT_PLUGIN -> "增强"
        Constants.INJECT_ADB -> "ADB"
        Constants.INJECT_INPUT -> "标准"
        Constants.INJECT_NONE -> "不可用"
        else -> if (state.injectOk) "增强" else "不可用"
    }
}

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

@Composable
private fun RowScope.NavItem(
    selected: Boolean,
    label: String,
    selectedIcon: ImageVector,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(if (selected) selectedIcon else icon, contentDescription = null) },
        label = { Text(label) },
    )
}
