package com.pocketremote.ui.devices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.Constants
import com.pocketremote.protocol.TvDevice
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.AppScaffold
import com.pocketremote.ui.components.CapsuleButton
import com.pocketremote.ui.components.SectionLabel

/** 发现局域网电视；找不到时弹窗手填 IP。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(state: UiState, viewModel: PhoneViewModel) {
    var host by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf(Constants.CONTROL_PORT.toString()) }
    var showManual by rememberSaveable { mutableStateOf(false) }
    val plugin = state.injectPref == Constants.INJECT_PLUGIN
    AppScaffold(
        title = "口袋遥控",
        actions = {
            if (state.scanning) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                        .semantics { contentDescription = "正在搜索" },
                    strokeWidth = 2.dp,
                )
            } else {
                TextButton(onClick = { viewModel.scan() }) { Text("搜索") }
            }
        },
    ) {
        Text(
            state.message.ifBlank { "同一 Wi‑Fi 下的电视会出现在下面" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            item(key = "mode") {
                SectionLabel("连接模式")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = plugin,
                        onClick = { viewModel.setInjectPref(Constants.INJECT_PLUGIN) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text("增强") }
                    SegmentedButton(
                        selected = !plugin,
                        onClick = { viewModel.setInjectPref(Constants.INJECT_ADB) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text("ADB") }
                }
                Text(
                    if (plugin) "系统插件，含鼠标。连上后不可更改。" else "本机调试，无鼠标。连上后不可更改。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                )
            }
            item(key = "devices-label") { SectionLabel("设备") }
            if (state.devices.isEmpty() && !state.scanning) {
                item(key = "empty") {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Outlined.Tv,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("未发现设备", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "请先在电视上打开「口袋遥控助手」",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                items(state.devices, key = { "${it.host}:${it.port}" }) { device ->
                    DeviceCard(
                        device = device,
                        connecting = state.connecting && state.selected?.host == device.host,
                        enabled = !state.connecting,
                        onConnect = { viewModel.connect(device) },
                    )
                }
            }
            item(key = "manual") {
                FilledTonalButton(
                    onClick = { showManual = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
                ) { Text("手动添加") }
            }
        }
    }
    if (showManual) {
        ModalBottomSheet(
            onDismissRequest = { showManual = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
            ) {
                Text("手动添加", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("电视与手机同一 Wi‑Fi。端口一般不用改。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("IP 地址") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it.filter { c -> c.isDigit() }.take(5) },
                    label = { Text("端口") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(Modifier.height(24.dp))
                CapsuleButton(
                    text = if (state.connecting) "连接中…" else "连接",
                    onClick = {
                        showManual = false
                        viewModel.connectManual(host, port)
                    },
                    enabled = host.isNotBlank() && !state.connecting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: TvDevice,
    connecting: Boolean,
    enabled: Boolean,
    onConnect: () -> Unit,
) {
    OutlinedCard(
        onClick = onConnect,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    Icons.Outlined.Tv,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(
                    device.name.ifBlank { device.host },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    device.host,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                if (connecting) "连接中" else "连接",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
