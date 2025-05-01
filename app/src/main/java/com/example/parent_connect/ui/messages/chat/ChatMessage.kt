package com.example.parent_connect.ui.messages.chat


data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L // Use Long type for timestamp
)
