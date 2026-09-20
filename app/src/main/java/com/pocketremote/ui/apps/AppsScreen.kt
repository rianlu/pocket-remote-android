package com.pocketremote.ui.apps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pocketremote.protocol.TvApp
import com.pocketremote.protocol.TvFile
import com.pocketremote.ui.PhoneViewModel
import com.pocketremote.ui.UiState
import com.pocketremote.ui.components.CapsuleButton
import com.pocketremote.ui.components.CapsuleTone
import com.pocketremote.ui.components.SettingsItem
import java.util.Locale

/** 已安装应用与电视上的安装包，分两个子页。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(state: UiState, viewModel: PhoneViewModel) {
    var pane by rememberSaveable { mutableStateOf(0) }
    var appQuery by rememberSaveable { mutableStateOf("") }
    var apkQuery by rememberSaveable { mutableStateOf("") }
    var appFilter by rememberSaveable { mutableStateOf("all") }
    var sheetPkg by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingUninstall by rememberSaveable { mutableStateOf<String?>(null) }
    var extractPkg by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingApk by rememberSaveable { mutableStateOf<String?>(null) }
    val pickApk = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.upload(uri, asApk = true)
    }
    val saveApk = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.android.package-archive"),
    ) { uri ->
        val pkg = extractPkg
        extractPkg = null
        if (uri != null && pkg != null) viewModel.extractApk(pkg, uri)
    }
    val filteredApps = state.apps.filter { app ->
        val match = appQuery.isBlank() ||
            app.name.contains(appQuery, ignoreCase = true) ||
            app.pkg.contains(appQuery, ignoreCase = true)
        val type = when (appFilter) {
            "user" -> !app.system
            "system" -> app.system
            else -> true
        }
        match && type
    }
    val filteredApks = state.tvApks.filter {
        apkQuery.isBlank() || it.name.contains(apkQuery, ignoreCase = true) ||
            it.dir.contains(apkQuery, ignoreCase = true)
    }
    val sheetApp = state.apps.find { it.pkg == sheetPkg }
    val pendingApp = state.apps.find { it.pkg == pendingUninstall }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pane, containerColor = MaterialTheme.colorScheme.surface) {
            Tab(
                selected = pane == 0,
                onClick = { pane = 0 },
                text = { Text("已安装") },
            )
            Tab(
                selected = pane == 1,
                onClick = {
                    pane = 1
                    viewModel.loadTvApks()
                },
                text = { Text("安装包") },
            )
        }
        if (pane == 0) {
            InstalledPane(
                query = appQuery,
                onQuery = { appQuery = it },
                filter = appFilter,
                onFilter = { appFilter = it },
                count = if (state.apps.isEmpty()) "正在拉取…" else "${filteredApps.size} 个应用",
                apps = filteredApps,
                iconOf = { viewModel.iconMap[it] },
                onVisible = { viewModel.ensureIcon(it) },
                onApp = { sheetPkg = it },
            )
        } else {
            PackagesPane(
                query = apkQuery,
                onQuery = { apkQuery = it },
                uploading = state.uploadProgress >= 0f,
                progress = state.uploadProgress,
                count = "${filteredApks.size} 个文件",
                files = filteredApks,
                iconOf = { path -> viewModel.iconMap["apk:$path"] },
                onVisible = { viewModel.ensureApkIcon(it) },
                onPickPhone = { pickApk.launch("*/*") },
                onFile = { pendingApk = apkKey(it) },
            )
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
    val pendingFile = pendingApk?.let { key -> state.tvApks.find { apkKey(it) == key } }
    if (pendingFile != null) {
        AlertDialog(
            onDismissRequest = { pendingApk = null },
            title = { Text("安装到电视") },
            text = { Text("在电视上安装「${pendingFile.name}」？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.installTvApk(pendingFile)
                    pendingApk = null
                }) { Text("安装") }
            },
            dismissButton = {
                TextButton(onClick = { pendingApk = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun InstalledPane(
    query: String,
    onQuery: (String) -> Unit,
    filter: String,
    onFilter: (String) -> Unit,
    count: String,
    apps: List<TvApp>,
    iconOf: (String) -> ImageBitmap?,
    onVisible: (String) -> Unit,
    onApp: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        SearchField(query, onQuery, "搜索应用")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == "all", onClick = { onFilter("all") }, label = { Text("全部") })
            FilterChip(selected = filter == "user", onClick = { onFilter("user") }, label = { Text("用户") })
            FilterChip(selected = filter == "system", onClick = { onFilter("system") }, label = { Text("系统") })
        }
        Text(
            count,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        )
        if (apps.isEmpty()) {
            EmptyHint("没有符合条件的应用", Modifier.weight(1f).fillMaxWidth())
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                gridItems(apps, key = { it.pkg }) { app ->
                    AppGridCell(
                        app = app,
                        icon = iconOf(app.pkg),
                        onVisible = onVisible,
                        onClick = { onApp(app.pkg) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PackagesPane(
    query: String,
    onQuery: (String) -> Unit,
    uploading: Boolean,
    progress: Float,
    count: String,
    files: List<TvFile>,
    iconOf: (String) -> ImageBitmap?,
    onVisible: (String) -> Unit,
    onPickPhone: () -> Unit,
    onFile: (TvFile) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        CapsuleButton(
            text = if (uploading) "正在推送 ${(progress * 100).toInt()}%" else "从手机安装",
            onClick = onPickPhone,
            enabled = !uploading,
            modifier = Modifier.fillMaxWidth(),
        )
        if (uploading) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(12.dp))
        SearchField(query, onQuery, "搜索安装包")
        Text(
            count,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        )
        if (files.isEmpty() && !uploading) {
            EmptyHint(
                "电视存储里没有发现 APK\n可从手机推送，或把安装包拷到下载目录",
                Modifier.weight(1f).fillMaxWidth(),
            )
        } else {
            LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                items(files, key = { apkKey(it) }) { file ->
                    ApkFileRow(
                        file = file,
                        icon = iconOf(file.path),
                        onVisible = onVisible,
                        onClick = { onFile(file) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ApkFileRow(
    file: TvFile,
    icon: ImageBitmap?,
    onVisible: (String) -> Unit,
    onClick: () -> Unit,
) {
    LaunchedEffect(file.path) {
        if (file.path.isNotBlank()) onVisible(file.path)
    }
    SettingsItem(
        headline = file.name,
        supporting = apkFolder(file).ifBlank { null },
        leading = { ApkIcon(icon) },
        onClick = onClick,
    )
}

@Composable
private fun AppGridCell(
    app: TvApp,
    icon: ImageBitmap?,
    onVisible: (String) -> Unit,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    LaunchedEffect(app.pkg) { onVisible(app.pkg) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onClick()
            }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ApkIcon(icon, size = 56.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            app.name.ifBlank { app.pkg },
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ApkIcon(icon: ImageBitmap?, size: androidx.compose.ui.unit.Dp = 40.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
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
                modifier = Modifier.size(size * 0.45f),
            )
        }
    }
}

private fun apkKey(file: TvFile): String {
    return file.path.ifBlank { file.dir + "/" + file.name }
}

private fun apkFolder(file: TvFile): String {
    val p = file.path.trim()
    if (p.isNotEmpty()) {
        val slash = p.lastIndexOf('/')
        return if (slash > 0) p.substring(0, slash) else p
    }
    return file.dir
}

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
    return String.format(Locale.US, "%.2f GB", mb / 1024.0)
}
