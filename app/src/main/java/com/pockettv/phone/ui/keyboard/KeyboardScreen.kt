package com.pockettv.phone.ui.keyboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pockettv.phone.ui.PhoneViewModel
import com.pockettv.phone.ui.Route
import com.pockettv.phone.ui.UiState

/** 手动把文本发到电视当前焦点，不监听电视输入框。 */
@Composable
fun KeyboardScreen(state: UiState, viewModel: PhoneViewModel) {
    var text by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("发送文本", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("请先在电视上点进输入框，再点发送。")
        if (state.message.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(state.message)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("内容") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.sendText(text) },
            enabled = text.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("发送") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.go(Route.Remote) }, modifier = Modifier.fillMaxWidth()) {
            Text("返回遥控板")
        }
    }
}
