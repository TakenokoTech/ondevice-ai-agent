package tech.takenoko.agent.entity

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Action(
    val functions: List<Function>,
    val next: Next,
) {
    @Serializable
    data class Function(
        val name: String,
        val returnVar: String,
        val arguments: Map<String, String> = mapOf(),
    )

    @Serializable
    data class Next(
        val prompt: String,
    )
}
