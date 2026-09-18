package com.pocketremote.ui.remote

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.Constants
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.MouseRemoteChrome
import com.pocketremote.ui.components.NumberPad
import com.pocketremote.ui.components.RemotePad
import com.pocketremote.ui.theme.rememberReducedMotion

private enum class PadMode { Keys, Mouse }

/** 遥控：方向键 / 鼠标。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(state: UiState, viewModel: PhoneViewModel) {
    var showNumbers by rememberSaveable { mutableStateOf(false) }
    var padMode by rememberSaveable { mutableStateOf(PadMode.Keys.name) }
    val mode = runCatching { PadMode.valueOf(padMode) }.getOrDefault(PadMode.Keys)
    val mouseOk = state.injectMode == Constants.INJECT_PLUGIN && state.injectOk
    val view = LocalView.current
    LaunchedEffect(mouseOk) {
        if (!mouseOk && padMode == PadMode.Mouse.name) {
            padMode = PadMode.Keys.name
        }
    }
    val reduceMotion = rememberReducedMotion()
    Column(Modifier.fillMaxSize()) {
        if (!state.injectOk) {
            TextButton(onClick = { viewModel.reconnect() }) { Text("重新检测注入") }
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            SegmentedButton(
                selected = mode == PadMode.Keys,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    padMode = PadMode.Keys.name
                },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
            ) { Text("按键") }
            SegmentedButton(
                selected = mode == PadMode.Mouse,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    if (mouseOk) padMode = PadMode.Mouse.name else viewModel.note("当前模式不支持鼠标")
                },
                enabled = mouseOk,
                shape = SegmentedButtonDefaults.itemShape(1, 2),
            ) { Text("鼠标") }
        }
        val padModifier = Modifier.fillMaxWidth().weight(1f)
        val padBody: @Composable (PadMode) -> Unit = { current ->
            when (current) {
                PadMode.Keys -> RemotePad(
                    onKey = { viewModel.sendKey(it) },
                    onOpenNumbers = { showNumbers = true },
                    modifier = Modifier.fillMaxSize(),
                )
                PadMode.Mouse -> MouseRemoteChrome(
                    onKey = { viewModel.sendKey(it) },
                    modifier = Modifier.fillMaxSize(),
                    trackpad = { padMod ->
                        MouseTrackpad(
                            onMove = { dx, dy -> viewModel.sendPointerMove(dx, dy) },
                            onClick = { viewModel.sendPointerClick() },
                            onUp = { viewModel.flushPointer() },
                            modifier = padMod,
                        )
                    },
                )
            }
        }
        if (reduceMotion) {
            Box(padModifier) { padBody(mode) }
        } else {
            AnimatedContent(
                targetState = mode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "pad",
                modifier = padModifier,
            ) { current ->
                padBody(current)
            }
        }
    }
    if (showNumbers) {
        ModalBottomSheet(
            onDismissRequest = { showNumbers = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("数字", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                NumberPad(
                    onDigit = { viewModel.sendKey(Constants.KEY_0 + it) },
                    onDelete = { viewModel.sendKey(Constants.KEY_DEL) },
                )
            }
        }
    }
}
