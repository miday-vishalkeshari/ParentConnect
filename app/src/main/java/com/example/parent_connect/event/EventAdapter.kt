package com.example.parent_connect.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

class EventAdapter(
    private val eventList: List<Event>,
    private val userRole: String,
    private val onDeleteClick: (Event) -> Unit // Callback for delete action
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventTitle: TextView = view.findViewById(R.id.eventTitle)
        val eventDate: TextView = view.findViewById(R.id.eventDate)
        val eventDescription: TextView = view.findViewById(R.id.eventDescription)
        val schoolName: TextView = view.findViewById(R.id.schoolName) // Add the school name TextView
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton) // Add delete button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.eventTitle.text = event.title
        holder.eventDate.text = event.date
        holder.eventDescription.text = event.description
        holder.schoolName.text = event.schoolName // Bind the school name

        // Show or hide delete button based on user role
        if (userRole == "SchoolAdmin") {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClick(event)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount() = eventList.size
}
