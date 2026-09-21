package com.pocketremote.ui.devices
import com.pocketremote.ui.theme.vibrate

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.Constants
import com.pocketremote.protocol.TvDevice
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.AppScaffold
import com.pocketremote.ui.components.NeoButton
import com.pocketremote.ui.components.NeoIconBadge
import com.pocketremote.ui.components.NeoItemPanel
import com.pocketremote.ui.components.NeoSectionLabel
import com.pocketremote.ui.components.NeoSettingsGroup
import com.pocketremote.ui.components.NeoSettingsItem

/** 发现局域网电视；找不到时弹窗手填 IP。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(state: UiState, viewModel: PhoneViewModel) {
    var host by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf(Constants.CONTROL_PORT.toString()) }
    var showManual by rememberSaveable { mutableStateOf(false) }
    val plugin = state.injectPref == Constants.INJECT_PLUGIN
    val view = LocalView.current

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
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                IconButton(onClick = {
                    view.vibrate(HapticFeedbackConstants.CONTEXT_CLICK)
                    viewModel.scan()
                }) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "搜索", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = { showManual = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "手动添加", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    ) {
        if (state.message.isNotBlank()) {
            Text(
                state.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
        ) {
            // Connect Mode
            item(key = "mode") {
                NeoSectionLabel("连接模式")
                NeoSettingsGroup(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModeButton(
                            label = "增强模式",
                            sub = "鼠标 + 按键",
                            selected = plugin,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setInjectPref(Constants.INJECT_PLUGIN) }
                        )
                        ModeButton(
                            label = "ADB 模式",
                            sub = "仅按键",
                            selected = !plugin,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setInjectPref(Constants.INJECT_ADB) }
                        )
                    }
                    Text(
                        if (plugin) "系统插件，含鼠标功能。连上后不可更改。"
                        else "需打开手机开发者选项中的「网络调试」。连上后不可更改。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
                    )
                }
            }

            // Device List
            item(key = "devices-label") { NeoSectionLabel("附近设备") }

            if (state.devices.isEmpty() && !state.scanning) {
                item(key = "empty") {
                    NeoItemPanel(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Outlined.Tv,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("未发现设备", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "请先在电视上打开「口袋遥控助手」\n并确保手机与电视在同一 Wi‑Fi",
                                style = MaterialTheme.typography.bodySmall,
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

            item(key = "spacer") { Spacer(Modifier.height(24.dp)) }
        }
    }

    // Manual Add Sheet
    if (showManual) {
        ModalBottomSheet(
            onDismissRequest = { showManual = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text("手动连接", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "电视与手机需在同一 Wi‑Fi 下，端口通常无需更改。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("电视 IP 地址") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it.filter { c -> c.isDigit() }.take(5) },
                    label = { Text("端口") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        showManual = false
                        viewModel.connectManual(host, port)
                    },
                    enabled = host.isNotBlank() && !state.connecting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (state.connecting) "连接中…" else "连接", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    label: String,
    sub: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val bg = if (selected) scheme.primaryContainer else null
    val labelColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface
    val subColor = if (selected) scheme.onPrimaryContainer.copy(alpha = 0.7f)
                   else scheme.onSurfaceVariant
    val view = LocalView.current

    NeoButton(
        onClick = {
            view.vibrate(HapticFeedbackConstants.CONTEXT_CLICK)
            onClick()
        },
        modifier = modifier.height(64.dp),
        containerColor = bg,
        contentColor = labelColor,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = labelColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = subColor)
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
    NeoItemPanel(
        onClick = if (enabled) onConnect else null,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoIconBadge(
                icon = Icons.Outlined.Tv,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                background = MaterialTheme.colorScheme.primaryContainer,
                size = 44.dp,
                iconSize = 22.dp
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    device.name.ifBlank { device.host },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    device.host,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (connecting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    "连接",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
