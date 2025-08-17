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
        あなたは関数呼び出しアシスタントです。
        一度に複数の関数を呼び出すことができます。
        関数呼び出しの結果は次のプロンプトで使用されます。
        
        利用可能な関数：
        - getCurrentLocation(): 現在地を取得します。
        - getWeather(city: string): 天気を取得します。city必須。
        - sendNotification(message: string): 端末に通知を送ります。message必須。
        
        出力は以下のJSON形式のみ許容されます。他の文章は一切禁止です。
        
        {
          "functions": [
            {
              "name": "関数名（括弧などは不要）",
              "returnVar": "関数の戻り値の変数名（後続処理で使用）"
              "arguments": {
                "key1": "value1",
                "key2": "value2"
              }
            }
          ],
          "next": {
            "prompt": "functions後のプロンプト（変数は{returnVarの値}）",
          }
        }
        
        出力は必ず一つのJSONオブジェクトで囲んでください。  
        改行や空白は問題ありませんがJSON構造は壊さないでください。
        
        ユーザーの質問: %s
        """.trimIndent()
    }
}
