package com.pocketremote.ui.devices

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.pocketremote.ui.components.NeoButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.Constants
import com.pocketremote.protocol.TvInfo
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.NeoGroupDivider
import com.pocketremote.ui.components.NeoIconBadge
import com.pocketremote.ui.components.NeoItemPanel
import com.pocketremote.ui.components.NeoSectionLabel
import com.pocketremote.ui.components.NeoSettingsGroup
import com.pocketremote.ui.components.NeoSettingsItem
import com.pocketremote.ui.theme.AppearanceMode
import com.pocketremote.ui.theme.LocalThemeStore
import com.pocketremote.ui.theme.PaletteKey
import com.pocketremote.ui.theme.label
import com.pocketremote.ui.theme.swatchColor

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(state: UiState, viewModel: PhoneViewModel) {
    val info = state.tvInfo
    val ctx = LocalContext.current
    val theme = LocalThemeStore.current
    val appearance by theme.appearance.collectAsState()
    val palette by theme.palette.collectAsState()
    val view = LocalView.current
    var confirmClean by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item(key = "connection_card") {
                Spacer(Modifier.height(4.dp))
                // Connection Status Card - uses theme surface + colorScheme
                NeoItemPanel(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column {
                        // Status indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "已连接",
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = { viewModel.refreshInfo() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = "刷新",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = info?.tvName.orEmpty().ifBlank { state.tvName }.ifBlank { "未知设备" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = address(state, info),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable {
                                val addr = address(state, info)
                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("ip", addr))
                            }
                        )
                        Spacer(Modifier.height(20.dp))
                        // Disconnect button
                        Surface(
                            onClick = { viewModel.disconnectToList() },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "断开连接",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            item(key = "maint") {
                NeoSectionLabel("电视工具")
                NeoItemPanel(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { confirmClean = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeoIconBadge(
                            icon = Icons.Outlined.CleaningServices,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            background = MaterialTheme.colorScheme.tertiaryContainer,
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "清理电视后台",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "结束后台应用，不删文件、不清缓存",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item(key = "info") {
                NeoSectionLabel("设备信息")
                NeoSettingsGroup(Modifier.fillMaxWidth()) {
                    NeoSettingsItem(
                        "厂商品牌",
                        leadingIcon = Icons.Outlined.Info,
                        trailingText = "${info?.manufacturer.orEmpty()} ${info?.brand.orEmpty()}".trim().ifBlank { "—" }
                    )
                    NeoGroupDivider()
                    NeoSettingsItem("系统固件", trailingText = "${info?.android.orEmpty()} (API ${info?.sdk ?: state.tvSdk})")
                    NeoGroupDivider()
                    NeoSettingsItem("硬件参数", trailingText = "${info?.hardware.orEmpty()} ${info?.cpu.orEmpty()}")
                    NeoGroupDivider()
                    NeoSettingsItem(
                        "屏幕分辨率",
                        trailingText = if (info != null && info.width > 0) "${info.width} × ${info.height}" else "—"
                    )
                    NeoGroupDivider()
                    NeoSettingsItem(
                        "内部存储",
                        trailingText = if (info != null && info.storageTotalMb > 0)
                            "${info.storageFreeMb} / ${info.storageTotalMb} MB 可用" else "—"
                    )
                    NeoGroupDivider()
                    NeoSettingsItem(
                        "运行内存",
                        trailingText = if (info != null && info.ramMb > 0)
                            "${info.ramAvailMb} / ${info.ramMb} MB 可用" else "—"
                    )
                }
            }

            item(key = "look") {
                NeoSectionLabel("外观设置")
                NeoSettingsGroup(Modifier.fillMaxWidth()) {

                    NeoSettingsItem(
                        "主题色",
                        leadingIcon = Icons.Outlined.Palette,
                        supporting = palette.label()
                    )
                    FlowRow(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val keys = buildList {
                            if (Build.VERSION.SDK_INT >= 31) add(PaletteKey.Dynamic)
                            add(PaletteKey.Blue)
                            add(PaletteKey.Green)
                            add(PaletteKey.Orange)
                            add(PaletteKey.Purple)
                            add(PaletteKey.Teal)
                        }
                        keys.forEach { key ->
                            PaletteSwatch(
                                key = key,
                                selected = palette == key,
                                onClick = { theme.setPalette(key) },
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (confirmClean) {
        AlertDialog(
            onDismissRequest = { confirmClean = false },
            title = { Text("清理后台") },
            text = { Text("将结束后台应用以释放内存。正在使用的前台应用不会被关掉。") },
            confirmButton = {
                TextButton(
                    onClick = { confirmClean = false; viewModel.requestClean() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text("结束后台") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClean = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun PaletteSwatch(
    key: PaletteKey,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outlineVariant
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(key.swatchColor())
                .border(if (selected) 3.dp else 1.dp, borderColor, CircleShape)
                .semantics {
                    role = Role.RadioButton
                    this.selected = selected
                    contentDescription = key.label()
                }
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            if (key == PaletteKey.Dynamic) {
                Icon(
                    Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            key.label(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

internal fun address(state: UiState, info: TvInfo?): String {
    val ip = info?.ip.orEmpty()
    val host = state.selected?.host.orEmpty()
    val shown = ip.ifBlank { host }
    val port = state.selected?.port ?: Constants.CONTROL_PORT
    return if (shown.isBlank()) "—" else "$shown:$port"
}
