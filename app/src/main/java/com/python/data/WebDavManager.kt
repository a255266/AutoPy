package com.python.data

import android.content.Context
import android.util.Log
import at.bitfire.dav4jvm.DavResource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import at.bitfire.dav4jvm.ResponseCallback
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.IOException
import java.net.URLDecoder


@Singleton
class WebDavManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.webDavDataStore

    private suspend fun loadSettings(): WebDavSettings {
        val prefs = dataStore.data.first()
        return WebDavSettings(
            server = prefs[WebDavKeys.SERVER] ?: "",
            account = prefs[WebDavKeys.ACCOUNT] ?: "",
            password = prefs[WebDavKeys.PASSWORD] ?: ""
        )
    }

    private suspend fun createResource(remotePath: String): DavResource {
        val settings = loadSettings()
        require(settings.server.isNotEmpty() && settings.account.isNotEmpty() && settings.password.isNotEmpty()) {
            "WebDAV settings are incomplete"
        }
        val okHttpClient = OkHttpClient.Builder()
            .followRedirects(false)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", Credentials.basic(settings.account, settings.password))
                    .build()
                chain.proceed(request)
            }
            .build()
        val cleanPath = remotePath.trimStart('/')
        val fullUrl = "${settings.server.trimEnd('/')}/$cleanPath"

        try {
            val url = fullUrl.toHttpUrl()
            return DavResource(okHttpClient, url)
        } catch (e: Exception) {
            Log.e("WebDAV", "createResource error: ${e.message}", e)
            throw e
        }
    }

    private suspend fun ensureDirectoryExists(remotePath: String) {
        val resource = createResource(remotePath)
        suspendCancellableCoroutine<Unit> { cont ->
            resource.mkCol(null) { response ->
                if (response.isSuccessful || response.code == 405) {
                    cont.resume(Unit)
                } else {
                    cont.resumeWithException(IOException("创建目录失败: HTTP ${response.code}"))
                }
            }
        }
    }

    suspend fun upload(remotePath: String, file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val resource = createResource(remotePath)
            val parentPath = remotePath.substringBeforeLast('/')
            if (parentPath.isNotEmpty()) {
                try {
                    ensureDirectoryExists(parentPath)
                } catch (e: Exception) {
                    Log.w("WebDAV", "忽略目录创建错误：${e.message}")
                }
            }
            val contentType = "application/octet-stream".toMediaType()
            val requestBody = file.readBytes().toRequestBody(contentType)

            suspendCancellableCoroutine { cont ->
                resource.put(
                    body = requestBody,
                    ifETag = null,
                    ifScheduleTag = null,
                    ifNoneMatch = false,
                    callback = ResponseCallback { response ->
                        if (response.isSuccessful) {
                            cont.resume(true)
                        } else {
                            cont.resumeWithException(IOException("Upload failed: HTTP ${response.code}"))
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("WebDAV", "Upload error", e)
            false
        }
    }

    suspend fun download(remotePath: String, localFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val resource = createResource(remotePath)
            val data = suspendCancellableCoroutine<ByteArray> { cont ->
                resource.get("application/octet-stream", null) { response ->
                    try {
                        val bytes = response.body?.bytes() ?: throw IOException("Empty response body")
                        cont.resume(bytes)
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                }
            }
            localFile.writeBytes(data)
            true
        } catch (e: Exception) {
            Log.e("WebDAV", "Download error", e)
            false
        }
    }

    suspend fun delete(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resource = createResource(remotePath)
            return@withContext suspendCancellableCoroutine { cont ->
                resource.delete { response ->
                    if (response.isSuccessful) {
                        cont.resume(true)
                    } else {
                        cont.resumeWithException(IOException("Delete failed: HTTP ${response.code}"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WebDAV", "Delete error", e)
            false
        }
    }


    suspend fun listCloudPythonFiles(dir: String = "python_files/"): List<String> = withContext(Dispatchers.IO) {
        try {
            val resource = createResource(dir)
            val result = mutableListOf<String>()

            val responses = CompletableDeferred<Unit>()
            resource.propfind(1) { response, _ ->
                try {
                    val href = response.href ?: return@propfind
                    val nameEncoded = href.toString().substringAfterLast("/")
                    if (nameEncoded.endsWith(".py")) {
                        val decoded = runCatching { URLDecoder.decode(nameEncoded, "UTF-8") }.getOrElse { nameEncoded }
                        result += decoded
                    }
                } catch (e: Exception) {
                    Log.e("WebDAV", "解析 response 失败", e)
                } finally {
                    responses.complete(Unit)
                }
            }

            responses.await()
            Log.d("WebDAV", "✅ 成功列出云端文件: $result")
            result
        } catch (e: Exception) {
            Log.e("WebDAV", "❌ 列出云端文件失败", e)
            emptyList()
        }
    }




    suspend fun incrementalSync(
        context: Context,
        dao: SyncFileDao,
        onFilesDownloaded: () -> Unit = {}
    ) {
        Log.d("WebDAV", "🔄 开始增量同步")

        val cloudList = listCloudPythonFiles()
        Log.d("WebDAV", "☁️ 云端文件列表: $cloudList")

        val localEntries = dao.getAll()
        val localMap = localEntries.associateBy { it.fileName } // fileName → entry
        val localDir = File(context.filesDir, "python_files").apply { mkdirs() }

        // 云端 fullName → 解出的 fileName 与 timestamp
        val cloudInfo = cloudList.mapNotNull {
            val name = it.substringBeforeLast("_") + ".py"
            val ts = extractTimestamp(it)
            if (name.isNotBlank()) name to (it to ts) else null
        }.toMap() // fileName → Pair(fullName, timestamp)

        val toDownload = cloudInfo.mapNotNull { (fileName, cloudPair) ->
            val (fullName, cloudTs) = cloudPair
            val localTs = localMap[fileName]?.timestamp ?: 0L
            if (cloudTs > localTs) fullName else null
        }

        Log.d("WebDAV", "⬇️ 需要下载的文件: $toDownload")

        toDownload.forEach { fullName ->
            val localName = fullName.substringBeforeLast("_") + ".py"
            val localFile = File(localDir, localName)
            val remotePath = "python_files/$fullName"
            val success = download(remotePath, localFile)
            if (success) {
                val timestamp = extractTimestamp(fullName)
                dao.insert(FileSyncEntry(localName, fullName, timestamp))
                Log.d("WebDAV", "✅ 下载并记录: $fullName -> ${localFile.name}")
            } else {
                Log.e("WebDAV", "❌ 下载失败: $remotePath")
            }
        }

        val toUpload = localEntries.mapNotNull { entry ->
            val cloudTs = cloudInfo[entry.fileName]?.second ?: 0L
            if (entry.timestamp > cloudTs) entry else null
        }

        Log.d("WebDAV", "⬆️ 需要上传的文件: ${toUpload.map { it.fullNameWithTimestamp }}")

        toUpload.forEach { entry ->
            val localFile = File(localDir, entry.fileName)
            if (localFile.exists()) {
                val remotePath = "python_files/${entry.fullNameWithTimestamp}"
                val success = upload(remotePath, localFile)
                if (success) {
                    Log.d("WebDAV", "✅ 上传成功: ${entry.fileName} -> $remotePath")
                } else {
                    Log.e("WebDAV", "❌ 上传失败: $remotePath")
                }
            } else {
                Log.w("WebDAV", "⚠️ 本地文件不存在: ${entry.fileName}")
            }
        }

        Log.d("WebDAV", "✅ 增量同步完成")
        onFilesDownloaded()
    }



    private fun extractTimestamp(name: String): Long {
        return name.substringAfterLast("_").removeSuffix(".py").toLongOrNull() ?: 0L
    }
}