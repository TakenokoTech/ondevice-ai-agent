package tech.takenoko.agent.entity

data class Message(
    val type: MessageType,
    val text: String,
    val action: suspend () -> Unit = {},
    // val timestamp: Long,
)

enum class MessageType {
    USER,
    AGENT,
    APPROVE,
    ACTION,
}
