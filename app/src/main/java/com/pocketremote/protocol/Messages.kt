package com.pocketremote.protocol

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

    fun hello(
        token: String = "",
        pin: String = "",
        phoneName: String,
        inject: String = "",
    ): Pair<String, String> {
        val p = JSONObject()
            .put("token", token)
            .put("pin", pin)
            .put("phoneName", phoneName)
        if (inject == Constants.INJECT_PLUGIN || inject == Constants.INJECT_ADB) {
            p.put("inject", inject)
        }
        return envelope(Constants.TYPE_HELLO, p)
    }

    fun key(code: Int): Pair<String, String> {
        val p = JSONObject().put("action", "click").put("code", code)
        return envelope(Constants.TYPE_KEY, p)
    }

    fun pointer(action: String, dx: Int = 0, dy: Int = 0): Pair<String, String> {
        val p = JSONObject().put("action", action).put("dx", dx).put("dy", dy)
        return envelope(Constants.TYPE_POINTER, p)
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

    fun info(): Pair<String, String> = envelope(Constants.TYPE_INFO)

    fun clean(): Pair<String, String> = envelope(Constants.TYPE_CLEAN)

    fun parseInfo(payload: JSONObject): TvInfo {
        return TvInfo(
            tvName = payload.optString("tvName"),
            manufacturer = payload.optString("manufacturer"),
            brand = payload.optString("brand"),
            android = payload.optString("android"),
            sdk = payload.optInt("sdk"),
            abi = payload.optString("abi"),
            soc = payload.optString("soc"),
            cpu = payload.optString("cpu"),
            ip = payload.optString("ip"),
            mac = payload.optString("mac"),
            firmware = payload.optString("firmware"),
            hardware = payload.optString("hardware"),
            width = payload.optInt("width"),
            height = payload.optInt("height"),
            density = payload.optInt("density"),
            ramMb = payload.optLong("ramMb"),
            ramAvailMb = payload.optLong("ramAvailMb"),
            storageFreeMb = payload.optLong("storageFreeMb"),
            storageTotalMb = payload.optLong("storageTotalMb"),
            injectMode = payload.optString("injectMode"),
            injectOk = payload.optBoolean("injectOk", true),
            helper = payload.optString("helper"),
            cacheMb = payload.optLong("cacheMb"),
        )
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
                    size = item.optLong("size"),
                    extractable = item.optBoolean("extractable"),
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
    val size: Long = 0,
    val extractable: Boolean = false,
)

data class TvInfo(
    val tvName: String = "",
    val manufacturer: String = "",
    val brand: String = "",
    val android: String = "",
    val sdk: Int = 0,
    val abi: String = "",
    val soc: String = "",
    val cpu: String = "",
    val ip: String = "",
    val mac: String = "",
    val firmware: String = "",
    val hardware: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val density: Int = 0,
    val ramMb: Long = 0,
    val ramAvailMb: Long = 0,
    val storageFreeMb: Long = 0,
    val storageTotalMb: Long = 0,
    val injectMode: String = "",
    val injectOk: Boolean = true,
    val helper: String = "",
    val cacheMb: Long = 0,
)

data class TvDevice(
    val host: String,
    val port: Int = Constants.CONTROL_PORT,
    val name: String,
    val id: String = "",
    val sdk: Int = 0,
)
