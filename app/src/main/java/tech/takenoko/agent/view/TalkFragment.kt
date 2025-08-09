package tech.takenoko.agent.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tech.takenoko.agent.R
import tech.takenoko.agent.databinding.FragmentTalkBinding

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
    }

    fun updateText(isUser: Boolean, text: String) {
        val adapter = recyclerViewAdapter
        if (adapter.items.lastOrNull()?.first == isUser) adapter.items.removeLastOrNull()
        adapter.items.add(isUser to text)
        adapter.notifyItemChanged(adapter.items.lastIndex)
    }

    private class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
        val items: MutableList<Pair<Boolean, String>> = mutableListOf()
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.textView)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = when (viewType) {
            0 -> ViewHolder(
                LayoutInflater.from(
                    parent.context,
                ).inflate(R.layout.list_item_user, parent, false),
            )
            else -> ViewHolder(
                LayoutInflater.from(
                    parent.context,
                ).inflate(R.layout.list_item_agent, parent, false),
            )
        }
        override fun getItemCount(): Int = items.count()
        override fun getItemViewType(position: Int): Int = if (items[position].first) 0 else 1
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position].second
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
