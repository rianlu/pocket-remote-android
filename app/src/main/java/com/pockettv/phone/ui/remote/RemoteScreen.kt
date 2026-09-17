package com.pockettv.phone.ui.remote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pockettv.phone.protocol.Constants
import com.pockettv.phone.ui.PhoneViewModel
import com.pockettv.phone.ui.Route
import com.pockettv.phone.ui.UiState

/** 方向键遥控板，按键立即反馈。 */
@Composable
fun RemoteScreen(state: UiState, viewModel: PhoneViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(state.tvName.ifBlank { "已连接" }, style = MaterialTheme.typography.titleLarge)
        Text(state.message, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Dpad { viewModel.sendKey(it) }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RemoteBtn("返回") { viewModel.sendKey(Constants.KEY_BACK) }
            RemoteBtn("主页") { viewModel.sendKey(Constants.KEY_HOME) }
            RemoteBtn("菜单") { viewModel.sendKey(Constants.KEY_MENU) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RemoteBtn("音量-") { viewModel.sendKey(Constants.KEY_VOL_DOWN) }
            RemoteBtn("静音") { viewModel.sendKey(Constants.KEY_MUTE) }
            RemoteBtn("音量+") { viewModel.sendKey(Constants.KEY_VOL_UP) }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            (0..9).forEach { n ->
                FilledTonalButton(
                    onClick = { viewModel.sendKey(7 + n) },
                    modifier = Modifier.weight(1f),
                ) { Text(n.toString()) }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { viewModel.go(Route.Keyboard) }, modifier = Modifier.fillMaxWidth()) { Text("键盘（发送文本）") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.go(Route.Files) }, modifier = Modifier.fillMaxWidth()) { Text("传文件 / 安装包") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.go(Route.Apps) }, modifier = Modifier.fillMaxWidth()) { Text("电视应用") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.disconnectToList() }, modifier = Modifier.fillMaxWidth()) {
            Text("断开")
        }
    }
}

@Composable
private fun Dpad(onKey: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RemoteBtn("上", wide = true) { onKey(Constants.KEY_UP) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RemoteBtn("左") { onKey(Constants.KEY_LEFT) }
            RemoteBtn("OK") { onKey(Constants.KEY_OK) }
            RemoteBtn("右") { onKey(Constants.KEY_RIGHT) }
        }
        RemoteBtn("下", wide = true) { onKey(Constants.KEY_DOWN) }
    }
}

@Composable
private fun RemoteBtn(label: String, wide: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = if (wide) Modifier.fillMaxWidth(0.5f).height(56.dp) else Modifier.size(width = 88.dp, height = 56.dp),
    ) { Text(label) }
}
