package tech.takenoko.agent.usecase

import tech.takenoko.agent.entity.Action

class CallActionUseCase {
    fun execute(action: Action.Function): Any {
        println("action: ${action.name}")
        return when (action.name.trimEnd(*"()".toCharArray())) {
            "getCurrentLocation" -> getCurrentLocation()
            "getWeather" -> getWeather(requireNotNull(action.arguments["city"]))
            "sendNotification" -> sendNotification(requireNotNull(action.arguments["message"]))
            else -> "不明なアクション: ${action.name}"
        }
    }

    private fun getCurrentLocation(): Map<String, Double> = mapOf(
        "lat" to 35.6895,
        "lon" to 139.6917,
    )

    private fun getWeather(
        city: String,
    ): String = when (city.length % 3) {
        0 -> "晴"
        1 -> "曇"
        2 -> "雨"
        else -> "雪"
    }

    private fun sendNotification(
        message: String,
    ) = println("通知: $message")
}
