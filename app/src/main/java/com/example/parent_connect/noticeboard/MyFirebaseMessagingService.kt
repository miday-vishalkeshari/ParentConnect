package com.example.parent_connect.noticeboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.parent_connect.R // Replace with your app's package name
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("MyFirebaseMessagingService", "Message received: ${remoteMessage.data}")

        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            val title = it.title ?: "New Notice"
            val body = it.body ?: "You have a new notice."
            Log.d("MyFirebaseMessagingService", "Notification payload: title=$title, body=$body")

            // Show the notification
            showNotification(title, body)
        }

        // Check if the message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("MyFirebaseMessagingService", "Message data payload: ${remoteMessage.data}")

            val title = remoteMessage.data["title"] ?: "New Notice"
            val body = remoteMessage.data["body"] ?: "You have a new notice."
            Log.d("MyFirebaseMessagingService", "Data payload: title=$title, body=$body")

            // Show the notification
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, body: String) {
        Log.d("MyFirebaseMessagingService", "Showing notification: title=$title, body=$body")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "notice_channel_id", // Channel ID
                "Notices", // Channel name
                NotificationManager.IMPORTANCE_HIGH // Importance level
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to open the app when the notification is clicked
        val intent = Intent(this, NoticeBoardActivity::class.java) // Replace with your main activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, "notice_channel_id")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.notifications_icon) // Replace with your app's notification icon
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the intent to open the app
            .build()

        // Show the notification
        notificationManager.notify(notificationId, notification)
        Log.d("MyFirebaseMessagingService", "Notification shown with ID: $notificationId")
    }

    override fun onNewToken(token: String) {
        Log.d("MyFirebaseMessagingService", "Refreshed token: $token")

        // Save the new token to Firestore or your server
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            saveTokenToDatabase(token, userId)
        } else {
            Log.e("MyFirebaseMessagingService", "User ID is null. Token not saved.")
        }
    }

    private fun saveTokenToDatabase(token: String, userId: String) {
        // Save the token to Firestore or your server
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("MyFirebaseMessagingService", "Token saved successfully for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("MyFirebaseMessagingService", "Error saving token for user: $userId", e)
            }
    }
}