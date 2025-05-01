package com.example.parent_connect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.databinding.ActivityRoleSelectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val credentials = mapOf(
        "Parent" to Pair("parent@example.com", "parent123"),
        "Teacher" to Pair("teacher@example.com", "teacher123"),
        "SchoolAdmin" to Pair("admin@example.com", "admin123")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardRoleParent.setOnClickListener {
            loginUser("Parent")
        }

        binding.cardRoleTeacher.setOnClickListener {
            loginUser("Teacher")
        }

        binding.cardRoleSchoolAdmin.setOnClickListener {
            loginUser("SchoolAdmin")
        }

    }

    private fun loginUser(role: String) {
        val (email, password) = credentials[role] ?: return

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        checkAndNavigate(role, currentUser)
                    }
                } else {
                    Toast.makeText(this, "Login failed. Registering new user...", Toast.LENGTH_SHORT).show()
                    registerUser(email, password, role)
                }
            }
    }

    private fun registerUser(email: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        addUserToFirestore(role, currentUser)
                    }
                } else {
                    Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndNavigate(role: String, currentUser: FirebaseUser) {
        val userRef = firestore.collection("users").document(currentUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // User already exists, just navigate
                navigateToHome(role)
            } else {
                // User doesn't exist, add to Firestore
                addUserToFirestore(role, currentUser)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error checking user data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUserToFirestore(role: String, currentUser: FirebaseUser) {
        val userRef = firestore.collection("users").document(currentUser.uid)
        val userData = hashMapOf(
            "role" to role,
            "email" to currentUser.email
        )

        userRef.set(userData).addOnSuccessListener {
            navigateToHome(role)
        }.addOnFailureListener {
            Toast.makeText(this, "Error saving user data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHome(role: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_ROLE", role)
        startActivity(intent)
        finish()
    }
}
