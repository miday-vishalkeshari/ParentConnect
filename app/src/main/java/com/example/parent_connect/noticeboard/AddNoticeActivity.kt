package com.example.parent_connect.noticeboard

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.*

class AddNoticeActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button

    // Initialize Firestore
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notice)

        titleEditText = findViewById(R.id.editTextTitle)
        descriptionEditText = findViewById(R.id.editTextDescription)
        submitButton = findViewById(R.id.btnSubmitNotice)

        // Handle form submission
        submitButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            sendNotificationToAllUsers(title, description)

            // Get the current date and time for the notice
            val currentDate = getCurrentDateTime()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Create new notice object
                val newNotice = Notice(title, description, currentDate)

                // Send the new notice back to NoticeBoardActivity
                val resultIntent = intent
                resultIntent.putExtra("newNotice", newNotice) // Return the new notice
                setResult(Activity.RESULT_OK, resultIntent) // Set result as OK
                finish() // Close this activity and return to the previous one
            }
        }
    }

    // Function to send a notification to all users
    private fun sendNotificationToAllUsers(title: String, description: String) {
        Log.d("AddNoticeActivity", "Starting to send notifications to all users...")

        // Get the list of all users' FCM tokens from Firestore
        firestore.collection("users")
            .get() // Fetch all users (no role filter)
            .addOnSuccessListener { snapshot ->
                Log.d("AddNoticeActivity", "Firestore query successful. Number of users: ${snapshot.size()}")

                val tokens = mutableListOf<String>()
                for (document in snapshot) {
                    // Get the FCM token for each user
                    val fcmToken = document.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                        Log.d("AddNoticeActivity", "FCM token found for user: $fcmToken")
                        tokens.add(fcmToken)
                    } else {
                        Log.d("AddNoticeActivity", "No FCM token found for user: ${document.id}")
                    }
                }

                // Send notification to all users if tokens are available
                if (tokens.isNotEmpty()) {
                    Log.d("AddNoticeActivity", "Sending notifications to ${tokens.size} users...")
                    for (token in tokens) {
                        try {
                            // Build the message using the recipient's FCM token
                            val message = RemoteMessage.Builder(token) // Use the user's FCM token
                                .setMessageId(System.currentTimeMillis().toString()) // Unique message ID
                                .addData("title", "New Notice: $title") // Add title to the notification
                                .addData("body", description) // Add description to the notification
                                .build()

                            // Send the message using FirebaseMessaging
                            FirebaseMessaging.getInstance().send(message)
                            Log.d("AddNoticeActivity", "Notification sent to user with token: $token")
                        } catch (e: Exception) {
                            Log.e("AddNoticeActivity", "Error sending notification to user with token: $token", e)
                        }
                    }
                    Log.d("AddNoticeActivity", "Notifications sent to ${tokens.size} users.")
                } else {
                    Log.d("AddNoticeActivity", "No FCM tokens found for users.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddNoticeActivity", "Error fetching FCM tokens from Firestore: ", e)
            }
    }

    // Helper function to get the current date and time
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}