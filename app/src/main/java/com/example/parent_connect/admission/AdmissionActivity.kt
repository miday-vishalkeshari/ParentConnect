package com.example.parent_connect.admission

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.example.parent_connect.schoolgalary.FullScreenImageActivity
import com.example.parent_connect.schoolgalary.MediaAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import java.net.URLDecoder

class AdmissionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdmissionAdapter
    private val sampleOffers = mutableListOf<AdmissionOffer>()

    private val ADD_OFFER_REQUEST_CODE = 1
    private val NEW_ADMISSION_REQUEST_CODE = 2

    private val firestore = FirebaseFirestore.getInstance()

    // Delete an offer from Firestore and update the list
    private var isDeleting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admission)

        recyclerView = findViewById(R.id.recyclerViewAdmissions)

        // Get role from intent
        val role = intent.getStringExtra("USER_ROLE") // "parent" or "schoolAdmin"

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter after fetching the role
        adapter = AdmissionAdapter(sampleOffers, ::deleteOfferFromFirestore, role ?: "parent") { imageUrl ->
            openFullScreenImage(imageUrl)
        }
        recyclerView.adapter = adapter

        // Fetch offers from Firestore
        fetchOffersFromFirestore()

        // Role-based visibility of buttons
        val btnAddAdmissionOffer: View = findViewById(R.id.btnAddAdmissionOffer)
        val btnAddNewAdmission: View = findViewById(R.id.btnNewStudentAdmission)


        //////////////
        // Set up the adapter for displaying media
//        adapter = MediaAdapter(this, mediaList) { mediaUrl ->
//            openFullScreenMedia(mediaUrl)
//        }
//        recyclerView.adapter = adapter

        /////////////


        if (role == "SchoolAdmin") {
            btnAddAdmissionOffer.visibility = View.VISIBLE
            btnAddNewAdmission.visibility = View.VISIBLE
            btnAddAdmissionOffer.setOnClickListener {
                val intent = Intent(this, AddOfferActivity::class.java)
                startActivityForResult(intent, ADD_OFFER_REQUEST_CODE)
            }
            btnAddNewAdmission.setOnClickListener {
                val intent = Intent(this, NewAdmissionActivity::class.java)
                startActivityForResult(intent, NEW_ADMISSION_REQUEST_CODE)
            }
        } else {
            btnAddAdmissionOffer.visibility = View.GONE
            btnAddNewAdmission.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // Fetch the latest offers when the activity is resumed
        fetchOffersFromFirestore()
    }

    private fun openFullScreenImage(imageUrl: String) {
        val intent = Intent(this, FullScreenImageActivity::class.java)
        intent.putExtra("imageUrl", imageUrl)
        startActivity(intent)
    }


    // Fetch offers from Firestore
    private fun fetchOffersFromFirestore() {
        val db = firestore.collection("Schools").document("sch1").collection("admission_offers")

        db.get().addOnSuccessListener { result ->
            sampleOffers.clear()  // Clear the previous offers before adding new ones
            for (document in result) {
                val title = document.getString("title") ?: "No title"
                val description = document.getString("description") ?: "No description"
                val imgLink = document.getString("imageUrl") ?: ""

                // Create a new offer and add it to the list
                val newOffer = AdmissionOffer(title, description, imgLink)
                sampleOffers.add(newOffer)
            }
            adapter.notifyDataSetChanged() // Notify adapter to update UI
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error fetching offers: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Save a new offer to Firestore
    private fun saveOfferToFirestore(offer: AdmissionOffer) {
        val db = firestore.collection("Schools")
            .document("sch1")
            .collection("admission_offers")

        // Create a new document with a unique ID
        val offerData = hashMapOf(
            "title" to offer.title,
            "description" to offer.description,
            "imageUrl" to offer.imgLink
        )

        db.add(offerData)
            .addOnSuccessListener {
                Toast.makeText(this, "Offer saved to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error saving offer: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }





    private fun deleteOfferFromFirestore(position: Int) {
        if (isDeleting) {
            Toast.makeText(this, "Please wait, deletion in progress", Toast.LENGTH_SHORT).show()
            return
        }

        isDeleting = true
        val offer = sampleOffers[position]
        val db = firestore.collection("Schools")
            .document("sch1")
            .collection("admission_offers")

        // Get the image URL from the offer
        val imageUrl = offer.imgLink

        // Extract the file path from the image URL and decode it
        val filePath = URLDecoder.decode(
            imageUrl.substringAfter("firebasestorage.googleapis.com/v0/b/parentconnect-2754d.firebasestorage.app/o/")
                .substringBefore("?alt=media"),
            "UTF-8"
        )

        // Delete the document from Firestore first
        db.whereEqualTo("title", offer.title)
            .whereEqualTo("description", offer.description)
            .whereEqualTo("imageUrl", offer.imgLink)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Delete the document from Firestore
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Now delete the image from Firebase Storage (if exists)
                            if (filePath.isNotEmpty()) {
                                // Construct the storage reference with the correct path
                                val storageRef = FirebaseStorage.getInstance().reference
                                    .child(filePath)

                                storageRef.delete()
                                    .addOnSuccessListener {
                                        // Remove from the local list and update the UI
                                        sampleOffers.removeAt(position)
                                        adapter.notifyItemRemoved(position)
                                        Toast.makeText(this, "Offer and image deleted", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Error deleting image: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // No image URL, just remove from the list
                                sampleOffers.removeAt(position)
                                adapter.notifyItemRemoved(position)
                                Toast.makeText(this, "Offer deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error deleting offer: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener {
                            isDeleting = false  // Reset the flag once deletion is complete
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching offer for deletion: ${exception.message}", Toast.LENGTH_SHORT).show()
                isDeleting = false  // Reset the flag in case of failure
            }
    }






    // Handle the result from AddOfferActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_OFFER_REQUEST_CODE && resultCode == RESULT_OK) {
            val title = data?.getStringExtra("NEW_OFFER_TITLE")
            val description = data?.getStringExtra("NEW_OFFER_DESCRIPTION")
            val imgLink = data?.getStringExtra("NEW_OFFER_IMGLINK") ?: ""

            if (title != null && description != null) {
                // Create a new offer object
                val newOffer = AdmissionOffer(title, description, imgLink)

                // Save to Firestore
                saveOfferToFirestore(newOffer)

                // Update the local list and notify the adapter
                sampleOffers.add(newOffer)
                adapter.notifyItemInserted(sampleOffers.size - 1)

                Toast.makeText(this, "New Offer Added", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
