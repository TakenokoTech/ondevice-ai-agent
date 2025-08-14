package tech.takenoko.agent.entity

import kotlinx.serialization.Serializable

@Serializable
data class Action(
    val name: String,
    val arguments: Map<String, String>,
)
