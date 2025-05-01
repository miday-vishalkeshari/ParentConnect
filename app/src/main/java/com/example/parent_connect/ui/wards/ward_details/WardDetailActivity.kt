package com.example.parent_connect.ui.wards.ward_details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.databinding.ActivityWardDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.example.parent_connect.R

class WardDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWardDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1001 // Request code for image selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Retrieve the data passed from the previous activity/fragment
        val wardName = intent.getStringExtra("WARD_NAME") ?: "N/A"
        val schoolName = intent.getStringExtra("SCHOOL_NAME") ?: "N/A"
        val wardClass = intent.getStringExtra("WARD_CLASS") ?: "N/A"
        val admissionNumber = intent.getStringExtra("WARD_ADMISSION_NO") ?: "N/A"

        // Call the fetchWardDetails function to get details from Firestore
        fetchWardDetails(schoolName, admissionNumber)

        // Display the data in the respective views
        binding.textViewFullName.text = wardName
        binding.textViewClassGrade.text = wardClass
        binding.textViewAdmissionNo.text = admissionNumber

        // Set the click listener for the ImageView
        binding.imageViewWardProfile.setOnClickListener {
            // Open image picker to select a new profile image
            openImagePicker()
        }
    }

    // Method to open the gallery for image selection
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // Set the MIME type for image selection
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun fetchWardDetails(schoolName: String, admissionNo: String) {
        // Construct the document path dynamically
        val docRef = db.collection("Schools")
            .document(schoolName)
            .collection("Students")
            .document(admissionNo)

        // Fetch the data from Firestore
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Map the Firestore document fields to the views
                    binding.textViewRollNumber.text = document.getString("rollNumber") ?: "N/A"
                    binding.textViewSection.text = document.getString("section") ?: "N/A"
                    binding.textViewDOB.text = document.getString("DOB") ?: "N/A"
                    binding.textViewGender.text = document.getString("gender") ?: "N/A"
                    binding.textViewBloodGroup.text = document.getString("BloodGroup") ?: "N/A"
                    binding.textViewParentGuardianNames.text = document.getString("parentGuardianNames") ?: "N/A"
                    binding.textViewEmergencyContactNumbers.text = document.getString("emergencyContact") ?: "N/A"
                    binding.textViewHomeAddress.text = document.getString("homeAddress") ?: "N/A"
                    binding.textViewEmailAddress.text = document.getString("emailAddress") ?: "N/A"
                    binding.textViewClubsSocieties.text = document.getString("clubsAndSocieties") ?: "N/A"
                    binding.textViewSportsParticipation.text = document.getString("sportsParticipation") ?: "N/A"
                    binding.textViewAchievementsAwards.text = document.getString("achievements") ?: "N/A"
                    binding.textViewSchoolEventsParticipation.text = document.getString("schoolEventsParticipation") ?: "N/A"
                    binding.textViewFeePaymentHistory.text = document.getString("feePaymentHistory") ?: "N/A"
                    binding.textViewOutstandingFees.text = document.getString("outstandingFees") ?: "N/A"
                    binding.textViewScholarshipDetails.text = document.getString("scholarshipDetails") ?: "N/A"

                    // Fetch and display the profile image
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    Log.d("WardDetailActivity", "profileImageUrl $profileImageUrl")
                    if (profileImageUrl.isNotEmpty()) {
                        loadProfileImage(profileImageUrl)
                    }
                } else {
                    Log.d("WardDetailActivity", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("WardDetailActivity", "Error getting document: ", exception)
            }
    }

    private fun loadProfileImage(imageUrl: String) {
        // Use Picasso to load the image into the ImageView
        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.ward_dp) // Default image while loading
            .error(R.drawable.ic_poster_placeholder) // Image in case of error
            .into(binding.imageViewWardProfile) // Assuming your ImageView has the ID imageViewWardProfile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data

            // Upload the selected image to Firebase Storage
            selectedImageUri?.let { uri ->
                uploadProfileImage(uri)
            }
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        // Create a reference to Firebase Storage
        val storageRef = storage.reference.child("profile_images/${System.currentTimeMillis()}.jpg")

        // Upload the image to Firebase Storage
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Get the download URL of the uploaded image
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    updateProfileImageInFirestore(imageUrl)
                    // Update the ImageView with the new image
                    loadProfileImage(imageUrl)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfileImageInFirestore(imageUrl: String) {
        // Retrieve the data passed to the activity
        val schoolName = intent.getStringExtra("SCHOOL_NAME") ?: ""
        val admissionNumber = intent.getStringExtra("WARD_ADMISSION_NO") ?: ""

        // Construct the document path dynamically
        val docRef = db.collection("Schools")
            .document(schoolName)
            .collection("Students")
            .document(admissionNumber)

        // Update the Firestore document with the new profile image URL
        docRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Log.d("WardDetailActivity", "Profile image updated successfully.")
            }
            .addOnFailureListener { exception ->
                Log.w("WardDetailActivity", "Error updating profile image: ", exception)
            }
    }
}
