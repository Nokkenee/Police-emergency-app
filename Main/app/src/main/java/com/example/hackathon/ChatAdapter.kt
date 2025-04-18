package com.example.hackathon

import Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages: MutableList<Message> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val textTimestamp: TextView = itemView.findViewById(R.id.textTimestamp)

        fun bind(message: Message) {
            textMessage.text = message.messageContent
            textTimestamp.text = message.timestamp
            val userMessageColor = ContextCompat.getColor(itemView.context, R.color.background3)
            val officerMessageColor = ContextCompat.getColor(itemView.context, R.color.background2)

            if (message.sender == "Basicuser") {
                itemView.setBackgroundColor(userMessageColor)
            } else {
                itemView.setBackgroundColor(officerMessageColor) // Blue for officer messages
            }
        }
    }
}
