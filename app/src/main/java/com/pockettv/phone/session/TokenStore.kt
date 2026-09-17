package com.pockettv.phone.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/** 按「主机:端口」加密保存配对 token。 */
class TokenStore(context: Context) {
    private val prefs: SharedPreferences = open(context.applicationContext)

    fun get(host: String, port: Int): String {
        return prefs.getString(key(host, port), "") ?: ""
    }

    fun put(host: String, port: Int, token: String) {
        prefs.edit().putString(key(host, port), token).apply()
    }

    private fun key(host: String, port: Int): String = "$host:$port"

    private fun open(context: Context): SharedPreferences {
        val master = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "pockettv_tokens_enc",
            master,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
}
