package com.example.parent_connect.teachers_and_students.teacherschedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.databinding.ItemTeacherBinding

class PersonAdapter(
    private val personList: List<PersonData>
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemTeacherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = personList[position]
        holder.bind(person)
    }

    override fun getItemCount(): Int = personList.size

    inner class PersonViewHolder(private val binding: ItemTeacherBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(person: PersonData) {
            // Set visibility for PersonAdapter
            binding.textPersonName.visibility = View.VISIBLE
            binding.textPersonId.visibility = View.VISIBLE

            binding.textschedulePeriod.visibility = View.GONE
            binding.textClassName.visibility = View.GONE
            binding.textSubject.visibility = View.GONE

            // Bind data
            binding.textPersonName.text = person.personName
            binding.textPersonId.text = person.personId
        }
    }
}
