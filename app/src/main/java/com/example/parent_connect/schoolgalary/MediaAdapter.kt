package com.example.parent_connect.schoolgalary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parent_connect.R

class MediaAdapter(
    private val context: Context,
    private val mediaList: List<String>, // List of media URLs or local paths
    private val onItemClick: (String) -> Unit // Callback for item click
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaUrl = mediaList[position]
        // Load image using Glide
        Glide.with(context)
            .load(mediaUrl)
            .placeholder(R.drawable.ic_poster_placeholder) // Placeholder while loading
            .into(holder.mediaThumbnail)

        // Handle click event
        holder.itemView.setOnClickListener {
            onItemClick(mediaUrl)
        }
    }

    override fun getItemCount(): Int = mediaList.size

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mediaThumbnail: ImageView = itemView.findViewById(R.id.media_thumbnail)
    }
}
