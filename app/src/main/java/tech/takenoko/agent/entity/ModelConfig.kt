package tech.takenoko.agent.entity

enum class ModelConfig(
    val modelId: String,
    val modelFile: String,
) {
    GEMMA_3N_E4B("google/gemma-3n-E4B-it-litert-preview", "gemma-3n-E4B-it-int4.task"),
    GEMMA_3N_E2B("google/gemma-3n-E2B-it-litert-preview", "gemma-3n-E2B-it-int4.task"),
    GEMMA_3_270M_IT("litert-community/gemma-3-270m-it", "gemma3-270m-it-q8.task"),
}
