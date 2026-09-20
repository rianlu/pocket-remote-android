package com.pocketremote.net

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pocketremote.protocol.Constants
import com.pocketremote.protocol.Frame
import com.pocketremote.protocol.Messages
import com.pocketremote.protocol.TvApp
import com.pocketremote.net.LanHosts
import com.pocketremote.protocol.TvDevice
import com.pocketremote.protocol.TvInfo
import com.pocketremote.session.TokenStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/** 维持到电视助手的 WebSocket，并完成 hello / PIN 握手。 */
class ConnectionManager(
    private val tokens: TokenStore,
) {
    interface Callback {
        fun onNeedPin()
        fun onReady(tvName: String, sdk: Int, injectOk: Boolean, injectMode: String)
        fun onError(message: String)
        fun onApps(apps: List<TvApp>)
        fun onInfo(info: TvInfo)
        fun onClean(killed: Int, ramAvailMb: Long)
        fun onClosed()
    }

    private val tag = "PocketRemoteConn"
    private val main = Handler(Looper.getMainLooper())
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var socket: WebSocket? = null
    @Volatile private var userDisconnect = false
    private var failCount = 0
    private var backoffMs = 800L
    private val reconnect = Runnable {
        if (userDisconnect || host.isEmpty()) return@Runnable
        connect(TvDevice(host = host, port = port, name = tvName.ifBlank { host }), injectPref)
    }
    var host: String = ""
        private set
    var port: Int = Constants.CONTROL_PORT
        private set
    var token: String = ""
        private set
    var tvSdk: Int = 0
        private set
    var injectOk: Boolean = true
        private set
    var injectMode: String = ""
        private set
    var injectPref: String = Constants.INJECT_PLUGIN
        private set
    var tvName: String = ""
        private set

    @Volatile
    var callback: Callback? = null

    fun connect(device: TvDevice, inject: String = Constants.INJECT_PLUGIN) {
        if (!LanHosts.allowed(device.host)) {
            main.post { callback?.onError("只允许连接局域网地址") }
            return
        }
        userDisconnect = false
        main.removeCallbacks(reconnect)
        closeSocket()
        injectPref = if (inject == Constants.INJECT_ADB) Constants.INJECT_ADB else Constants.INJECT_PLUGIN
        host = device.host
        port = if (device.port > 0) device.port else Constants.CONTROL_PORT
        token = tokens.get(host, port)
        val url = "ws://$host:$port${Constants.WS_PATH}"
        val req = Request.Builder().url(url).build()
        socket = client.newWebSocket(req, listener)
    }

    fun submitPin(pin: String) {
        sendHello(pin = pin)
    }

    fun sendKey(code: Int) {
        val (_, json) = Messages.key(code)
        socket?.send(json)
    }

    fun sendPointer(action: String, dx: Int = 0, dy: Int = 0) {
        val (_, json) = Messages.pointer(action, dx, dy)
        socket?.send(json)
    }

    fun sendText(text: String) {
        val (_, json) = Messages.text(text)
        socket?.send(json)
    }

    fun requestApps() {
        val (_, json) = Messages.apps()
        socket?.send(json)
    }

    fun openApp(pkg: String) {
        val (_, json) = Messages.appOpen(pkg)
        socket?.send(json)
    }

    fun uninstallApp(pkg: String) {
        val (_, json) = Messages.appUninstall(pkg)
        socket?.send(json)
    }

    fun installApk(name: String, path: String = "") {
        val (_, json) = Messages.apkInstall(name, path)
        socket?.send(json)
    }

    fun requestInfo() {
        val (_, json) = Messages.info()
        socket?.send(json)
    }

    fun requestClean() {
        val (_, json) = Messages.clean()
        socket?.send(json)
    }

    fun disconnect() {
        userDisconnect = true
        main.removeCallbacks(reconnect)
        closeSocket()
        host = ""
    }

    private fun closeSocket() {
        val old = socket
        socket = null
        try {
            old?.cancel()
        } catch (_: Exception) {
        }
        try {
            old?.close(1000, "bye")
        } catch (_: Exception) {
        }
    }

    private fun scheduleReconnect(why: String?) {
        if (userDisconnect || host.isEmpty()) {
            main.post { callback?.onClosed() }
            return
        }
        failCount++
        if (failCount > 6) {
            main.post { callback?.onClosed() }
            return
        }
        val wait = backoffMs
        backoffMs = (backoffMs * 2).coerceAtMost(12_000L)
        val hint = why?.takeIf { it.isNotBlank() } ?: "连接中断"
        main.post { callback?.onError("$hint，正在重连…") }
        main.postDelayed(reconnect, wait)
    }

    private fun sendHello(pin: String = "") {
        val name = Build.MODEL ?: "android"
        val (_, json) = Messages.hello(token = token, pin = pin, phoneName = name, inject = injectPref)
        socket?.send(json)
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            failCount = 0
            backoffMs = 800L
            sendHello()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val frame = Messages.parse(text) ?: return
            main.post { dispatch(frame) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (webSocket != socket) return
            Log.w(tag, "ws fail", t)
            scheduleReconnect(t.message)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (webSocket != socket) return
            if (userDisconnect) {
                main.post { callback?.onClosed() }
            } else {
                scheduleReconnect(reason)
            }
        }
    }

    private fun dispatch(frame: Frame) {
        when (frame.type) {
            Constants.TYPE_NEED_PIN -> callback?.onNeedPin()
            Constants.TYPE_HELLO_OK -> {
                token = frame.payload.optString("token", token)
                tvName = frame.payload.optString("tvName", host)
                tvSdk = frame.payload.optInt("sdk", 0)
                injectOk = frame.payload.optBoolean("injectOk", true)
                injectMode = frame.payload.optString("injectMode", "")
                tokens.put(host, port, token)
                callback?.onReady(tvName, tvSdk, injectOk, injectMode)
            }
            Constants.TYPE_ERROR -> {
                val code = frame.payload.optString("code")
                val msg = frame.payload.optString("message")
                callback?.onError("$code $msg".trim())
            }
            Constants.TYPE_APPS_OK -> callback?.onApps(Messages.parseApps(frame.payload))
            Constants.TYPE_INFO_OK -> callback?.onInfo(Messages.parseInfo(frame.payload))
            Constants.TYPE_CLEAN_OK -> callback?.onClean(
                frame.payload.optInt("killed"),
                frame.payload.optLong("ramAvailMb"),
            )
        }
    }
}
