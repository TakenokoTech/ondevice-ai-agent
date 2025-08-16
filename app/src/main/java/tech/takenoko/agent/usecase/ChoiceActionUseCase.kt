package tech.takenoko.agent.usecase

import kotlinx.serialization.json.Json
import tech.takenoko.agent.entity.Action
import tech.takenoko.agent.entity.LLModel

class ChoiceActionUseCase {

    suspend fun execute(
        model: LLModel,
        input: String,
        updated: (String) -> Unit,
    ): Action? {
        val res = model.infer(PROMPT.format(input)) {
            updated(it.trimCodeBlock())
        }
        return runCatching {
            Json.decodeFromString<Action>(res.trimCodeBlock())
        }.getOrNull()
    }

    fun String.trimCodeBlock() = this
        .removePrefix("```json")
        .removeSuffix("```")
        .trim()

    companion object {
        private val PROMPT = """
        あなたはAndroidアプリ内で動作する関数呼び出しアシスタントです。
        
        利用可能な関数：
        - getCurrentLocation(): 現在地を取得します。
        - getWeather(city: string): 天気を取得します。必ずcityキーを含めてください。
        - sendNotification(message: string): 端末に通知を送ります。messageキー必須。
        
        出力は以下のJSON形式のみ許容されます。他の文章は一切禁止です。
        
        {
          "name": "関数名",
          "arguments": {
            "key1": "value1",
            "key2": "value2"
          }
        }
        
        出力は必ず一つのJSONオブジェクトで囲んでください。  
        改行や空白は問題ありませんがJSON構造は壊さないでください。
        
        ユーザーの質問: %s
        """.trimIndent()
    }
}
