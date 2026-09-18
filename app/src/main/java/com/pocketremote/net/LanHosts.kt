package com.pocketremote.net

/** 仅允许 RFC1918 / 链路本地 / localhost，与规格「明文只走内网」一致。 */
object LanHosts {
    fun allowed(host: String): Boolean {
        val h = host.trim().lowercase()
            .removePrefix("[")
            .removeSuffix("]")
        if (h.isEmpty()) return false
        if (h == "localhost" || h == "::1" || h.endsWith(".local")) return true
        val v4 = h.split('.')
        if (v4.size == 4) {
            val a = v4[0].toIntOrNull() ?: return false
            val b = v4[1].toIntOrNull() ?: return false
            if (v4[2].toIntOrNull() == null || v4[3].toIntOrNull() == null) return false
            return a == 10 ||
                a == 127 ||
                (a == 192 && b == 168) ||
                (a == 172 && b in 16..31) ||
                (a == 169 && b == 254)
        }
        if (h.contains(':')) {
            return h.startsWith("fe80:") || h.startsWith("fc") || h.startsWith("fd")
        }
        return false
    }
}
