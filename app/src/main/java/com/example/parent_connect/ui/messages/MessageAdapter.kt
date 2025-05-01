package com.example.parent_connect.ui.messages

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

class MessageAdapter(
    private val messageList: List<MessageItem>,
    private val onItemClickListener: OnItemClickListener,
    private val role: String?
) : RecyclerView.Adapter<MessageAdapter.GroupViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    class GroupViewHolder(itemView: View, listener: OnItemClickListener?) : RecyclerView.ViewHolder(itemView) {
        val groupNameTextView: TextView = itemView.findViewById(R.id.messageNameTextView)
        val groupDescriptionTextView: TextView = itemView.findViewById(R.id.messageDescriptionTextView)
        val roleIcon: ImageView = itemView.findViewById(R.id.participant_avatar)

        init {
            itemView.setOnClickListener { listener?.onItemClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return GroupViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = messageList[position]
        holder.groupNameTextView.text = group.messageName
        holder.groupDescriptionTextView.text = group.messageDescription

        Log.d("MessageAdapter", "Role: $role")

        // Set role icon based on the role
        when (role) {
            "Teacher" -> holder.roleIcon.setImageResource(R.drawable.parents_icon) // Set icon for teacher
            "Parent" -> holder.roleIcon.setImageResource(R.drawable.teacher_icon) // Set icon for parent
            "SchoolAdmin" -> holder.roleIcon.setImageResource(R.drawable.person_icon) // Set icon for school admin
            else -> holder.roleIcon.setImageResource(R.drawable.person_icon) // Default icon if no role matches
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    // Method to return the group at a specific position
    fun getGroupAtPosition(position: Int): MessageItem {
        return messageList[position]
    }
}
