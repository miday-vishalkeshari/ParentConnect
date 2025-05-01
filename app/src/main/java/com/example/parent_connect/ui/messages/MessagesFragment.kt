package com.example.parent_connect.ui.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.example.parent_connect.ui.messages.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesFragment : Fragment(), MessageAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<MessageItem>()
    private lateinit var firestore: FirebaseFirestore
    // Declare role as a global variable
    private var role: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_messages, container, false)

        // Initialize RecyclerView
        recyclerView = root.findViewById(R.id.recycler_view_message_fragment)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get the user ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Fetch the user's role from Firestore to check if the user is a "parent", "teacher", or "admin"
            checkUserRoleAndFetchData(userId)
        } else {
            Log.e("WardsFragment", "User not logged in or userId is null")
        }

        return root
    }

    private fun checkUserRoleAndFetchData(userId: String) {
        // Fetch the user's role from Firestore
        firestore.collection("users") // Assuming the users' role is stored in the users collection
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    role = document.getString("role") // Fetch the role field

                    Log.d("MessagesFragment", "Fetched Role: $role")

                    // Initialize the adapter with the fetched role
                    messageAdapter = MessageAdapter(messageList, this, role)
                    recyclerView.adapter = messageAdapter

                    when (role) {
                        "Parent" -> {
                            // Fetch data for parent role
                            fetchMessagesFromFirestore("Schools/sch1/Students/adm1/messages")
                        }
                        else -> {
                            // Fetch data for teacher or admin role
                            fetchMessagesFromFirestore("Schools/sch1/staffs/staff1/messages")
                        }
                    }
                } else {
                    Log.e("MessageFragment", "User document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MessageFragment", "Error fetching user role: ${exception.message}")
            }
    }

    private fun fetchMessagesFromFirestore(addressPath: String) {
        firestore.collection(addressPath)
            .get()
            .addOnSuccessListener { snapshot ->
                messageList.clear()

                val messageIds = snapshot.documents.map { it.id }

                // Now fetch details for each messageId from the "messages" collection
                fetchMessagesDetails(messageIds)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents from $addressPath: ", exception)
            }
    }

    private fun fetchMessagesDetails(messageIds: List<String>) {
        for (messageId in messageIds) {
            firestore.collection("messages")
                .document(messageId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        var messageName = document.getString("student_name") ?: ""
                        var messageDescriptor = document.getString("student_title") ?: ""

                        val teacherName = document.getString("teachername") ?: ""
                        val teacherTitle = document.getString("teachers_title") ?: ""

                        if (role == "Parent" || role == "SchoolAdmin") {
                            messageName = teacherName
                            messageDescriptor = teacherTitle
                        }

                        val messageItem = MessageItem(messageId, messageName, messageDescriptor)
                        messageList.add(messageItem)
                        messageAdapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting document with ID $messageId: ", exception)
                }
        }
    }

    override fun onItemClick(position: Int) {
        val clickedMessage = messageList[position]

        // Create an Intent to navigate to ChatActivity
        val intent = Intent(activity, ChatActivity::class.java).apply {
            putExtra("messageId", clickedMessage.messageId)
            putExtra("messageName", clickedMessage.messageName)
            putExtra("messageDescription", clickedMessage.messageDescription)
            putExtra("role", role)
        }

        // Start the ChatActivity
        startActivity(intent)
    }
}
