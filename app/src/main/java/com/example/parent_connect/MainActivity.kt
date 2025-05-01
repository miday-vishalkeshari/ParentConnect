package com.example.parent_connect

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.parent_connect.databinding.ActivityMainBinding
import com.example.parent_connect.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var roleViewModel: RoleViewModel

    private lateinit var bottomNavigationView: BottomNavigationView
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        roleViewModel = ViewModelProvider(this).get(RoleViewModel::class.java)

        // Get BottomNavigationView and NavController
        bottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Setup BottomNavigationView with NavController
        bottomNavigationView.setupWithNavController(navController)

        // Retrieve role from intent and save it in ViewModel
        val role = intent.getStringExtra("USER_ROLE")
        Log.d("MainActivity", "Received Role: $role")
        roleViewModel.role = role

        if (savedInstanceState == null) {
            val bundle = Bundle().apply {
                putString("USER_ROLE", role)
            }
            navController.navigate(R.id.navigation_home, bundle)
        }

        // Fetch role from Firebase if needed
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            fetchUserRole(userId)
        }

        // Handle navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val bundle = Bundle().apply {
                        putString("USER_ROLE", roleViewModel.role)
                    }
                    navController.navigate(R.id.navigation_home, bundle)
                    true
                }
                else -> {
                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
                    val navController = navHostFragment.navController
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Check if the current destination is the start destination (e.g., HomeFragment)
        if (navController.currentDestination?.id == R.id.navigation_home) {
            // If we're on the HomeFragment, exit the app
            finishAffinity() // This exits the app completely
        } else {
            // Otherwise, let NavController handle the back press
            super.onBackPressed()
        }
    }


    // Function to fetch user role from Firebase Firestore
    private fun fetchUserRole(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userRole = document.getString("role")  // Fetch role field from Firestore
                    roleViewModel.role = userRole
                    // Update bottom navigation titles based on the role
                    updateBottomNavMenuTitle()
                }
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Failed to fetch user role.")
            }
    }

    // Function to update the title of the BottomNavigationView menu items based on user role
    private fun updateBottomNavMenuTitle() {
        // Access the menu items of BottomNavigationView
        val menu = bottomNavigationView.menu
        val menuItem = menu.findItem(R.id.navigation_wards)

        // Update the title based on user role
        when (userRole) {
            "Parent" -> {
                menuItem.title = "My Wards"  // Title for Parent role
                menuItem.setIcon(R.drawable.wards_icon)
            }
            "Teacher" -> {
                menuItem.title = "My Classes"  // Title for Teacher role
                menuItem.setIcon(R.drawable.teacher_class_schedule_icon)
            }
            else -> {
                menuItem.title = "All Classes"  // Default title
                menuItem.setIcon(R.drawable.teacher_class_schedule_icon)
            }
        }
    }
}
