package com.example.parent_connect.ui.wards

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.example.parent_connect.ui.wards.ward_details.WardDetailActivity
import com.squareup.picasso.Picasso

class WardAdapter(private val wardList: List<Ward>) : RecyclerView.Adapter<WardAdapter.WardViewHolder>() {

    class WardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val wardNameText: TextView = view.findViewById(R.id.wardNameText)  // Ward name
        val schoolNameText: TextView = view.findViewById(R.id.schoolNameText)
        val gradeText: TextView = view.findViewById(R.id.gradeText)
        val admissionNoText: TextView = view.findViewById(R.id.admissionNoText)
        val profileImageView: ImageView = view.findViewById(R.id.ward_dp) // Correct ID for profile image
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ward, parent, false)
        return WardViewHolder(view)
    }

    override fun onBindViewHolder(holder: WardViewHolder, position: Int) {
        val ward = wardList[position]

        // Bind ward details
        holder.wardNameText.text = ward.wardName
        holder.schoolNameText.text = ward.schoolId
        holder.gradeText.text = "Class: ${ward.classId}"
        holder.admissionNoText.text = "Admission No: ${ward.admissionNo}"

        // Load profile image using Picasso
        if (!ward.profileImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(ward.profileImageUrl)
                .placeholder(R.drawable.ward_dp) // Default image while loading
                .error(R.drawable.ward_dp) // Image in case of error
                .into(holder.profileImageView)
        } else {
            // If no image URL is available, set the default image
            holder.profileImageView.setImageResource(R.drawable.ward_dp)
        }

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, WardDetailActivity::class.java).apply {
                putExtra("WARD_NAME", ward.wardName)
                putExtra("WARD_CLASS", ward.classId)
                putExtra("WARD_ADMISSION_NO", ward.admissionNo)
                putExtra("SCHOOL_NAME", ward.schoolId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = wardList.size
}