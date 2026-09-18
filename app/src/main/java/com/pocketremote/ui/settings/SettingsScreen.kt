package com.pocketremote.ui.settings

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.Constants
import com.pocketremote.protocol.TvInfo
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.Route
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.GroupDivider
import com.pocketremote.ui.components.SectionLabel
import com.pocketremote.ui.components.SettingsGroup
import com.pocketremote.ui.components.SettingsItem
import com.pocketremote.ui.components.injectLine
import com.pocketremote.ui.theme.AppearanceMode
import com.pocketremote.ui.theme.LocalThemeStore
import com.pocketremote.ui.theme.PaletteKey
import com.pocketremote.ui.theme.label
import com.pocketremote.ui.theme.swatchColor

/** 已连接设置：设备卡、外观、维护。 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: UiState, viewModel: PhoneViewModel) {
    val info = state.tvInfo
    val ctx = LocalContext.current
    val theme = LocalThemeStore.current
    val appearance by theme.appearance.collectAsState()
    val palette by theme.palette.collectAsState()
    var confirmClean by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            item(key = "tv-label") { SectionLabel("电视") }
            item(key = "tv") {
                SettingsGroup {
                    SettingsItem(
                        headline = "名称",
                        trailingText = info?.tvName.orEmpty().ifBlank { state.tvName }.ifBlank { "—" },
                    )
                    GroupDivider()
                    SettingsItem(
                        headline = "地址",
                        trailingText = address(state, info),
                        onClick = {
                            copy(ctx, address(state, info))
                            viewModel.note("已复制地址")
                        },
                    )
                    GroupDivider()
                    SettingsItem("连接模式", trailingText = injectLine(state))
                }
            }
            item(key = "device-label") { SectionLabel("设备") }
            item(key = "device") {
                SettingsGroup {
                    SettingsItem(
                        headline = "设备信息",
                        leading = { Icon(Icons.Outlined.Info, contentDescription = null) },
                        trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null) },
                        onClick = { viewModel.go(Route.Info) },
                    )
                    GroupDivider()
                    SettingsItem(
                        headline = "助手",
                        trailingText = info?.helper.orEmpty().ifBlank { "—" },
                        leading = { Icon(Icons.Outlined.Tv, contentDescription = null) },
                    )
                }
            }
            item(key = "look-label") { SectionLabel("外观") }
            item(key = "look") {
                SettingsGroup {
                    SettingsItem("深浅色", supporting = appearance.label())
                    SingleChoiceSegmentedButtonRow(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        val modes = AppearanceMode.entries
                        modes.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = appearance == mode,
                                onClick = { theme.setAppearance(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                            ) {
                                Text(
                                    when (mode) {
                                        AppearanceMode.System -> "系统"
                                        AppearanceMode.Light -> "浅色"
                                        AppearanceMode.Dark -> "深色"
                                    },
                                )
                            }
                        }
                    }
                    GroupDivider()
                    SettingsItem("主题色", supporting = palette.label())
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
            item(key = "maint-label") { SectionLabel("维护") }
            item(key = "maint") {
                SettingsGroup {
                    SettingsItem(
                        headline = "清理后台",
                        supporting = "结束后台应用，不删文件、不清缓存",
                        leading = { Icon(Icons.Outlined.CleaningServices, contentDescription = null) },
                        onClick = { confirmClean = true },
                    )
                }
            }
        }
    }
    if (confirmClean) {
        AlertDialog(
            onDismissRequest = { confirmClean = false },
            title = { Text("清理后台") },
            text = { Text("将结束后台应用以释放内存。正在使用的前台应用不会被关掉。") },
            confirmButton = {
                TextButton(onClick = {
                    confirmClean = false
                    viewModel.requestClean()
                }) { Text("结束后台") }
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
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(key.swatchColor())
                .border(if (selected) 3.dp else 1.dp, border, CircleShape)
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            key.label(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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

private fun copy(ctx: Context, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("tv", text))
}
