package com.pockettv.phone.ui.files

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pockettv.phone.ui.PhoneViewModel
import com.pockettv.phone.ui.Route
import com.pockettv.phone.ui.UiState

/** 选择本机文件上传到电视 inbox 或 apk。 */
@Composable
fun FilesScreen(state: UiState, viewModel: PhoneViewModel) {
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.upload(uri, asApk = false)
    }
    val pickApk = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.upload(uri, asApk = true)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("传文件", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("普通文件到 inbox；APK 到 apk 并尝试调起安装。")
        if (state.message.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(state.message)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { pickFile.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("上传到 inbox")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { pickApk.launch("application/vnd.android.package-archive") }, modifier = Modifier.fillMaxWidth()) {
            Text("上传 APK 并安装")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { viewModel.go(Route.Remote) }, modifier = Modifier.fillMaxWidth()) {
            Text("返回遥控板")
        }
    }
}
