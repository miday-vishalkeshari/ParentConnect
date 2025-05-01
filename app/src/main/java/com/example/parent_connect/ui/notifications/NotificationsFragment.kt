package com.example.parent_connect.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parent_connect.R
import com.example.parent_connect.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationsList = listOf(
        Notification(
            title = "New Event Scheduled",
            message = "The Science Fair event has been scheduled for 12th March 2024.",
            time = "1:00 PM",
            schoolName = "Jane Smith - School B"
        ),
        Notification(
            title = "Fee Payment Reminder",
            message = "Please pay your fees before 15th March to avoid late fees.",
            time = "3:00 PM",
            schoolName = "John Doe - School A"
        ),
        Notification(
            title = "Parent-Teacher Meeting Reminder",
            message = "The parent-teacher meeting is scheduled for 18th March 2024.",
            time = "9:00 AM",
            schoolName = "Jane Smith - School B"
        ),
        Notification(
            title = "Holiday Announcement",
            message = "The school will remain closed on 25th December 2024 for Christmas holidays.",
            time = "5:00 PM",
            schoolName = "John Doe - School A"
        ),
        Notification(
            title = "New Admission Guidelines",
            message = "Please check the new admission guidelines for the academic year 2024-2025.",
            time = "2:00 PM",
            schoolName = "Jane Smith - School B"
        ),
        Notification(
            title = "Exam Schedule Update",
            message = "The exam schedule has been updated. Please check the notice board for details.",
            time = "4:00 PM",
            schoolName = "John Doe - School A"
        ),
        Notification(
            title = "Sports Day Results",
            message = "Congratulations to the winners of the Sports Day! Check the official website for details.",
            time = "6:00 PM",
            schoolName = "Jane Smith - School B"
        ),
        Notification(
            title = "Library Book Return Reminder",
            message = "Please return your library books by 30th March to avoid fines.",
            time = "10:00 AM",
            schoolName = "John Doe - School A"
        ),
        Notification(
            title = "Workshop on Career Development",
            message = "A workshop on career development will be held on 5th April. Don't miss it!",
            time = "11:00 AM",
            schoolName = "Jane Smith - School B"
        ),
        Notification(
            title = "New Sports Club Formation",
            message = "A new sports club is being formed. Interested students can sign up by next week.",
            time = "7:00 PM",
            schoolName = "John Doe - School A"
        )
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerViewNotifications
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = NotificationAdapter(notificationsList)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
