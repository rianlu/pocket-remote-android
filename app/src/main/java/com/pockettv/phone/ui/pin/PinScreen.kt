package com.pockettv.phone.ui.pin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pockettv.phone.ui.PhoneViewModel
import com.pockettv.phone.ui.UiState

/** 输入电视屏幕上的 6 位配对码。 */
@Composable
fun PinScreen(state: UiState, viewModel: PhoneViewModel) {
    var pin by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("输入配对码", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(state.message.ifBlank { "请看电视全屏上的 6 位数字" })
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6) pin = it.filter { c -> c.isDigit() } },
            label = { Text("PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.submitPin(pin) },
            enabled = pin.length == 6,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("确认") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.disconnectToList() }, modifier = Modifier.fillMaxWidth()) {
            Text("返回设备列表")
        }
    }
}
