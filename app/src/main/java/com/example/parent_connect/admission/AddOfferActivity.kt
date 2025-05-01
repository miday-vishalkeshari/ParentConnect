package com.example.parent_connect.admission

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class AddOfferActivity : AppCompatActivity() {

    private lateinit var editTextOfferTitle: EditText
    private lateinit var editTextOfferDescription: EditText
    private lateinit var btnSaveOffer: Button
    private lateinit var btnSelectImage: Button
    private lateinit var imageViewSelected: ImageView

    private lateinit var progressBar: ProgressBar

    private val imagePickRequestCode = 1000  // Request code for image selection
    private var imageUri: Uri? = null  // Selected image URI

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_offer)

        // Initialize views
        editTextOfferTitle = findViewById(R.id.editTextOfferTitle)
        editTextOfferDescription = findViewById(R.id.editTextOfferDescription)
        btnSaveOffer = findViewById(R.id.btnSaveOffer)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        imageViewSelected = findViewById(R.id.imageViewSelected)
        progressBar = findViewById(R.id.progressBar)

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        // Set click listener for Save Offer Button
        btnSaveOffer.setOnClickListener {
            val title = editTextOfferTitle.text.toString()
            val description = editTextOfferDescription.text.toString()

            if (title.isNotBlank() && description.isNotBlank()) {
                // Save offer details to Firestore with image if selected
                progressBar.visibility = View.VISIBLE

                saveOfferToFirestore(title, description)
            } else {
                // Show error message
                editTextOfferTitle.error = "Title is required"
                editTextOfferDescription.error = "Description is required"
            }
        }

        // Set click listener for Select Image Button
        btnSelectImage.setOnClickListener {
            // Open the gallery to choose an image
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, imagePickRequestCode)
        }
    }

    // Handle the image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == imagePickRequestCode && resultCode == RESULT_OK) {
            imageUri = data?.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageViewSelected.setImageBitmap(bitmap)  // Display the selected image
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Save offer details to Firestore and upload image to Firebase Storage
    private fun saveOfferToFirestore(title: String, description: String) {
        // If an image is selected, upload it to Firebase Storage
        if (imageUri != null) {
            uploadImageToFirebase(imageUri!!, title, description)
        } else {
            // Save offer data without image
            saveOfferDataToFirestore(title, description, null)
        }
    }

    // Upload image to Firebase Storage and save the download URL to Firestore
    private fun uploadImageToFirebase(uri: Uri, title: String, description: String) {
        val fileReference = storageReference.child("school_media/sch1/admission_offers/${uri.lastPathSegment}")

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Upload the image to Firebase Storage
        val uploadTask = fileReference.putBytes(data)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // After successful upload, get the image's download URL
            fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                // Save the offer data (with image URL) to Firestore
                saveOfferDataToFirestore(title, description, downloadUri.toString())
            }
        }.addOnFailureListener { e ->
            // Handle errors if the upload fails
            Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Save offer data to Firestore
    private fun saveOfferDataToFirestore(title: String, description: String, imageUrl: String?) {
        val offerData = hashMapOf(
            "title" to title,
            "description" to description
        )

        // Include imageUrl if available
        imageUrl?.let {
            offerData["imageUrl"] = it
        }

        // Save the offer data under /Schools/sch1/admission_offers path
        firestore.collection("Schools")
            .document("sch1")
            .collection("admission_offers")
            .add(offerData)
            .addOnSuccessListener {
                Toast.makeText(this, "Offer saved successfully!", Toast.LENGTH_SHORT).show()
                finish()  // Optionally close the activity after saving
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save offer: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
