package com.pockettv.phone.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pockettv.phone.protocol.Constants
import com.pockettv.phone.ui.PhoneViewModel
import com.pockettv.phone.ui.UiState

/** 发现设备或手填 IP。 */
@Composable
fun DevicesScreen(state: UiState, viewModel: PhoneViewModel) {
    var host by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf(Constants.CONTROL_PORT.toString()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text("口袋遥控", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(state.message.ifBlank { "同一 Wi-Fi 下的电视会出现在列表中" }, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { viewModel.scan() }, enabled = !state.scanning) { Text("重新搜索") }
            if (state.scanning) CircularProgressIndicator(modifier = Modifier.height(24.dp))
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
            items(state.devices, key = { it.host + it.port }) { device ->
                ListItem(
                    headlineContent = { Text(device.name.ifBlank { device.host }) },
                    supportingContent = { Text("${device.host}:${device.port}") },
                    modifier = Modifier.clickable { viewModel.connect(device) },
                )
            }
        }
        Text("手填 IP", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("电视 IP") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("端口") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { viewModel.connectManual(host, port) },
            enabled = host.isNotBlank() && !state.connecting,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (state.connecting) "连接中…" else "连接") }
    }
}
