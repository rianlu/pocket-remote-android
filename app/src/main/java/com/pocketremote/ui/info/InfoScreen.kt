package com.pocketremote.ui.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pocketremote.protocol.TvInfo
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.GroupDivider
import com.pocketremote.ui.components.SectionLabel
import com.pocketremote.ui.components.SettingsGroup
import com.pocketremote.ui.components.SettingsItem

/** 电视硬件与系统详情，从设置进入。 */
@Composable
fun InfoScreen(state: UiState) {
    val info = state.tvInfo
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            item(key = "net-label") { SectionLabel("网络") }
            item(key = "net") {
                SettingsGroup {
                    SettingsItem("MAC", trailingText = info?.mac.orEmpty().ifBlank { "—" })
                }
            }
            item(key = "sys-label") { SectionLabel("系统") }
            item(key = "sys") {
                SettingsGroup {
                    SettingsItem("厂商", trailingText = info?.manufacturer.orEmpty().ifBlank { "—" })
                    GroupDivider()
                    SettingsItem("品牌", trailingText = info?.brand.orEmpty().ifBlank { "—" })
                    GroupDivider()
                    SettingsItem("Android", trailingText = androidLine(info, state.tvSdk))
                    GroupDivider()
                    SettingsItem("固件", trailingText = info?.firmware.orEmpty().ifBlank { "—" })
                    GroupDivider()
                    SettingsItem("硬件", trailingText = info?.hardware.orEmpty().ifBlank { "—" })
                    GroupDivider()
                    SettingsItem("芯片", trailingText = info?.soc.orEmpty().ifBlank { "—" })
                    GroupDivider()
                    SettingsItem("CPU", trailingText = cpuLine(info))
                    GroupDivider()
                    SettingsItem("分辨率", trailingText = resolution(info))
                    GroupDivider()
                    SettingsItem("DPI", trailingText = if (info != null && info.density > 0) "${info.density}" else "—")
                }
            }
            item(key = "sto-label") { SectionLabel("存储") }
            item(key = "sto") {
                SettingsGroup {
                    SettingsItem("内存", trailingText = ramLine(info))
                    GroupDivider()
                    SettingsItem("内部存储", trailingText = storageLine(info))
                    GroupDivider()
                    SettingsItem("助手缓存", trailingText = if (info != null) "${info.cacheMb} MB" else "—")
                }
            }
            item(key = "helper-label") { SectionLabel("助手") }
            item(key = "helper") {
                SettingsGroup {
                    SettingsItem("版本", trailingText = info?.helper.orEmpty().ifBlank { "—" })
                }
            }
        }
    }
}

private fun androidLine(info: TvInfo?, sdk: Int): String {
    val rel = info?.android.orEmpty()
    val n = info?.sdk ?: sdk
    return if (rel.isNotBlank()) "$rel（API $n）" else if (n > 0) "API $n" else "—"
}

private fun resolution(info: TvInfo?): String {
    if (info == null || info.width <= 0 || info.height <= 0) return "—"
    return "${info.width} × ${info.height}"
}

private fun cpuLine(info: TvInfo?): String {
    val cpu = info?.cpu.orEmpty()
    val abi = info?.abi.orEmpty()
    return when {
        cpu.isNotBlank() && abi.isNotBlank() && !cpu.contains(abi) -> "$cpu（$abi）"
        cpu.isNotBlank() -> cpu
        abi.isNotBlank() -> abi
        else -> "—"
    }
}

private fun ramLine(info: TvInfo?): String {
    if (info == null || info.ramMb <= 0) return "—"
    return "${info.ramAvailMb} / ${info.ramMb} MB 可用"
}

private fun storageLine(info: TvInfo?): String {
    if (info == null || info.storageTotalMb <= 0) return "—"
    return "${info.storageFreeMb} / ${info.storageTotalMb} MB 可用"
}
