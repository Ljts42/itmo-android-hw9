package com.github.ljts42.hw9_arch.ui.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ljts42.hw9_arch.R
import com.github.ljts42.hw9_arch.network.Message

class ChatAdapter(
    private var messages: MutableList<Message>, private val onClickImage: (Message) -> Unit
) : RecyclerView.Adapter<MessageViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].data?.Text != null) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val holder = MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        )
        if (viewType == 1) {
            holder.imageView.setOnClickListener {
                onClickImage(messages[holder.adapterPosition])
            }
        }
        return holder
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) =
        holder.bind(messages[position])

    fun update(newMessages: MutableList<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}