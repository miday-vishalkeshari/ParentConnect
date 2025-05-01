package com.example.parent_connect.holidays

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

class HolidayAdapter(private val holidays: List<Holiday>) : RecyclerView.Adapter<HolidayAdapter.HolidayViewHolder>() {

    inner class HolidayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.holidayTitle)
        val date: TextView = itemView.findViewById(R.id.holidayDate)
        val description: TextView = itemView.findViewById(R.id.holidayDescription)
        val schoolName: TextView = itemView.findViewById(R.id.schoolName) // Added binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_holiday, parent, false)
        return HolidayViewHolder(view)
    }

    override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
        val holiday = holidays[position]
        holder.title.text = holiday.title
        holder.date.text = holiday.date
        holder.description.text = holiday.description
        holder.schoolName.text = holiday.schoolName // Bind the school name
    }

    override fun getItemCount(): Int = holidays.size
}
