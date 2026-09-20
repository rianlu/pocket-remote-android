package com.pocketremote.net

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.pocketremote.protocol.TvFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** HTTP 上传到电视 /transfer/upload。 */
class TransferClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun upload(
        context: Context,
        host: String,
        port: Int,
        token: String,
        uri: Uri,
        dir: String,
        onProgress: (Float) -> Unit = {},
    ): Result<Unit> {
        if (!LanHosts.allowed(host)) return Result.failure(IllegalArgumentException("只允许局域网地址"))
        return try {
            val name = queryName(context, uri) ?: "file.bin"
            val size = querySize(context, uri)
            val body = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()
                override fun contentLength() = if (size > 0) size else -1L
                override fun writeTo(sink: BufferedSink) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val buf = ByteArray(16 * 1024)
                        var written = 0L
                        while (true) {
                            val n = input.read(buf)
                            if (n < 0) break
                            sink.write(buf, 0, n)
                            written += n
                            if (size > 0) {
                                onProgress((written.toFloat() / size.toFloat()).coerceIn(0f, 1f))
                            }
                        }
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

    fun listFiles(host: String, port: Int, token: String): Result<List<TvFile>> {
        if (!LanHosts.allowed(host)) return Result.failure(IllegalArgumentException("只允许局域网地址"))
        return try {
            val url = "http://$host:$port/transfer/list"
            val req = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return Result.failure(IllegalStateException("HTTP ${resp.code}"))
                }
                val body = resp.body?.string() ?: return Result.failure(IllegalStateException("empty"))
                val arr = JSONObject(body).optJSONArray("files") ?: return Result.success(emptyList())
                val out = ArrayList<TvFile>(arr.length())
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val name = o.optString("name")
                    if (name.isBlank()) continue
                    out.add(
                        TvFile(
                            name = name,
                            dir = o.optString("dir"),
                            size = o.optLong("size"),
                            mtime = o.optLong("mtime"),
                            path = o.optString("path"),
                        ),
                    )
                }
                Result.success(out)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBytes(host: String, port: Int, token: String, pathAndQuery: String): ByteArray? {
        if (!LanHosts.allowed(host)) return null
        return try {
            val url = "http://$host:$port$pathAndQuery"
            val req = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) null else resp.body?.bytes()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun downloadTo(
        host: String,
        port: Int,
        token: String,
        pathAndQuery: String,
        dest: java.io.OutputStream,
    ): Result<Unit> {
        if (!LanHosts.allowed(host)) return Result.failure(IllegalArgumentException("只允许局域网地址"))
        return try {
            val url = "http://$host:$port$pathAndQuery"
            val req = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return Result.failure(IllegalStateException("HTTP ${resp.code}"))
                }
                resp.body?.byteStream()?.use { input ->
                    val buf = ByteArray(16 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        dest.write(buf, 0, n)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return uri.lastPathSegment
        cursor.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && it.moveToFirst()) {
                return it.getString(idx)
            }
        }
        return uri.lastPathSegment
    }

    private fun querySize(context: Context, uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return -1L
        cursor.use {
            val idx = it.getColumnIndex(OpenableColumns.SIZE)
            if (idx >= 0 && it.moveToFirst()) {
                return it.getLong(idx)
            }
        }
        return -1L
    }
}
