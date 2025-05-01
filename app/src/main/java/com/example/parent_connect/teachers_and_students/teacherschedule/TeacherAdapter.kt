package com.example.parent_connect.teachers_and_students.teacherschedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.databinding.ItemTeacherBinding

class TeacherAdapter(
    private val teacherList: List<TeacherScheduleData>
) : RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val binding = ItemTeacherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeacherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        val teacher = teacherList[position]
        holder.bind(teacher)
    }

    override fun getItemCount(): Int = teacherList.size

    inner class TeacherViewHolder(private val binding: ItemTeacherBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(teacher: TeacherScheduleData) {
            // Set visibility for TeacherAdapter
            binding.textschedulePeriod.visibility = View.VISIBLE
            binding.textClassName.visibility = View.VISIBLE
            binding.textSubject.visibility = View.VISIBLE

            binding.textPersonName.visibility = View.GONE
            binding.textPersonId.visibility = View.GONE

            // Bind data
            binding.textschedulePeriod.text = teacher.schedulePeriod
            binding.textClassName.text = teacher.classId
            binding.textSubject.text = teacher.subject
        }
    }
}
