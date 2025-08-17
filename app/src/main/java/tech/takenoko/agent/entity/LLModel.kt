package tech.takenoko.agent.entity

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions
import kotlinx.coroutines.guava.await
import java.io.File
import java.util.concurrent.atomic.AtomicReference

class LLModel(
    context: Context,
    file: File,
) {
    private val inference: LlmInference
    private val session: LlmInferenceSession

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
        session = LlmInferenceSession.createFromOptions(
            inference,
            LlmInferenceSessionOptions.builder()
                .build(),
        )
        println("LlmInference created successfully")
    }

    suspend fun inferSingle(prompt: String, updated: (String) -> Unit): String {
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

    suspend fun infer(prompt: String, updated: (String) -> Unit): String {
        println("Starting inference...")
        val resultText = AtomicReference("")
        session.addQueryChunk(prompt)
        val result = session.generateResponseAsync { partialResult, done ->
            println("Partial result: $partialResult, done: $done")
            resultText.updateAndGet { it + partialResult }
            updated(resultText.get())
        }
        println("result: ${result.await()}")
        return resultText.get()
    }
}
