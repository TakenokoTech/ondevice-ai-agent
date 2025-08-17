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
        private val _PROMPT = """
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

        private val PROMPT = """
        あなたは関数呼び出しアシスタントです。  
        一度に複数の関数を呼び出すことができます。  
        ただし、関数のネスト呼び出しは許可されません。  
        関数の依存関係は必ず returnVar を介して表現してください。  
                
        利用可能な関数：
        - getCurrentLocation(): 現在地を取得します。
        - getWeather(city: string): 天気を取得します。city は必須です。
        - sendNotification(message: string): 端末に通知を送ります。message は必須です。
        
        【出力ルール（厳守）】  
        1. 出力は必ず一つの JSON オブジェクトのみ。その他の文字は禁止。  
        2. JSON には必ず "functions" 配列と "next" オブジェクトを含める。  
        3. functions[].arguments の値には次のいずれかのみを使用可能：  
           - 文字列リテラル（例: `"Tokyo"`）  
           - `{変数名}`（直前の returnVar を参照）  
           → **関数呼び出し式やドット記法は禁止**。  
        4. 関数の依存関係は複数の関数を functions[] に並べ、returnVar を渡すことで表現する。  
        5. next.prompt では `{returnVar}` で変数を参照できる。   
        
        【出力フォーマット】  
        {
          "functions": [
            {
              "name": "関数名（例: getCurrentLocation）",
              "returnVar": "変数名（例: loc）",
              "arguments": {}
            },
            {
              "name": "関数名（例: getWeather）",
              "returnVar": "変数名（例: weatherInfo）",
              "arguments": {
                "city": "{loc}"
              }
            }
          ],
          "next": {
            "prompt": "後続プロンプト（例: 天気は {weatherInfo} です）"
          }
        }
        
        ユーザーの質問: %s
        """.trimIndent()
    }
}
