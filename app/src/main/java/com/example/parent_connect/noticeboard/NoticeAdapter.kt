package com.example.parent_connect.noticeboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

class NoticeAdapter(
    private val noticeList: List<Notice>,
    private val userRole: String,
    private val onDeleteClick: (Notice) -> Unit // Callback for delete action
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = noticeList[position]
        holder.noticeTitle.text = notice.title
        holder.noticeDescription.text = notice.description
        holder.noticeDate.text = "Date: ${notice.date}"

        // Show or hide delete button based on user role
        if (userRole == "SchoolAdmin") {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClick(notice)
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return noticeList.size
    }

    class NoticeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noticeTitle: TextView = view.findViewById(R.id.noticeTitle)
        val noticeDescription: TextView = view.findViewById(R.id.noticeDescription)
        val noticeDate: TextView = view.findViewById(R.id.noticeDate)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton) // Add delete button
    }
}
