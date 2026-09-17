package com.pockettv.phone.ui

import android.app.Application
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.pockettv.phone.net.ConnectionManager
import com.pockettv.phone.net.Discovery
import com.pockettv.phone.net.TransferClient
import com.pockettv.phone.protocol.Constants
import com.pockettv.phone.protocol.TvApp
import com.pockettv.phone.protocol.TvDevice
import com.pockettv.phone.session.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** 设备发现、握手与遥控指令的界面状态。 */
class PhoneViewModel(app: Application) : AndroidViewModel(app), ConnectionManager.Callback, Discovery.Listener {
    private val tokens = TokenStore(app)
    private val connection = ConnectionManager(tokens)
    private val discovery = Discovery(app)
    private val transfer = TransferClient()
    private var lastKeyAt = 0L

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init {
        connection.callback = this
    }

    fun scan() {
        _ui.update { it.copy(scanning = true, devices = emptyList(), message = "正在搜索…") }
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
        connection.connect(device)
    }

    fun submitPin(pin: String) {
        connection.submitPin(pin.trim())
    }

    fun sendKey(code: Int) {
        val now = System.currentTimeMillis()
        if (now - lastKeyAt < 80) return
        lastKeyAt = now
        connection.sendKey(code)
        if (!connection.injectOk) {
            _ui.update { it.copy(message = "电视端 injectOk=false，按键可能无效") }
        }
    }

    fun sendText(text: String) {
        if (connection.tvSdk in 1 until 21 && text.any { it.code > 127 }) {
            _ui.update { it.copy(message = "当前电视系统过旧，中文输入可能无效") }
        }
        connection.sendText(text)
    }

    fun loadApps() {
        connection.requestApps()
    }

    fun openApp(pkg: String) {
        connection.openApp(pkg)
    }

    fun uninstallApp(pkg: String) {
        connection.uninstallApp(pkg)
    }

    fun upload(uri: Uri, asApk: Boolean) {
        val host = connection.host
        val port = connection.port
        val token = connection.token
        if (host.isEmpty() || token.isEmpty()) {
            _ui.update { it.copy(message = "尚未连接") }
            return
        }
        _ui.update { it.copy(message = "正在上传…") }
        viewModelScope.launch(Dispatchers.IO) {
            val dir = if (asApk) Constants.DIR_APK else Constants.DIR_INBOX
            val result = transfer.upload(getApplication(), host, port, token, uri, dir)
            _ui.update {
                it.copy(
                    message = result.fold(
                        onSuccess = { if (asApk) "已上传，电视将调起安装" else "上传完成" },
                        onFailure = { e -> "上传失败: ${e.message}" },
                    ),
                )
            }
        }
    }

    fun go(route: Route) {
        _ui.update { it.copy(route = route) }
        if (route == Route.Apps) loadApps()
    }

    fun disconnectToList() {
        connection.disconnect()
        _ui.update {
            it.copy(route = Route.Devices, connecting = false, apps = emptyList(), message = "")
        }
        scan()
    }

    override fun onCleared() {
        discovery.stop()
        connection.disconnect()
        connection.callback = null
        super.onCleared()
    }

    override fun onDevice(device: TvDevice) {
        _ui.update { state ->
            val list = state.devices.toMutableList()
            if (list.none { it.host == device.host && it.port == device.port }) {
                list.add(device)
            }
            state.copy(devices = list, message = "发现 ${list.size} 台设备")
        }
    }

    override fun onFinished() {
        _ui.update { it.copy(scanning = false) }
    }

    override fun onNeedPin() {
        _ui.update { it.copy(connecting = false, route = Route.Pin, message = "请输入电视上的配对码") }
    }

    override fun onReady(tvName: String, sdk: Int, injectOk: Boolean) {
        val extra = if (!injectOk) "（injectOk=false，按键可能无效）" else ""
        _ui.update {
            it.copy(
                connecting = false,
                route = Route.Remote,
                message = "已连接 $tvName$extra",
                tvName = tvName,
                tvSdk = sdk,
                injectOk = injectOk,
            )
        }
    }

    override fun onError(message: String) {
        _ui.update { it.copy(connecting = false, message = message) }
    }

    override fun onApps(apps: List<TvApp>) {
        _ui.update { it.copy(apps = apps) }
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

enum class Route { Devices, Pin, Remote, Keyboard, Files, Apps }

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
    val message: String = "",
)
