package com.example.parent_connect.noticeboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class NoticeBoardActivity : AppCompatActivity() {

    private lateinit var noticeRecyclerView: RecyclerView
    private lateinit var noticeAdapter: NoticeAdapter
    private lateinit var noticeList: MutableList<Notice>
    private lateinit var database: DatabaseReference

    private val ADD_NOTICE_REQUEST_CODE = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_board)

        noticeRecyclerView = findViewById(R.id.noticeRecyclerView)
        noticeRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Initialize the notice list
        noticeList = mutableListOf()

        // Get the user role from the intent
        val role = intent.getStringExtra("USER_ROLE") ?: ""

        // Initialize the adapter with the user role
        noticeAdapter = NoticeAdapter(noticeList, role) { notice ->
            //deleteNoticeFromFirebase(notice)
            deleteNoticeFromFirestore(notice)
        }
        noticeRecyclerView.adapter = noticeAdapter

        // Fetch notices from Firebase when the activity starts
        //fetchNoticesFromFirebase()///////////////////////////////////////////////////////////////////////////////


        // Check if the user is a School Admin
        //val role = intent.getStringExtra("USER_ROLE")
        Log.d("NoticeBoardActivity", "User Role from Intent: $role")

        if (role == "SchoolAdmin") {
            // Show the "Add New Notice" button if the user is School Admin
            val addNoticeButton = findViewById<View>(R.id.addNoticeButton)
            addNoticeButton.visibility = View.VISIBLE
            Log.d("NoticeBoardActivity", "Role is SchoolAdmin, Add Notice Button is visible")

            fetchSchoolIdAndNoticesFromFirestore()

            // Handle add new notice button click
            addNoticeButton.setOnClickListener {
                fetchSchoolIdAndProceed()
            }
        }
        else if(role=="Parent")
        {
            //yaha i have to get notices for all wards of that parent
            //and for that i have to fetch schoolid from all wards
            fetchSchoolIdsAndNoticesForAllWards()
        }
        else {
            // Hide the "Add New Notice" button if the user is not School Admin
//            val addNoticeButton = findViewById<View>(R.id.addNoticeButton)
//            addNoticeButton.visibility = View.GONE
            Log.d("NoticeBoardActivity", "Role is not SchoolAdmin, Add Notice Button is hidden")
            fetchSchoolIdAndNoticesFromFirestore()
        }
    }


    ///=======================================================================================================
    private fun fetchSchoolIdAndNoticesFromFirestore() {
        Log.d("NoticeBoardActivity", "Fetching school ID and notices from Firestore...")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get the logged-in user's ID
            Log.d("NoticeBoardActivity", "User ID: $userId")

            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(userId)

            // Fetch the schoolId directly from the user's document
            userDocRef.get()
                .addOnSuccessListener { userDocument ->
                    if (userDocument.exists()) {
                        val schoolId = userDocument.getString("schoolId") // Fetch schoolId
                        if (schoolId != null) {
                            Log.d("NoticeBoardActivity", "School ID: $schoolId")
                            fetchNoticesFromSchool(schoolId) // Fetch notices for this schoolId
                        } else {
                            Log.e("NoticeBoardActivity", "No schoolId found for user")
                            Toast.makeText(this@NoticeBoardActivity, "School ID not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("NoticeBoardActivity", "User document not found")
                        Toast.makeText(this@NoticeBoardActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("NoticeBoardActivity", "Failed to fetch user: ${exception.message}")
                    Toast.makeText(this@NoticeBoardActivity, "Failed to fetch user", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("NoticeBoardActivity", "No user logged in")
            Toast.makeText(this@NoticeBoardActivity, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchNoticesFromSchool(schoolId: String) {
        Log.d("NoticeBoardActivity", "Fetching notices from Firestore for school ID: $schoolId")

        val firestore = FirebaseFirestore.getInstance()
        val noticesCollection = firestore.collection("Schools")
            .document(schoolId)
            .collection("notices")

        noticesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("NoticeBoardActivity", "Failed to load notices: ${error.message}")
                Toast.makeText(this@NoticeBoardActivity, "Failed to load notices", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                Log.d("NoticeBoardActivity", "Notices fetched successfully for school ID: $schoolId")


                // Get the user role from the intent
                val role = intent.getStringExtra("USER_ROLE") ?: ""

                if (role == "SchoolAdmin")
                {
                    // Clear the list before adding new notices to avoid duplicates
                    noticeList.clear()
                }

                // Append notices to the list instead of clearing
                for (document in snapshot.documents) {
                    val notice = document.toObject(Notice::class.java)
                    notice?.let {
                        noticeList.add(it) // Accumulate notices
                        Log.d("NoticeBoardActivity", "Notice: ${it.title} - ${it.description}")
                    }
                }

                // Notify the adapter that the data has changed
                noticeAdapter.notifyDataSetChanged()
            } else {
                Log.d("NoticeBoardActivity", "No notices found for school ID: $schoolId")
                Toast.makeText(this@NoticeBoardActivity, "No notices found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //............................
    private fun fetchSchoolIdsAndNoticesForAllWards() {
        Log.d("NoticeBoardActivity", "Fetching school IDs and notices for all wards from Firestore...")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid  // Get the logged-in user's ID
            Log.d("NoticeBoardActivity", "User ID: $userId")

            val firestore = FirebaseFirestore.getInstance()
            val wardsCollectionRef = firestore.collection("users").document(userId).collection("Wards")

            // Fetch all wards for the parent
            wardsCollectionRef.get().addOnSuccessListener { wardsSnapshot ->
                if (wardsSnapshot != null && !wardsSnapshot.isEmpty) {
                    Log.d("NoticeBoardActivity", "Wards found for user ID: $userId")

                    for (wardDocument in wardsSnapshot.documents) {
                        val schoolId = wardDocument.getString("schoolId")
                        val wardId = wardDocument.id

                        if (schoolId != null) {
                            Log.d("NoticeBoardActivity", "Ward ID: $wardId, School ID: $schoolId")
                            // Fetch notices for each ward's school ID
                            fetchNoticesFromSchool(schoolId)
                        } else {
                            Log.e("NoticeBoardActivity", "No schoolId found for ward: $wardId")
                        }
                    }
                } else {
                    Log.e("NoticeBoardActivity", "No wards found for user ID: $userId")
                    Toast.makeText(this@NoticeBoardActivity, "No wards found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("NoticeBoardActivity", "Failed to fetch wards: ${exception.message}")
                Toast.makeText(this@NoticeBoardActivity, "Failed to fetch wards", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("NoticeBoardActivity", "No user logged in")
            Toast.makeText(this@NoticeBoardActivity, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }



    //----------------------------------------------------------------------------------


    // Function to push new notice to Firestore
    // Function to push a notice to Firestore
    private fun pushNoticeToFirestore(notice: Notice) {
        Log.d("NoticeBoardActivity", "Pushing new notice to Firestore...")

        // Get the current logged-in user's ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid  // Get the logged-in user's ID
            Log.d("NoticeBoardActivity", "User ID: $userId")

            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(userId)

            // Fetch the schoolId from the user's document
            userDocRef.get().addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val schoolId = userDocument.getString("schoolId")
                    if (schoolId != null) {
                        Log.d("NoticeBoardActivity", "School ID: $schoolId")
                        // Now pass the schoolId to push the notice to the correct school collection
                        pushNoticeToSchool(schoolId, notice)  // Corrected this line
                    } else {
                        Log.e("NoticeBoardActivity", "No schoolId found for user")
                        Toast.makeText(this@NoticeBoardActivity, "School ID not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("NoticeBoardActivity", "User document not found")
                    Toast.makeText(this@NoticeBoardActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("NoticeBoardActivity", "Failed to fetch user: ${exception.message}")
                Toast.makeText(this@NoticeBoardActivity, "Failed to fetch user", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("NoticeBoardActivity", "No user logged in")
            Toast.makeText(this@NoticeBoardActivity, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to push the notice to the specific school
    private fun pushNoticeToSchool(schoolId: String, notice: Notice) {
        Log.d("NoticeBoardActivity", "Pushing notice to Firestore for school ID: $schoolId")

        val firestore = FirebaseFirestore.getInstance()

        // Reference to the notices collection for the specific school
        val noticesCollection = firestore.collection("Schools")
            //.document(schoolId) // Specify the schoolId
            .document("sch1") // Specify the schoolId
            .collection("notices") // Access the notices subcollection

        // Generate a unique document ID for the notice
        val noticeId = noticesCollection.document().id

        // Add the notice to Firestore
        noticesCollection.document(noticeId).set(notice)
            .addOnSuccessListener {
                // Successfully added the notice to Firestore
                Log.d("NoticeBoardActivity", "Notice successfully added to Firestore")
                Toast.makeText(this, "Notice successfully added to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                // Failed to add the notice to Firestore
                Log.e("NoticeBoardActivity", "Failed to add notice to Firestore: ${error.message}")
                Toast.makeText(this, "Failed to add notice to Firestore", Toast.LENGTH_SHORT).show()
            }
    }


    //-----------------------------------------------------------------------

    private fun deleteNoticeFromFirestore(notice: Notice) {
        Log.d("NoticeBoardActivity", "Deleting notice from Firestore...")

        // Get the current logged-in user's ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid  // Get the logged-in user's ID
            Log.d("NoticeBoardActivity", "User ID: $userId")

            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(userId)

            // Fetch the schoolId from the user's document
            userDocRef.get().addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val schoolId = userDocument.getString("schoolId")
                    if (schoolId != null) {
                        Log.d("NoticeBoardActivity", "School ID: $schoolId")
                        // Now delete the notice from the specific school's notices collection
                        deleteNoticeFromSchool(schoolId, notice)
                    } else {
                        Log.e("NoticeBoardActivity", "No schoolId found for user")
                        Toast.makeText(this@NoticeBoardActivity, "School ID not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("NoticeBoardActivity", "User document not found")
                    Toast.makeText(this@NoticeBoardActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("NoticeBoardActivity", "Failed to fetch user: ${exception.message}")
                Toast.makeText(this@NoticeBoardActivity, "Failed to fetch user", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("NoticeBoardActivity", "No user logged in")
            Toast.makeText(this@NoticeBoardActivity, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNoticeFromSchool(schoolId: String, notice: Notice) {
        Log.d("NoticeBoardActivity", "Deleting notice from Firestore for school ID: $schoolId")

        val firestore = FirebaseFirestore.getInstance()

        // Reference to the notices collection for the specific school
        val noticesCollection = firestore.collection("Schools")
            .document(schoolId) // Specify the schoolId
            .collection("notices") // Access the notices subcollection

        // Query Firestore to find the document with the matching title
        noticesCollection.whereEqualTo("title", notice.title)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        // Delete the matching document
                        noticesCollection.document(document.id).delete()
                            .addOnSuccessListener {
                                Log.d("NoticeBoardActivity", "Notice successfully deleted from Firestore")
                                Toast.makeText(this, "Notice deleted", Toast.LENGTH_SHORT).show()
                                noticeList.remove(notice)
                                noticeAdapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { error ->
                                Log.e("NoticeBoardActivity", "Failed to delete notice: ${error.message}")
                                Toast.makeText(this, "Failed to delete notice", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Log.e("NoticeBoardActivity", "No matching notice found to delete")
                    Toast.makeText(this, "No matching notice found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Log.e("NoticeBoardActivity", "Error fetching notice: ${error.message}")
                Toast.makeText(this, "Error finding notice to delete", Toast.LENGTH_SHORT).show()
            }
    }

//============================================================================================
// Fetch schoolId from Firestore
private fun fetchSchoolIdAndProceed() {

    val currentUser = FirebaseAuth.getInstance().currentUser

    // Assuming the user information is stored in a `users` collection or similar

    if (currentUser != null) {

        val userId = currentUser.uid
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val schoolId = documentSnapshot.getString("schoolId")

                if (schoolId != null) {
                    // School ID fetched successfully, now pass it to AddNoticeActivity
                    val intent = Intent(this, AddNoticeActivity::class.java)
                    //intent.putExtra("schoolId", schoolId)  // Pass schoolId to AddNoticeActivity
                    startActivityForResult(intent, ADD_NOTICE_REQUEST_CODE)
                } else {
                    // Handle case where schoolId is not found or available
                    Toast.makeText(this, "School ID not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure in fetching user data
                Toast.makeText(this, "Failed to fetch school ID: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    else{
        Log.e("NoticeBoardActivity", "User id is null at fetchSchoolIdAndProceed")
    }

}


    // Handle the result from AddNoticeActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_NOTICE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the notice object from the result
            val newNotice = data?.getSerializableExtra("newNotice") as? Notice

            if (newNotice != null) {
                pushNoticeToFirestore(newNotice)
            }
        }
    }
}


