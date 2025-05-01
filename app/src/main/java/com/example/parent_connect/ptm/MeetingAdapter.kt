package com.example.parent_connect.ptm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import kotlinx.android.synthetic.main.item_meeting.view.*

class MeetingAdapter(private val meetingList: List<Meeting>) : RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meeting, parent, false)
        return MeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val meeting = meetingList[position]
        holder.bind(meeting)
    }

    override fun getItemCount(): Int {
        return meetingList.size
    }

    class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(meeting: Meeting) {
            itemView.dateText.text = meeting.date
            itemView.titleText.text = meeting.title
            itemView.timeText.text = meeting.time
            itemView.descriptionText.text = meeting.description
        }
    }
}
