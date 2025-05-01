package com.example.parent_connect.homework

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

class HomeworkAdapter(private val homeworkList: List<HomeworkItem>, private val role: String) :
    RecyclerView.Adapter<HomeworkAdapter.HomeworkViewHolder>() {

    // ViewHolder class for the homework items
    class HomeworkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectTextView: TextView = itemView.findViewById(R.id.subjectTextView)
        val taskTextView: TextView = itemView.findViewById(R.id.taskTextView)
        val addImageButton: ImageButton = itemView.findViewById(R.id.addImageButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_homework, parent, false)
        return HomeworkViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        val homeworkItem = homeworkList[position]
        holder.subjectTextView.text = homeworkItem.subject
        holder.taskTextView.text = homeworkItem.task

         //Hide the add button for parents
        if (role == "Parent") {
            holder.addImageButton.visibility = View.GONE
        } else {
            holder.addImageButton.visibility = View.VISIBLE
            holder.addImageButton.setOnClickListener {
                // have to implement delete homework logic
                Toast.makeText(holder.itemView.context, "Have to implement this delete homework logic here", Toast.LENGTH_SHORT).show()


            }
        }
    }

    override fun getItemCount(): Int {
        return homeworkList.size
    }
}