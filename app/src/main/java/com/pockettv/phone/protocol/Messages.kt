package com.pockettv.phone.protocol

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** 编解码 PROTOCOL 规定的 JSON 帧。 */
object Messages {
    fun envelope(type: String, payload: JSONObject = JSONObject(), id: String = UUID.randomUUID().toString()): Pair<String, String> {
        val obj = JSONObject()
            .put("v", Constants.PROTOCOL_V)
            .put("id", id)
            .put("type", type)
            .put("payload", payload)
        return id to obj.toString()
    }

    fun hello(token: String = "", pin: String = "", phoneName: String): Pair<String, String> {
        val p = JSONObject()
            .put("token", token)
            .put("pin", pin)
            .put("phoneName", phoneName)
        return envelope(Constants.TYPE_HELLO, p)
    }

    fun key(code: Int): Pair<String, String> {
        val p = JSONObject().put("action", "click").put("code", code)
        return envelope(Constants.TYPE_KEY, p)
    }

    fun text(value: String): Pair<String, String> {
        return envelope(Constants.TYPE_TEXT, JSONObject().put("text", value))
    }

    fun apps(): Pair<String, String> = envelope(Constants.TYPE_APPS)

    fun appOpen(pkg: String): Pair<String, String> {
        return envelope(Constants.TYPE_APP_OPEN, JSONObject().put("pkg", pkg))
    }

    fun appUninstall(pkg: String): Pair<String, String> {
        return envelope(Constants.TYPE_APP_UNINSTALL, JSONObject().put("pkg", pkg))
    }

    fun parse(raw: String): Frame? {
        return try {
            val o = JSONObject(raw)
            Frame(
                v = o.optInt("v", 0),
                id = o.optString("id", ""),
                type = o.optString("type", ""),
                payload = o.optJSONObject("payload") ?: JSONObject(),
            )
        } catch (_: Exception) {
            null
        }
    }

    fun parseApps(payload: JSONObject): List<TvApp> {
        val arr: JSONArray = payload.optJSONArray("apps") ?: return emptyList()
        val out = ArrayList<TvApp>(arr.length())
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            out.add(
                TvApp(
                    name = item.optString("name"),
                    pkg = item.optString("pkg"),
                    system = item.optBoolean("system"),
                ),
            )
        }
        return out
    }
}

data class Frame(
    val v: Int,
    val id: String,
    val type: String,
    val payload: JSONObject,
)

data class TvApp(
    val name: String,
    val pkg: String,
    val system: Boolean,
)

data class TvDevice(
    val host: String,
    val port: Int = Constants.CONTROL_PORT,
    val name: String,
    val id: String = "",
    val sdk: Int = 0,
)
