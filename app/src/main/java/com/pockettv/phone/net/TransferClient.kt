package com.pockettv.phone.net

import android.content.Context
import android.net.Uri
import com.pockettv.phone.protocol.Constants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.util.concurrent.TimeUnit

/** HTTP 上传到电视 /transfer/upload。 */
class TransferClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun upload(context: Context, host: String, port: Int, token: String, uri: Uri, dir: String): Result<Unit> {
        return try {
            val name = queryName(context, uri) ?: "file.bin"
            val body = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()
                override fun writeTo(sink: BufferedSink) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        sink.writeAll(input.source())
                    } ?: error("无法读取文件")
                }
            }
            val url = "http://$host:$port/transfer/upload?dir=$dir&name=${java.net.URLEncoder.encode(name, "UTF-8")}"
            val req = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .put(body)
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) Result.success(Unit)
                else Result.failure(IllegalStateException("HTTP ${resp.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return uri.lastPathSegment
        cursor.use {
            val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && it.moveToFirst()) {
                return it.getString(idx)
            }
        }
        return uri.lastPathSegment
    }
}
