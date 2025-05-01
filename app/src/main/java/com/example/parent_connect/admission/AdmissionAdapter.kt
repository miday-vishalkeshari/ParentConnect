package com.example.parent_connect.admission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parent_connect.R

class AdmissionAdapter(
    private val offers: MutableList<AdmissionOffer>,
    private val onDeleteClickListener: (Int) -> Unit, // Lambda to handle delete action
    private val userRole: String, // Role passed from the activity
    private val onImageClick: (String) -> Unit // Lambda to handle image click
) : RecyclerView.Adapter<AdmissionAdapter.AdmissionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdmissionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admission, parent, false)
        return AdmissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdmissionViewHolder, position: Int) {
        val offer = offers[position]
        holder.titleTextView.text = offer.title
        holder.descriptionTextView.text = offer.description

        // Load image dynamically (using Glide)
        if (offer.imgLink.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(offer.imgLink) // Load image from URL or Firebase Storage
                .into(holder.offerImageView)
        }

        // Set the delete button's visibility based on the user role
        if (userRole == "SchoolAdmin") {
            holder.deleteButton.visibility = View.VISIBLE // Show delete button
        } else {
            holder.deleteButton.visibility = View.GONE // Hide delete button for other roles
        }

        // Set the delete button's click listener
        holder.deleteButton.setOnClickListener {
            onDeleteClickListener(position)  // Invoke the delete action
        }

        // Set the image click listener to open full-screen image
        holder.offerImageView.setOnClickListener {
            onImageClick(offer.imgLink) // Invoke the image click action
        }
    }

    override fun getItemCount(): Int = offers.size

    inner class AdmissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val offerImageView: ImageView = itemView.findViewById(R.id.imageViewPoster)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteOffer) // Add delete button
    }

    // Function to delete an item from the list
    fun deleteItem(position: Int) {
        offers.removeAt(position)
        notifyItemRemoved(position)
    }
}
