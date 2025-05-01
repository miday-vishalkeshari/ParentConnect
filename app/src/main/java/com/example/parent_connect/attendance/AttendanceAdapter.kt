package com.example.parent_connect.attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.example.parent_connect.databinding.ItemAttendanceBinding

class AttendanceAdapter(private val role: String) : ListAdapter<StudentAttendance, AttendanceAdapter.AttendanceViewHolder>(AttendanceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val studentAttendance = getItem(position)
        holder.bind(studentAttendance)
    }

    inner class AttendanceViewHolder(private val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(studentAttendance: StudentAttendance) {
            // Bind the student name and attendance icon
            binding.studentNameTextView.text = studentAttendance.studentName
            binding.attendanceIcon.setImageResource(
                if (studentAttendance.isPresent) R.drawable.attendance_checkbox_present else R.drawable.attendance_checkbox_absent
            )

            // Set up click listener to toggle present icon
            binding.attendanceIcon1.setOnClickListener {
                if( studentAttendance.isPresent)
                {
                    studentAttendance.isPresent = false
                    binding.attendanceIcon1.setImageResource(R.drawable.attendance_checkbox_absent)
                }
                else{
                    studentAttendance.isPresent = true // Mark as present
                    binding.attendanceIcon1.setImageResource(R.drawable.attendance_checkbox_present) // Update to present icon
                }
                notifyItemChanged(adapterPosition) // Notify adapter about change
            }



            // Initial setup for icons based on attendance status
            binding.attendanceIcon1.setImageResource(
                if (studentAttendance.isPresent) R.drawable.attendance_checkbox_present else R.drawable.attendance_checkbox_absent
            )


            // Admin will see the attendance date, else it's hidden for the parent
            if (role == "admin") {
                binding.studentNameTextView.visibility = View.VISIBLE
                binding.dateTextView.text = studentAttendance.date
                binding.dateTextView.visibility = View.VISIBLE
            } else if (role == "parent") {
                binding.studentNameTextView.visibility = View.GONE
                binding.dateTextView.visibility = View.VISIBLE
                binding.dateTextView.text = studentAttendance.date
            } else {
                binding.studentNameTextView.visibility = View.VISIBLE
                binding.attendanceIcon.visibility = View.GONE
                binding.attendanceIcon1.visibility = View.VISIBLE
            }
        }
    }

    // DiffCallback for comparing items
    class AttendanceDiffCallback : DiffUtil.ItemCallback<StudentAttendance>() {
        override fun areItemsTheSame(oldItem: StudentAttendance, newItem: StudentAttendance): Boolean {
            return oldItem.studentName == newItem.studentName
        }

        override fun areContentsTheSame(oldItem: StudentAttendance, newItem: StudentAttendance): Boolean {
            return oldItem == newItem
        }
    }

    // Add this method to update the list dynamically
    fun updateList(newList: List<StudentAttendance>) {
        submitList(newList) // submitList is used for ListAdapter to efficiently update the list
    }
}
