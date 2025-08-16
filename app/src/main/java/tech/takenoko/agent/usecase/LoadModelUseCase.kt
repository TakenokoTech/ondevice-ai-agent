package tech.takenoko.agent.usecase

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import tech.takenoko.agent.BuildConfig
import tech.takenoko.agent.entity.LLModel
import tech.takenoko.agent.entity.ModelConfig
import java.io.File
import java.io.FileOutputStream

class LoadModelUseCase(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun execute(): LLModel {
        if (modelMap.containsKey(MODEL_ID)) return modelMap.getValue(MODEL_ID)
        val file = File(context.cacheDir, MODEL_FILE)
        if (!file.exists()) download(file)
        return LLModel(context, file).also { modelMap[MODEL_ID] = it }
    }

    private suspend fun download(outFile: File) = withContext(Dispatchers.IO) {
        val request = Request
            .Builder()
            .url(MODEL_URL.format(MODEL_ID, MODEL_FILE))
            .addHeader("Authorization", "Bearer ${BuildConfig.HF_TOKEN}")
            .build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "\"HTTP error: ${response.code}" }
            val inputStream = response.body?.byteStream() ?: error("Response body is null")
            val contentLength = response.body?.contentLength() ?: error("Response body is null")
            var lastProgress = -1L // 前回表示した進捗率
            inputStream.use { input ->
                FileOutputStream(outFile).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE * 1024)
                    var bytesCopied: Long = 0
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes

                        val progress = (bytesCopied * 100) / contentLength
                        val bytesCopiedBytes = (bytesCopied / 1024 / 1024).toInt()
                        val contentLengthBytes = (contentLength / 1024 / 1024).toInt()
                        if (contentLength > 0 && lastProgress != progress) {
                            lastProgress = progress
                            println(
                                "Download progress: ${progress.toInt()}% " +
                                    "(${bytesCopiedBytes}MB/${contentLengthBytes}MB)",
                            )
                        }
                        bytes = input.read(buffer)
                    }
                }
            }
        }
    }

    companion object {
        // private const val MODEL_ID = "google/gemma-3n-E4B-it-litert-preview"
        // private const val MODEL_FILE = "gemma-3n-E4B-it-int4.task"
        // private const val MODEL_ID = "google/gemma-3n-E2B-it-litert-preview"
        // private const val MODEL_FILE = "gemma-3n-E2B-it-int4.task"
        // private const val MODEL_ID = "litert-community/gemma-3-270m-it"
        // private const val MODEL_FILE = "gemma3-270m-it-q8.task"
        private val MODEL_ID = ModelConfig.GEMMA_3N_E2B.modelId
        private val MODEL_FILE = ModelConfig.GEMMA_3N_E2B.modelFile
        private const val MODEL_URL = "https://huggingface.co/%s/resolve/main/%s?download=true"
        private val modelMap = mutableMapOf<String, LLModel>()
    }
}
