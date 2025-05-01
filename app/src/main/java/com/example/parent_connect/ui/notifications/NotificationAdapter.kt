package com.example.parent_connect.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

class NotificationAdapter(private val notifications: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textNotificationTitle)
        val messageTextView: TextView = view.findViewById(R.id.textNotificationMessage)
        val timeTextView: TextView = view.findViewById(R.id.textNotificationTime)
        val schoolNameTextView: TextView = view.findViewById(R.id.textNotificationSchoolName) // New TextView for school name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.titleTextView.text = notification.title
        holder.messageTextView.text = notification.message
        holder.timeTextView.text = notification.time
        holder.schoolNameTextView.text = notification.schoolName // Bind the school name
    }

    override fun getItemCount(): Int = notifications.size
}
