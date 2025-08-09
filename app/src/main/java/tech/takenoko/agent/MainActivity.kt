package tech.takenoko.agent

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import tech.takenoko.agent.usecase.LoadModelUseCase
import tech.takenoko.agent.view.TalkFragment

class MainActivity : AppCompatActivity() {

    private val talkFragment: TalkFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.talk_fragment) as TalkFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            val model = LoadModelUseCase(applicationContext).execute()
            withContext(Dispatchers.Main) {
                talkFragment.updateText(true, "こんにちは")
            }
            model.infer("こんにちは") {
                launch(Dispatchers.Main) {
                    talkFragment.updateText(false, it)
                }
            }
        }
    }
}
