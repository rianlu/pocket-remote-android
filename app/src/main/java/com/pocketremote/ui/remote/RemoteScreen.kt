package com.pocketremote.ui.remote

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.Constants
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.MouseRemoteChrome
import com.pocketremote.ui.components.NeoButton
import com.pocketremote.ui.components.NeoIconButton
import com.pocketremote.ui.components.NumberPad
import com.pocketremote.ui.components.RemotePad
import com.pocketremote.ui.theme.rememberReducedMotion

private enum class PadMode { Keys, Mouse }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(state: UiState, viewModel: PhoneViewModel) {
    var showNumbers by rememberSaveable { mutableStateOf(false) }
    var showKeyboard by rememberSaveable { mutableStateOf(false) }
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
        // Status / error bar
        if (!state.injectOk) {
            Surface(
                onClick = { viewModel.reconnect() },
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    "按键不可用，点击重新检测注入",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Mode switcher + Text input button
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Custom Neumorphic Mode Switcher ────────────────────────────────
            val scheme = MaterialTheme.colorScheme
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp), // breathing room for NeoButton shadows
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                NeoButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        padMode = PadMode.Keys.name
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (mode == PadMode.Keys) scheme.primaryContainer else null,
                    contentColor = if (mode == PadMode.Keys) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                ) {
                    Text(
                        "按键",
                        fontWeight = if (mode == PadMode.Keys) FontWeight.Bold else FontWeight.Normal,
                    )
                }
                NeoButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        if (mouseOk) padMode = PadMode.Mouse.name
                        else viewModel.note("当前模式不支持鼠标")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    enabled = mouseOk || mode == PadMode.Mouse,
                    containerColor = if (mode == PadMode.Mouse) scheme.primaryContainer else null,
                    contentColor = if (mode == PadMode.Mouse) scheme.onPrimaryContainer
                                   else if (!mouseOk) scheme.onSurfaceVariant.copy(alpha = 0.4f)
                                   else scheme.onSurfaceVariant,
                ) {
                    Text(
                        "鼠标",
                        fontWeight = if (mode == PadMode.Mouse) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            NeoIconButton(
                onClick = { showKeyboard = true }
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "发送文字",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))


        // Pad Area
        val padModifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 100.dp)
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
            ) { current -> padBody(current) }
        }
    }

    // Numbers Sheet
    if (showNumbers) {
        ModalBottomSheet(
            onDismissRequest = { showNumbers = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "数字键盘",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                NumberPad(
                    onDigit = { viewModel.sendKey(Constants.KEY_0 + it) },
                    onDelete = { viewModel.sendKey(Constants.KEY_DEL) },
                )
            }
        }
    }

    // Keyboard Input Sheet
    if (showKeyboard) {
        ModalBottomSheet(
            onDismissRequest = { showKeyboard = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            KeyboardInputSheet(state, viewModel) { showKeyboard = false }
        }
    }
}

@Composable
fun KeyboardInputSheet(state: UiState, viewModel: PhoneViewModel, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    fun send() {
        if (!state.injectOk) {
            viewModel.note("当前模式无法输入，请改用增强或打开网络调试。")
            return
        }
        if (text.isNotEmpty()) {
            viewModel.sendText(text)
            text = ""
            onDismiss()
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            "发送文字到电视",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!state.injectOk) {
            Spacer(Modifier.height(6.dp))
            Text(
                "当前模式无法输入，请改用增强或打开网络调试。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("搜索词、账号、密码…") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { send() }),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = {
                    if (state.injectOk) viewModel.sendKey(Constants.KEY_DEL)
                    else viewModel.note("当前模式无法操作")
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) { Text("删除") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { send() },
                enabled = state.injectOk && text.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) { Text("发送") }
        }
    }
}
