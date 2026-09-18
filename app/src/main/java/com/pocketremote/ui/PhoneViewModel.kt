package com.pocketremote.ui

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.pocketremote.net.ConnectionManager
import com.pocketremote.net.Discovery
import com.pocketremote.net.LanHosts
import com.pocketremote.net.TransferClient
import com.pocketremote.protocol.Constants
import com.pocketremote.protocol.TvApp
import com.pocketremote.protocol.TvDevice
import com.pocketremote.protocol.TvInfo
import com.pocketremote.session.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** 设备发现、握手与遥控指令的界面状态。 */
class PhoneViewModel(app: Application) : AndroidViewModel(app), ConnectionManager.Callback, Discovery.Listener {
    private val tokens = TokenStore(app)
    private val connection = ConnectionManager(tokens)
    private val discovery = Discovery(app)
    private val transfer = TransferClient()
    private val prefs = app.getSharedPreferences("pocketremote_phone", Context.MODE_PRIVATE)
    private val main = Handler(Looper.getMainLooper())
    private var lastKeyAt = 0L
    private var lastPointerAt = 0L
    private var pendingKey = 0
    private var pendingPointerDx = 0
    private var pendingPointerDy = 0
    private val flushKey = Runnable {
        val code = pendingKey
        pendingKey = 0
        if (code != 0) {
            lastKeyAt = System.currentTimeMillis()
            connection.sendKey(code)
        }
    }

    private val _ui = MutableStateFlow(UiState(injectPref = loadInjectPref()))
    val ui: StateFlow<UiState> = _ui

    init {
        connection.callback = this
    }

    /** 仅连接页可改；写入 hello.inject。 */
    fun setInjectPref(mode: String) {
        if (mode != Constants.INJECT_PLUGIN && mode != Constants.INJECT_ADB) return
        prefs.edit().putString("inject_pref", mode).apply()
        _ui.update { it.copy(injectPref = mode) }
    }

    private fun loadInjectPref(): String {
        val raw = prefs.getString("inject_pref", Constants.INJECT_PLUGIN) ?: Constants.INJECT_PLUGIN
        return if (raw == Constants.INJECT_ADB) Constants.INJECT_ADB else Constants.INJECT_PLUGIN
    }

    fun scan() {
        _ui.update { it.copy(scanning = true, message = "正在搜索…") }
        discovery.start(this)
    }

    fun stopScan() {
        discovery.stop()
        _ui.update { it.copy(scanning = false) }
    }

    fun connectManual(host: String, portText: String) {
        val port = portText.toIntOrNull() ?: Constants.CONTROL_PORT
        connect(TvDevice(host = host.trim(), port = port, name = host.trim()))
    }

    fun connect(device: TvDevice) {
        if (_ui.value.connecting) return
        if (!LanHosts.allowed(device.host)) {
            _ui.update { it.copy(message = "只允许连接局域网地址") }
            return
        }
        discovery.stop()
        _ui.update {
            it.copy(
                scanning = false,
                connecting = true,
                selected = device,
                message = "正在连接 ${device.host}…",
                route = Route.Devices,
            )
        }
        connection.connect(device, _ui.value.injectPref)
    }

    fun submitPin(pin: String) {
        connection.submitPin(pin.trim())
    }

    fun sendKey(code: Int, minIntervalMs: Long = 80L) {
        val now = System.currentTimeMillis()
        val wait = lastKeyAt + minIntervalMs - now
        if (wait > 0) {
            pendingKey = code
            main.removeCallbacks(flushKey)
            main.postDelayed(flushKey, wait)
            return
        }
        pendingKey = 0
        main.removeCallbacks(flushKey)
        lastKeyAt = now
        connection.sendKey(code)
        if (!connection.injectOk) {
            _ui.update { it.copy(message = "按键注入不可用，请在电视上打开网络调试") }
        }
    }

    fun sendText(text: String) {
        connection.sendText(text)
    }

    fun sendPointerMove(dx: Int, dy: Int) {
        if (connection.injectMode != Constants.INJECT_PLUGIN) return
        pendingPointerDx += dx
        pendingPointerDy += dy
        if (pendingPointerDx == 0 && pendingPointerDy == 0) return
        val now = System.currentTimeMillis()
        if (now - lastPointerAt < 16) return
        lastPointerAt = now
        connection.sendPointer("move", pendingPointerDx, pendingPointerDy)
        pendingPointerDx = 0
        pendingPointerDy = 0
    }

