package com.example.parent_connect.ui.messages.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val chatMessages: MutableList<ChatMessage>,
    private val userNamesMap: Map<String, String>,
    private val role: String?
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    // ViewHolder for individual chat message
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContainer: LinearLayout = itemView.findViewById(R.id.message_container)
        val senderTextView: TextView = itemView.findViewById(R.id.text_view_sender)
        val messageTextView: TextView = itemView.findViewById(R.id.text_view_message)
        val timestampTextView: TextView = itemView.findViewById(R.id.text_view_timestamp)
        val senderIcon: ImageView = itemView.findViewById(R.id.sender_icon)
        val messageBlock: LinearLayout = itemView.findViewById(R.id.message_block)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = chatMessages[position]

        // Fetch sender name using senderId from the map
        val senderName = userNamesMap[message.senderId] ?: "Unknown"

        holder.messageTextView.text = message.messageText
        holder.timestampTextView.text = formatTimestamp(message.timestamp)

        // Set the sender icon based on role
        setSenderIcon(holder, message.senderId)

        // Align the message based on whether the sender is the current user
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == message.senderId) {
            // Current user's message (align to the right)
            holder.senderTextView.visibility = View.GONE
            holder.senderIcon.visibility = View.GONE
            holder.messageContainer.gravity = Gravity.END
            holder.messageBlock.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.color_current_user_message)
            )
        } else {
            // Other user's message (align to the left)
            holder.senderTextView.visibility = View.VISIBLE
            holder.senderTextView.text = senderName
            holder.senderIcon.visibility = View.VISIBLE
            holder.messageContainer.gravity = Gravity.START
            holder.messageBlock.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.color_other_user_message)
            )
        }
    }

    private fun setSenderIcon(holder: ChatViewHolder, senderId: String) {
        when (role) {
            "Parent" -> {
                holder.senderIcon.setImageResource(R.drawable.teacher_icon) // Set Parent icon
            }
            "Teacher" -> {
                holder.senderIcon.setImageResource(R.drawable.parents_icon) // Set Teacher icon
            }
            else -> {
                holder.senderIcon.setImageResource(R.drawable.person_icon) // Default icon
            }
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    // Method to update the chat list when new data is added
    fun updateChatList(newMessages: List<ChatMessage>) {
        chatMessages.clear()
        chatMessages.addAll(newMessages)
        notifyDataSetChanged()
    }

    // Helper function to format timestamp (if required)
    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(date)
    }
}