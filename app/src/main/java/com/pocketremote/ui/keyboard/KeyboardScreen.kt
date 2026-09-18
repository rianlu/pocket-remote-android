package com.pocketremote.ui.keyboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.Constants
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.CapsuleButton

/** 向电视当前焦点发文本。 */
@Composable
fun KeyboardScreen(state: UiState, viewModel: PhoneViewModel) {
    var text by rememberSaveable { mutableStateOf("") }
    fun send() {
        if (!state.injectOk) {
            viewModel.note("当前模式无法输入，请改用增强或打开网络调试。")
            return
        }
        if (text.isNotEmpty()) {
            viewModel.sendText(text)
            text = ""
        }
    }
    Column(Modifier.fillMaxSize()) {
        Text(
            if (state.injectOk) "先在电视上点进输入框。" else "当前模式无法输入，请改用增强或打开网络调试。",
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.injectOk) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("发送到电视") },
            placeholder = { Text("搜索词、账号、密码") },
            modifier = Modifier.fillMaxWidth().height(180.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { send() }),
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            FilledTonalButton(
                onClick = {
                    if (state.injectOk) viewModel.sendKey(Constants.KEY_DEL)
                    else viewModel.note("当前模式无法输入，请改用增强或打开网络调试。")
                },
                enabled = state.injectOk,
            ) { Text("删除") }
            Spacer(Modifier.weight(1f))
            FilledTonalButton(
                onClick = {
                    if (state.injectOk) viewModel.sendKey(Constants.KEY_CLEAR)
                    else viewModel.note("当前模式无法输入，请改用增强或打开网络调试。")
                },
                enabled = state.injectOk,
            ) { Text("清空") }
        }
        Spacer(Modifier.height(12.dp))
        CapsuleButton(
            text = "发送",
            onClick = { send() },
            enabled = state.injectOk && text.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
    }
}