    fun sendPointerClick() {
        if (connection.injectMode != Constants.INJECT_PLUGIN) {
            _ui.update { it.copy(message = "鼠标仅增强模式可用") }
            return
        }
        if (pendingPointerDx != 0 || pendingPointerDy != 0) {
            connection.sendPointer("move", pendingPointerDx, pendingPointerDy)
            pendingPointerDx = 0
            pendingPointerDy = 0
        }
        connection.sendPointer("click")
    }

    fun flushPointer() {
        if (connection.injectMode != Constants.INJECT_PLUGIN) return
        if (pendingPointerDx == 0 && pendingPointerDy == 0) return
        connection.sendPointer("move", pendingPointerDx, pendingPointerDy)
        pendingPointerDx = 0
        pendingPointerDy = 0
        lastPointerAt = System.currentTimeMillis()
    }

    fun note(msg: String) {
        _ui.update { it.copy(message = msg) }
    }

    fun consumeMessage() {
        if (_ui.value.message.isNotEmpty()) {
            _ui.update { it.copy(message = "") }
        }
    }

    fun loadApps() {
        connection.requestApps()
    }

    fun openApp(pkg: String) {
        connection.openApp(pkg)
        _ui.update { it.copy(message = "已在电视上打开") }
    }

    fun uninstallApp(pkg: String) {
        connection.uninstallApp(pkg)
        _ui.update { it.copy(message = "请在电视上确认卸载") }
    }

    private val iconCache = java.util.concurrent.ConcurrentHashMap<String, ByteArray>()
    private val inflightIcons = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    val iconMap: SnapshotStateMap<String, ImageBitmap> = mutableStateMapOf()

