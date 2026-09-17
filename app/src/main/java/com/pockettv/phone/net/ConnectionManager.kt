package com.pockettv.phone.net

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pockettv.phone.protocol.Constants
import com.pockettv.phone.protocol.Frame
import com.pockettv.phone.protocol.Messages
import com.pockettv.phone.protocol.TvApp
import com.pockettv.phone.protocol.TvDevice
import com.pockettv.phone.session.TokenStore
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
        fun onReady(tvName: String, sdk: Int, injectOk: Boolean)
        fun onError(message: String)
        fun onApps(apps: List<TvApp>)
        fun onClosed()
    }

    private val tag = "PocketTvConn"
    private val main = Handler(Looper.getMainLooper())
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var socket: WebSocket? = null
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
    var tvName: String = ""
        private set

    @Volatile
    var callback: Callback? = null

    fun connect(device: TvDevice) {
        disconnect()
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

    fun disconnect() {
        socket?.close(1000, "bye")
        socket = null
    }

    private fun sendHello(pin: String = "") {
        val name = Build.MODEL ?: "android"
        val (_, json) = Messages.hello(token = token, pin = pin, phoneName = name)
        socket?.send(json)
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            sendHello()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val frame = Messages.parse(text) ?: return
            main.post { dispatch(frame) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(tag, "ws fail", t)
            main.post { callback?.onError(t.message ?: "连接失败") }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            main.post { callback?.onClosed() }
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
                tokens.put(host, port, token)
                callback?.onReady(tvName, tvSdk, injectOk)
            }
            Constants.TYPE_ERROR -> {
                val code = frame.payload.optString("code")
                val msg = frame.payload.optString("message")
                callback?.onError("$code $msg".trim())
            }
            Constants.TYPE_APPS_OK -> callback?.onApps(Messages.parseApps(frame.payload))
        }
    }
}
