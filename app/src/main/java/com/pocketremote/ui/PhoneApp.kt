package com.pocketremote.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pocketremote.ui.apps.AppsScreen
import com.pocketremote.ui.components.ConnectedShell
import com.pocketremote.ui.components.KeepAlivePane
import com.pocketremote.ui.components.injectLine
import com.pocketremote.ui.devices.DevicesScreen
import com.pocketremote.ui.info.InfoScreen
import com.pocketremote.ui.keyboard.KeyboardScreen
import com.pocketremote.ui.pin.PinScreen
import com.pocketremote.ui.remote.RemoteScreen
import com.pocketremote.ui.settings.SettingsScreen
import com.pocketremote.ui.theme.rememberReducedMotion

/** 未连接走页切换；已连接四 Tab 共用一个壳，页面保活。 */
@Composable
fun PhoneApp(viewModel: PhoneViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val reduce = rememberReducedMotion()
    val connected = state.route != Route.Devices && state.route != Route.Pin
    AnimatedContent(
        targetState = connected,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "session",
    ) { isConnected ->
        if (isConnected) {
            ConnectedSession(state, viewModel)
        } else {
            AnimatedContent(
                targetState = state.route,
                transitionSpec = {
                    if (reduce) {
                        fadeIn() togetherWith fadeOut()
                    } else {
                        (fadeIn() + slideInHorizontally { it / 8 }) togetherWith
                            (fadeOut() + slideOutHorizontally { -it / 8 })
                    }
                },
                label = "preconnect",
            ) { route ->
                when (route) {
                    Route.Pin -> PinScreen(state, viewModel)
                    else -> DevicesScreen(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun ConnectedSession(state: UiState, viewModel: PhoneViewModel) {
    val pickApk = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.upload(uri, asApk = true)
    }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.route) {
        if (state.route != Route.Keyboard) focusManager.clearFocus()
    }
    val title = when (state.route) {
        Route.Remote -> state.tvName.ifBlank { "遥控" }
        Route.Keyboard -> "键盘"
        Route.Apps -> "应用"
        Route.Info -> "设备信息"
        else -> "设置"
    }
    ConnectedShell(
        state = state,
        viewModel = viewModel,
        title = title,
        subtitle = if (state.route == Route.Remote) injectLine(state) else null,
        subtitleError = state.route == Route.Remote && !state.injectOk,
        navigationIcon = {
            if (state.route == Route.Info) {
                IconButton(onClick = { viewModel.go(Route.Settings) }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                }
            }
        },
        actions = {
            when (state.route) {
                Route.Remote -> TextButton(onClick = { viewModel.disconnectToList() }) {
                    Text("断开", color = MaterialTheme.colorScheme.error)
                }
                Route.Apps -> {
                    TextButton(onClick = { pickApk.launch("*/*") }) { Text("安装") }
                    TextButton(onClick = { viewModel.loadApps() }) { Text("刷新") }
                }
                Route.Info -> TextButton(onClick = { viewModel.refreshInfo() }) { Text("刷新") }
                else -> Unit
            }
        },
    ) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            KeepAlivePane(visible = state.route == Route.Remote) {
                RemoteScreen(state, viewModel)
            }
            KeepAlivePane(visible = state.route == Route.Keyboard) {
                KeyboardScreen(state, viewModel)
            }
            KeepAlivePane(visible = state.route == Route.Apps) {
                AppsScreen(state, viewModel)
            }
            KeepAlivePane(visible = state.route == Route.Settings || state.route == Route.Info) {
                Box(Modifier.fillMaxSize()) {
                    SettingsScreen(state, viewModel)
                    KeepAlivePane(visible = state.route == Route.Info) {
                        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            InfoScreen(state)
                        }
                    }
                }
            }
        }
    }
}
