package tech.takenoko.agent.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.takenoko.agent.R
import tech.takenoko.agent.databinding.FragmentTalkBinding
import tech.takenoko.agent.entity.Message
import tech.takenoko.agent.entity.MessageType
import tech.takenoko.agent.usecase.CallActionUseCase
import tech.takenoko.agent.usecase.ChoiceActionUseCase
import tech.takenoko.agent.usecase.LoadModelUseCase

class TalkFragment : Fragment() {
    private lateinit var binding: FragmentTalkBinding
    private val recyclerViewAdapter = RecyclerViewAdapter()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentTalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = recyclerViewAdapter
        binding.button.setOnClickListener {
            val inputText = binding.inputText.text.toString()
            if (!inputText.isNotBlank()) return@setOnClickListener
            updateText(MessageType.USER, inputText)
            updateText(MessageType.AGENT, "...")
            binding.inputText.text.clear()
            lifecycleScope.launch {
                val model = LoadModelUseCase(requireContext()).execute()
                val action = ChoiceActionUseCase().execute(model, inputText) { response ->
                    launch(Dispatchers.Main) {
                        updateText(MessageType.AGENT, response)
                    }
                }
                launch(Dispatchers.Main) {
                    updateText(MessageType.APPROVE, action.name) {
                        val text = CallActionUseCase().execute(action).toString()
                        updateText(MessageType.ACTION, text)
                        lifecycleScope.launch {
                            model.infer("結果は${text}でした。説明してください。") {
                                updateText(MessageType.AGENT, it)
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateText(
        messageType: MessageType,
        text: String,
        action: () -> Unit = { },
    ) = lifecycleScope.launch(Dispatchers.Main) {
        val adapter = recyclerViewAdapter
        if (adapter.items.lastOrNull()?.type == messageType) adapter.items.removeLastOrNull()
        adapter.items.add(Message(messageType, text, action))
        adapter.notifyItemChanged(adapter.items.lastIndex)
        binding.recyclerView.scrollToPosition(adapter.items.lastIndex)
    }

    private class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
        val items: MutableList<Message> = mutableListOf()

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.textView)
            val approveButton: MaterialButton? = view.findViewById(R.id.approveButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = when (viewType) {
            MessageType.USER.ordinal -> ViewHolder(
                LayoutInflater.from(
                    parent.context,
                ).inflate(R.layout.list_item_user, parent, false),
            )

            MessageType.AGENT.ordinal -> ViewHolder(
                LayoutInflater.from(
                    parent.context,
                ).inflate(R.layout.list_item_agent, parent, false),
            )

            MessageType.APPROVE.ordinal -> ViewHolder(
                LayoutInflater.from(
                    parent.context,
                ).inflate(R.layout.list_item_approve, parent, false),
            )

            MessageType.ACTION.ordinal -> ViewHolder(
                LayoutInflater.from(
                    parent.context,
                ).inflate(R.layout.list_item_agent, parent, false),
            )

            else -> TODO()
        }

        override fun getItemCount(): Int = items.count()
        override fun getItemViewType(position: Int): Int = items[position].type.ordinal
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position].text
            holder.approveButton?.setOnClickListener { items[position].action() }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = TalkFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
    }
}
