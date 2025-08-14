package tech.takenoko.agent.entity

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.guava.await
import java.io.File
import java.util.concurrent.atomic.AtomicReference

class LLModel(
    context: Context,
    file: File,
) {
    private val inference: LlmInference

    init {
        println("LLModel initialized with file: ${file.absolutePath}")
        inference = LlmInference.createFromOptions(
            context,
            LlmInference.LlmInferenceOptions.builder()
                .setModelPath(file.path)
                .setPreferredBackend(LlmInference.Backend.DEFAULT)
                .setMaxTopK(8 /*64*/)
                .build(),
        )
        println("LlmInference created successfully")
    }

    suspend fun infer(prompt: String, updated: (String) -> Unit): String {
        println("Starting inference...")
        val resultText = AtomicReference("")
        val result = inference.generateResponseAsync(prompt) { partialResult, done ->
            println("Partial result: $partialResult, done: $done")
            resultText.updateAndGet { it + partialResult }
            updated(resultText.get())
        }
        println("result: ${result.await()}")
        return resultText.get()
    }
}
