package tech.takenoko.agent.usecase

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import tech.takenoko.agent.BuildConfig
import tech.takenoko.agent.entity.LLModel
import java.io.File
import java.io.FileOutputStream

class LoadModelUseCase(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun execute(): LLModel {
        val file = File(context.cacheDir, MODEL_FILE)
        if (!file.exists()) download(file)
        return LLModel(context, file)
    }

    private suspend fun download(outFile: File) = withContext(Dispatchers.IO) {
        val request = Request
            .Builder()
            .url(MODEL_URL)
            .addHeader("Authorization", "Bearer ${BuildConfig.HF_TOKEN}")
            .build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "\"HTTP error: ${response.code}" }
            val inputStream = response.body?.byteStream() ?: error("Response body is null")
            val contentLength = response.body?.contentLength() ?: error("Response body is null")
            inputStream.use { input ->
                FileOutputStream(outFile).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesCopied: Long = 0
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        if (contentLength > 0) {
                            val progress = (bytesCopied * 100) / contentLength
                            println("Download progress: ${progress.toInt()}%")
                        }
                        bytes = input.read(buffer)
                    }
                }
            }
        }
    }

    companion object {
        private const val MODEL_ID = "google/gemma-3n-E2B-it-litert-preview"
        private const val MODEL_FILE = "gemma-3n-E2B-it-int4.task"
        private const val MODEL_URL = "https://huggingface.co/$MODEL_ID/resolve/main/$MODEL_FILE?download=true"
    }
}
