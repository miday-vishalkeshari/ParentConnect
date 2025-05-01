package com.example.parent_connect.schoolgalary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class SchoolGalaryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MediaAdapter
    private val mediaList = mutableListOf<String>() // To hold fetched media URLs

    // Hardcoded school ID for now
    private val schoolId = "sch1"

    // ProgressBar to show loading state
    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_galary)

        // Initialize views
        recyclerView = findViewById(R.id.media_list_recycler)
        progressBar = findViewById(R.id.progress_bar) // Reference to ProgressBar

        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // Fetch media from Firebase (for sch1)
        fetchMediaFromFirebase()

        // Set up the adapter for displaying media
        adapter = MediaAdapter(this, mediaList) { mediaUrl ->
            openFullScreenMedia(mediaUrl)
        }
        recyclerView.adapter = adapter

        // Set up upload button
        val uploadButton = findViewById<FloatingActionButton>(R.id.upload_button)
        uploadButton.setOnClickListener {
            selectMediaToUpload()
        }
    }

    private fun openFullScreenMedia(mediaUrl: String) {
        val intent = Intent(this, FullScreenImageActivity::class.java)
        intent.putExtra("imageUrl", mediaUrl)  // Pass the image URL
        startActivity(intent)
    }

    private fun selectMediaToUpload() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val fileUri = data?.data ?: return
            uploadMediaToFirebase(fileUri) { uploadedUrl ->
                if (uploadedUrl != null) {
                    Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
                    mediaList.add(uploadedUrl) // Add to local list
                    adapter.notifyDataSetChanged() // Refresh the RecyclerView
                } else {
                    Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show()
                }
                // Hide ProgressBar after upload is complete
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun uploadMediaToFirebase(fileUri: Uri, onComplete: (String?) -> Unit) {
        // Show ProgressBar while uploading
        progressBar.visibility = View.VISIBLE

        // Use schoolId (sch1) as part of the file path
        val storageReference = FirebaseStorage.getInstance().reference.child("school_media/$schoolId/${fileUri.lastPathSegment}")
        val uploadTask = storageReference.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                // Save the URL to Firebase Realtime Database under school_gallery/sch1
                val databaseReference = FirebaseDatabase.getInstance().getReference("school_gallery/$schoolId")
                val key = databaseReference.push().key ?: return@addOnSuccessListener
                databaseReference.child(key).setValue(downloadUrl.toString())
                onComplete(downloadUrl.toString())
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            onComplete(null)
        }
    }

    private fun fetchMediaFromFirebase() {
        // Show ProgressBar while fetching data
        progressBar.visibility = View.VISIBLE

        // Fetch media URLs from Firebase Realtime Database for the hardcoded schoolId
        val databaseReference = FirebaseDatabase.getInstance().getReference("school_gallery/$schoolId")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mediaList.clear()
                for (mediaSnapshot in snapshot.children) {
                    val mediaUrl = mediaSnapshot.getValue(String::class.java)
                    if (mediaUrl != null) {
                        mediaList.add(mediaUrl)
                    }
                }
                adapter.notifyDataSetChanged() // Refresh the RecyclerView
                // Hide ProgressBar after data is loaded
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
                // Hide ProgressBar on failure
                progressBar.visibility = View.GONE
            }
        })
    }
}
