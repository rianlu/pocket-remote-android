package com.pocketremote.ui.apps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.TvApp
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.CapsuleButton
import com.pocketremote.ui.components.CapsuleTone
import com.pocketremote.ui.components.SettingsItem
import java.util.Locale

/** 电视可打开的应用。 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppsScreen(state: UiState, viewModel: PhoneViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("all") }
    var sheetPkg by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingUninstall by rememberSaveable { mutableStateOf<String?>(null) }
    var extractPkg by rememberSaveable { mutableStateOf<String?>(null) }
    val filtered = state.apps.filter { app ->
        val match = query.isBlank() ||
            app.name.contains(query, ignoreCase = true) ||
            app.pkg.contains(query, ignoreCase = true)
        val type = when (filter) {
            "user" -> !app.system
            "system" -> app.system
            else -> true
        }
        match && type
    }
    val sheetApp = state.apps.find { it.pkg == sheetPkg }
    val pendingApp = state.apps.find { it.pkg == pendingUninstall }
    val saveApk = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")) { uri ->
        val pkg = extractPkg
        extractPkg = null
        if (uri != null && pkg != null) viewModel.extractApk(pkg, uri)
    }
    Column(Modifier.fillMaxSize()) {
        Text(
            if (state.apps.isEmpty()) "正在拉取可打开的应用…" else "${state.apps.size} 个可打开的应用",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.uploadProgress >= 0f) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.uploadProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("搜索") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = filter == "all", onClick = { filter = "all" }, label = { Text("全部") })
            FilterChip(selected = filter == "user", onClick = { filter = "user" }, label = { Text("用户") })
            FilterChip(selected = filter == "system", onClick = { filter = "system" }, label = { Text("系统") })
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(filtered, key = { it.pkg }) { app ->
                AppRow(
                    app = app,
                    icon = viewModel.iconMap[app.pkg],
                    onClick = { sheetPkg = app.pkg },
                )
            }
        }
    }
    if (sheetApp != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetPkg = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
            ) {
                Text(sheetApp.name.ifBlank { sheetApp.pkg }, style = MaterialTheme.typography.headlineSmall)
                if (sheetApp.size > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(formatSize(sheetApp.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val restriction = when {
                    sheetApp.system -> "系统应用无法卸载或提取"
                    !sheetApp.extractable -> "该应用含分体包，无法提取完整 APK"
                    else -> null
                }
                if (restriction != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(restriction, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(20.dp))
                CapsuleButton(
                    text = "打开",
                    onClick = {
                        viewModel.openApp(sheetApp.pkg)
                        sheetPkg = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (sheetApp.extractable) {
                    Spacer(Modifier.height(8.dp))
                    CapsuleButton(
                        text = "提取 APK",
                        onClick = {
                            extractPkg = sheetApp.pkg
                            val file = (if (sheetApp.name.isBlank() || sheetApp.name == sheetApp.pkg) sheetApp.pkg else sheetApp.name) + ".apk"
                            saveApk.launch(file.replace('/', '_'))
                            sheetPkg = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        tone = CapsuleTone.Neutral,
                    )
                }
                if (!sheetApp.system) {
                    Spacer(Modifier.height(8.dp))
                    CapsuleButton(
                        text = "卸载",
                        onClick = {
                            pendingUninstall = sheetApp.pkg
                            sheetPkg = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        tone = CapsuleTone.Destructive,
                    )
                }
            }
        }
    }
    if (pendingApp != null) {
        AlertDialog(
            onDismissRequest = { pendingUninstall = null },
            title = { Text("卸载应用") },
            text = { Text("在电视上卸载「${pendingApp.name}」？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.uninstallApp(pendingApp.pkg)
                    pendingUninstall = null
                }) { Text("卸载", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingUninstall = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun AppRow(
    app: TvApp,
    icon: ImageBitmap?,
    onClick: () -> Unit,
) {
    SettingsItem(
        headline = app.name.ifBlank { app.pkg },
        supporting = buildList {
            if (app.system) add("系统")
            if (app.size > 0) add(formatSize(app.size))
        }.joinToString(" · ").ifBlank { null },
        leading = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                if (icon != null) {
                    Image(icon, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(
                        Icons.Outlined.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        },
        onClick = onClick,
    )
}

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
    return String.format(Locale.US, "%.2f GB", mb / 1024.0)
}