    fun ensureIcon(pkg: String) {
        if (iconMap.containsKey(pkg) || !inflightIcons.add(pkg)) return
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = iconPng(pkg)
            val bmp = if (bytes != null) BitmapFactory.decodeByteArray(bytes, 0, bytes.size) else null
            withContext(Dispatchers.Main) {
                if (bmp != null) iconMap[pkg] = bmp.asImageBitmap()
            }
        }
    }

    fun requestClean() {
        _ui.update { it.copy(message = "正在结束后台应用…") }
        connection.requestClean()
    }

    fun iconPng(pkg: String): ByteArray? {
        iconCache[pkg]?.let { return it }
        val host = connection.host
        val token = connection.token
        if (host.isEmpty() || token.isEmpty()) return null
        val bytes = transfer.getBytes(
            host,
            connection.port,
            token,
            "/apps/icon?pkg=${java.net.URLEncoder.encode(pkg, "UTF-8")}",
        )
        if (bytes != null) iconCache[pkg] = bytes
        return bytes
    }

    fun extractApk(pkg: String, dest: Uri) {
        val host = connection.host
        val token = connection.token
        if (host.isEmpty() || token.isEmpty()) {
            _ui.update { it.copy(message = "尚未连接") }
            return
        }
        _ui.update { it.copy(message = "正在提取 APK…") }
        viewModelScope.launch(Dispatchers.IO) {
            val out = getApplication<Application>().contentResolver.openOutputStream(dest)
            if (out == null) {
                _ui.update { it.copy(message = "无法写入文件") }
                return@launch
            }
            val result = out.use {
                transfer.downloadTo(
                    host,
                    connection.port,
                    token,
                    "/apps/apk?pkg=${java.net.URLEncoder.encode(pkg, "UTF-8")}",
                    it,
                )
            }
            _ui.update {
                it.copy(
                    message = result.fold(
                        onSuccess = { "APK 已保存到手机" },
                        onFailure = { e -> "无法提取：系统应用或分体包不支持" },
                    ),
                )
            }
        }
    }

    fun upload(uri: Uri, asApk: Boolean) {
        val host = connection.host
        val port = connection.port
        val token = connection.token
        if (host.isEmpty() || token.isEmpty()) {
            _ui.update { it.copy(message = "尚未连接", uploadProgress = -1f) }
            return
        }
        _ui.update { it.copy(message = "正在上传…", uploadProgress = 0f) }
        viewModelScope.launch(Dispatchers.IO) {
            val dir = if (asApk) Constants.DIR_APK else Constants.DIR_INBOX
            val result = transfer.upload(getApplication(), host, port, token, uri, dir) { p ->
                _ui.update { it.copy(uploadProgress = p, message = "正在上传 ${(p * 100).toInt()}%") }
            }
            _ui.update {
                it.copy(
                    uploadProgress = if (result.isSuccess) 1f else -1f,
                    message = result.fold(
                        onSuccess = { if (asApk) "已上传，请在电视上确认安装" else "上传完成，文件在电视 PocketRemote/inbox" },
                        onFailure = { e -> "上传失败: ${e.message}" },
                    ),
                )
            }
        }
    }

    fun go(route: Route) {
        val cur = _ui.value
        if (cur.route == route) return
        _ui.update { it.copy(route = route) }
        if (route == Route.Apps && cur.apps.isEmpty()) loadApps()
        if ((route == Route.Settings || route == Route.Info) && cur.tvInfo == null) {
            connection.requestInfo()
        }
    }

    fun refreshInfo() {
        connection.requestInfo()
    }

    fun reconnect() {
        val device = _ui.value.selected ?: return
        connect(device)
    }

    fun disconnectToList() {
        connection.disconnect()
        _ui.update {
            it.copy(route = Route.Devices, connecting = false, apps = emptyList(), tvInfo = null, message = "")
        }
        scan()
    }

    override fun onCleared() {
        main.removeCallbacks(flushKey)
        discovery.stop()
        connection.disconnect()
        connection.callback = null
        super.onCleared()
    }

    override fun onDevice(device: TvDevice) {
        _ui.update { state ->
            val list = state.devices.toMutableList()
            val idx = list.indexOfFirst { it.host == device.host && it.port == device.port }
            if (idx < 0) {
                list.add(device)
            } else if (betterName(device.name, list[idx].name)) {
                list[idx] = list[idx].copy(name = device.name)
            }
            state.copy(devices = list, message = "发现 ${list.size} 台设备")
        }
    }

    private fun betterName(incoming: String, current: String): Boolean {
        if (incoming.isBlank() || incoming == current) return false
        if (current.isBlank() || current == incoming) return true
        if (current == Constants.NSD_NAME) return incoming != Constants.NSD_NAME
        return false
    }

    override fun onFinished() {
        _ui.update { it.copy(scanning = false) }
    }

    override fun onNeedPin() {
        _ui.update { it.copy(connecting = false, route = Route.Pin, message = "请输入电视上的配对码") }
    }

    override fun onReady(tvName: String, sdk: Int, injectOk: Boolean, injectMode: String) {
        val extra = if (!injectOk) "按键不可用，请在电视上打开网络调试" else ""
        _ui.update {
            it.copy(
                connecting = false,
                route = Route.Remote,
                message = extra,
                injectMode = injectMode,
                tvName = tvName,
                tvSdk = sdk,
                injectOk = injectOk,
            )
        }
        connection.requestInfo()
        connection.requestApps()
    }

    override fun onInfo(info: TvInfo) {
        _ui.update {
            it.copy(
                tvInfo = info,
                tvName = info.tvName.ifBlank { it.tvName },
                tvSdk = if (info.sdk > 0) info.sdk else it.tvSdk,
                injectMode = info.injectMode.ifBlank { it.injectMode },
                injectOk = info.injectOk,
            )
        }
    }

    override fun onError(message: String) {
        _ui.update {
            it.copy(
                connecting = false,
                message = message,
                pinNonce = if (it.route == Route.Pin) it.pinNonce + 1 else it.pinNonce,
            )
        }
    }

    override fun onApps(apps: List<TvApp>) {
        _ui.update { it.copy(apps = apps) }
        apps.take(24).forEach { ensureIcon(it.pkg) }
    }

    override fun onClean(killed: Int, ramAvailMb: Long) {
        _ui.update {
            it.copy(message = "已结束后台 $killed 个，可用内存 ${ramAvailMb} MB")
        }
        connection.requestInfo()
    }

    override fun onClosed() {
        // 遥控页掉线时回到列表
        val route = _ui.value.route
        if (route != Route.Devices && route != Route.Pin) {
            _ui.update { it.copy(message = "连接已断开", route = Route.Devices, connecting = false) }
        }
    }

    fun phoneName(): String {
        return try {
            Settings.Global.getString(getApplication<Application>().contentResolver, "device_name")
                ?: Build.MODEL
        } catch (_: Exception) {
            Build.MODEL
        }
    }
}

enum class Route { Devices, Pin, Remote, Keyboard, Apps, Settings, Info }

data class UiState(
    val route: Route = Route.Devices,
    val scanning: Boolean = false,
    val connecting: Boolean = false,
    val devices: List<TvDevice> = emptyList(),
    val selected: TvDevice? = null,
    val apps: List<TvApp> = emptyList(),
    val tvName: String = "",
    val tvSdk: Int = 0,
    val injectOk: Boolean = true,
    val injectMode: String = "",
    val injectPref: String = Constants.INJECT_PLUGIN,
    val tvInfo: TvInfo? = null,
    val message: String = "",
    val uploadProgress: Float = -1f,
    val pinNonce: Int = 0,
)
