package com.example.parent_connect.ui.messages.chat

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var firestore: FirebaseFirestore
    private var messageId: String? = null
    private var role: String? = null

    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button

    private val userNamesMap = mutableMapOf<String, String>() // Holds senderId -> senderName mapping

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Get the messageId from the Intent
        messageId = intent.getStringExtra("messageId")
        role = intent.getStringExtra("role")

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_chat)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Adapter with empty user names map (we will update it later)
        chatAdapter = ChatAdapter(chatList, userNamesMap,role)
        recyclerView.adapter = chatAdapter

        // Initialize EditText and Button
        editTextMessage = findViewById(R.id.edit_text_message)
        buttonSend = findViewById(R.id.button_send)

        // Set button click listener to send message
        buttonSend.setOnClickListener {
            sendMessage()
        }

        // Fetch chat messages
        fetchChatMessages()

        // Fetch user names (for displaying sender name instead of senderId)
        fetchUserNames()
    }

    private fun fetchChatMessages() {
        messageId?.let {
            firestore.collection("messages")
                .document(it)
                .collection("chats")
                .orderBy("timestamp") // Ensure this is the correct field to order by
                .get()
                .addOnSuccessListener { snapshot ->
                    val newMessages = mutableListOf<ChatMessage>()
                    for (document in snapshot) {
                        val chatMessage = document.toObject(ChatMessage::class.java)
                        newMessages.add(chatMessage)
                    }
                    chatAdapter.updateChatList(newMessages) // Update adapter with new data
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting chats: ", exception)
                    Toast.makeText(this, "Error loading chats", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchUserNames() {
        // Fetch user names from Firestore (you may want to use another collection to store user details)
        firestore.collection("users") // Assuming you have a users collection
            .get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot) {
                    val userId = document.id
                    val userName = document.getString("name") ?: "Unknown"
                    userNamesMap[userId] = userName
                }
                // After fetching the user names, update the adapter to display them
                chatAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting users: ", exception)
            }
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()

        if (messageText.isNotEmpty()) {
            // Get current user ID from Firebase Authentication
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null && messageId != null) {
                val newMessage = ChatMessage(
                    messageId = UUID.randomUUID().toString(), // Generate a unique message ID
                    senderId = currentUserId,
                    messageText = messageText,
                    timestamp = System.currentTimeMillis() // Store timestamp as Long (milliseconds)
                )

                // Add message to Firestore
                firestore.collection("messages")
                    .document(messageId!!)
                    .collection("chats")
                    .add(newMessage)
                    .addOnSuccessListener {
                        // Clear the input field after sending the message
                        editTextMessage.text.clear()
                        // Optionally, update the chat list with the new message immediately
                        chatList.add(newMessage)
                        chatAdapter.notifyItemInserted(chatList.size - 1)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firestore", "Error adding message: ", exception)
                        Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }
}
