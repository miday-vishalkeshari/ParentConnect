package com.example.parent_connect.admin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.databinding.ItemClassBinding
import com.example.parent_connect.insideclass.InsideClassActivity

class AdminAdapter(private val classList: List<ClassData>) : RecyclerView.Adapter<AdminAdapter.ClassViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val binding = ItemClassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val classData = classList[position]
        holder.bind(classData)
    }

    override fun getItemCount(): Int = classList.size

    class ClassViewHolder(private val binding: ItemClassBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(classData: ClassData) {
            binding.classId.text = classData.classId

            // Set up click listener for the class ID to show sections
            binding.classId.setOnClickListener {
                showPopupMenu(it, classData.classId, classData.sections)  // Pass classId here
            }
        }


        // Function to show a popup menu with sections
        private fun showPopupMenu(view: View, classId: String, sections: List<String>) {
            val context = view.context
            val popupMenu = PopupMenu(context, view)

            // Add each section to the popup menu
            for (section in sections) {
                popupMenu.menu.add(section)
            }

            // Set listener for menu item click
            popupMenu.setOnMenuItemClickListener { menuItem ->
                // Show a toast with the selected section and class ID
                val selectedSection = menuItem.title.toString()
                selectSection(classId, selectedSection)
                true // Return true to indicate the item click was handled
            }

            popupMenu.show()
        }

        private fun selectSection(classId: String, section: String) {
            // Show a toast with the class ID and selected section
            val context = binding.root.context
            //Toast.makeText(context, "Class: $classId, Section: $section", Toast.LENGTH_SHORT).show()

            // Create an Intent to open InsideClassActivity
            val intent = Intent(context, InsideClassActivity::class.java).apply {
                // Pass classId and section as extras
                putExtra("CLASS_ID", classId)
                putExtra("SECTION", section)
            }

            // Start the activity
            context.startActivity(intent)
        }


    }
}
