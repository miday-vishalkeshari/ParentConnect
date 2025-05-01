package com.example.parent_connect.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.example.parent_connect.R
import com.example.parent_connect.aboutschool.AboutSchoolActivity
import com.example.parent_connect.admission.AdmissionActivity
import com.example.parent_connect.attendance.AttendanceActivity
import com.example.parent_connect.contactschool.ContactSchoolActivity
import com.example.parent_connect.databinding.FragmentHomeBinding
import com.example.parent_connect.event.EventsActivity
import com.example.parent_connect.fees.FeesActivity
import com.example.parent_connect.food.FoodActivity
import com.example.parent_connect.holidays.HolidaysActivity
import com.example.parent_connect.homework.HomeworkActivity
import com.example.parent_connect.noticeboard.NoticeBoardActivity
import com.example.parent_connect.ptm.PTMActivity
import com.example.parent_connect.reportcard.ReportCardActivity
import com.example.parent_connect.schoolgalary.SchoolGalaryActivity
import com.example.parent_connect.timetable.TimetableActivity
import com.example.parent_connect.RoleViewModel
import com.example.parent_connect.teachers_and_students.teacherschedule.TeacherStudentTeacherscheduleActivity
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.parent_connect.bustracking.BusTrackingActivity
import com.example.parent_connect.settings.SettingsActivity


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var goodMorningText: TextView
    private lateinit var dateAndDay: TextView
    private lateinit var schoolName: TextView
    private lateinit var role: String  // Class-level role variable

    private lateinit var roleViewModel: RoleViewModel
    private var userName: String? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get the ViewModel from the activity
        roleViewModel = ViewModelProvider(requireActivity()).get(RoleViewModel::class.java)
        role = roleViewModel.role ?: "defaultRole"  // This ensures role is initialized
        Log.d("HomeFragment", "Received Role: $role")

        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Inflate the fragment's layout using ViewBinding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Find views using binding instead of rootView
        goodMorningText = binding.goodMorningText
        dateAndDay = binding.dateAndDay
        schoolName = binding.schoolName

        // Configure UI based on role
        configureUIBasedOnRole(role)


        // Set the listener for the settings button
        binding.settingsButton.setOnClickListener {
            // Create an Intent to start the SettingsActivity
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        // Set click listeners for common CardViews
//        setCardViewClickListener(binding, R.id.cardViewNotifications, null) {
//            findNavController().navigate(R.id.navigation_notifications)
//        }
//        setCardViewClickListener(binding, R.id.cardViewMessages, null) {
//            findNavController().navigate(R.id.navigation_messages)
//        }

        // Set date and time
        setDateAndTime()

        // Fetch the user name from Firestore
        fetchUserNameFromFirestore()

        // Observe LiveData from HomeViewModel
        homeViewModel.text.observe(viewLifecycleOwner) {}

        return binding.root
    }




    private fun fetchUserNameFromFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser  // Get the current user
        if (currentUser != null) {
            val userId = currentUser.uid  // Get the userId (UID of the current user)

            // Fetch user data from Firestore
            firestore.collection("users")  // Reference to the "users" collection
                .document(userId)  // Use the user's UID to get the corresponding document
                .get()  // Perform the get operation to fetch the document
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Fetch the 'name' field from the document
                        userName = document.getString("name") ?: "DefaultUser"  // Provide a default if the name is null
                        Log.d("Firestore", "User name: $userName")
                        // Now update the UI after fetching the name
                        userName?.let {
                            updateGreetingBasedOnTimeAndName(it)
                        }
                    } else {
                        Log.d("Firestore", "No such user found.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting user data: ", exception)
                }
        } else {
            Log.e("Firestore", "User is not logged in")
        }
    }



    private fun setDateAndTime() {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.getDefault())
        val formattedDate = dateFormat.format(currentTime)

        // Update the dateAndDay TextView with the formatted date
        dateAndDay.text = formattedDate
    }

    private fun updateGreetingBasedOnTimeAndName(userName: String) {
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingText = when {
            hourOfDay in 6..11 -> "Good Morning, $userName!"
            hourOfDay in 12..17 -> "Good Afternoon, $userName!"
            else -> "Good Evening, $userName!"
        }
        Log.e("HomeFragment", "User name $userName")
        goodMorningText.text = greetingText
    }

    private fun configureUIBasedOnRole(role: String?) {
        hideAllCardViews()

        when (role) {
            "Parent" -> showParentUI()
            "Teacher" -> showTeacherUI()
            "SchoolAdmin" -> showAdminUI()
            else -> Log.d("HomeFragment", "Role not recognized")
        }

        setupClickListeners()
    }

    private fun hideAllCardViews() {
        val cardViewIds = listOf(
            R.id.cardViewAttendance, R.id.cardViewNoticeBoard, R.id.cardViewBusTracking,
            R.id.cardViewHomework, R.id.cardViewTimetable, R.id.cardViewEvents,
            R.id.cardViewFees, R.id.cardViewContactSchool, R.id.cardViewHolidays,
            R.id.cardViewSchoolGalary, R.id.cardViewReportCard, R.id.cardViewPtm,
            R.id.cardViewTeachers, R.id.cardViewAdmission, R.id.cardViewFood,
            R.id.cardViewAboutSchool, R.id.cardViewNotifications, R.id.cardViewMessages
        )

        cardViewIds.forEach { id ->
            binding.root.findViewById<CardView>(id)?.visibility = View.GONE
        }
    }

    private fun showParentUI() {
        val parentCardViewIds = listOf(
            R.id.cardViewAttendance, R.id.cardViewReportCard, R.id.cardViewPtm,
            R.id.cardViewHomework, R.id.cardViewNoticeBoard, R.id.cardViewBusTracking,
            R.id.cardViewTimetable, R.id.cardViewEvents,
             R.id.cardViewContactSchool,
            R.id.cardViewSchoolGalary, R.id.cardViewAdmission,
             R.id.cardViewHolidays
            //R.id.cardViewAboutSchool, R.id.cardViewFees, R.id.cardViewFood,
        )

        parentCardViewIds.forEach { id -> showCardView(id) }
    }

    private fun showTeacherUI() {
        val teacherCardViewIds = listOf(
            R.id.cardViewAttendance, R.id.cardViewReportCard,
            R.id.cardViewNoticeBoard, R.id.cardViewHomework, R.id.cardViewHolidays, R.id.cardViewEvents,
            R.id.cardViewSchoolGalary, R.id.cardViewPtm,R.id.cardViewAdmission, R.id.cardViewContactSchool
        )

        teacherCardViewIds.forEach { id -> showCardView(id) }
    }

    private fun showAdminUI() {
        val adminCardViewIds = listOf(
            R.id.cardViewAdmission, R.id.cardViewNoticeBoard, R.id.cardViewEvents,R.id.cardViewReportCard,
            R.id.cardViewContactSchool, R.id.cardViewTeachers
            //, R.id.cardViewAboutSchool
        )

        adminCardViewIds.forEach { id -> showCardView(id) }
    }

    private fun showCardView(cardViewId: Int) {
        binding.root.findViewById<CardView>(cardViewId)?.visibility = View.VISIBLE
    }

    private fun setupClickListeners() {
        setCardViewClickListener(binding, R.id.cardViewAttendance, AttendanceActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewNoticeBoard, NoticeBoardActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewBusTracking, BusTrackingActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewHomework, HomeworkActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewTimetable, TimetableActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewEvents, EventsActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewFees, FeesActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewContactSchool, ContactSchoolActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewHolidays, HolidaysActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewSchoolGalary, SchoolGalaryActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewReportCard, ReportCardActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewPtm, PTMActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewTeachers, TeacherStudentTeacherscheduleActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewAdmission, AdmissionActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewFood, FoodActivity::class.java)
        setCardViewClickListener(binding, R.id.cardViewAboutSchool, AboutSchoolActivity::class.java)
    }



    private fun setCardViewClickListener(
        binding: ViewBinding,
        cardViewId: Int,
        targetActivity: Class<*>? = null
    ) {
        val cardView: CardView = binding.root.findViewById(cardViewId)
        cardView.setOnClickListener {
            if (targetActivity != null) {
                val intent = Intent(activity, targetActivity)
                intent.putExtra("USER_ROLE", role)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
