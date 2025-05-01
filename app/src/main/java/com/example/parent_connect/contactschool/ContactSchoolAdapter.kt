package com.example.parent_connect.contactschool

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

class ContactSchoolAdapter(private val schools: List<School>) : RecyclerView.Adapter<ContactSchoolAdapter.ContactSchoolViewHolder>() {

    inner class ContactSchoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // School Info
        val name: TextView = itemView.findViewById(R.id.schoolName)
        val address: TextView = itemView.findViewById(R.id.schoolAddress)

        // Principal Contact Info
        val principalPhone: TextView = itemView.findViewById(R.id.principalPhone)
        val principalEmail: TextView = itemView.findViewById(R.id.principalEmail)

        // Fees Contact Info
        val feesPhone: TextView = itemView.findViewById(R.id.feesPhone)
        val feesEmail: TextView = itemView.findViewById(R.id.feesEmail)

        // General Contact Info
        val schoolPhone: TextView = itemView.findViewById(R.id.schoolPhone)
        val schoolEmail: TextView = itemView.findViewById(R.id.schoolEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactSchoolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_school, parent, false)
        return ContactSchoolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactSchoolViewHolder, position: Int) {
        val school = schools[position]

        // School Details
        holder.name.text = school.name
        holder.address.text = school.address

        // Principal Contact
        setUpContact(holder.principalPhone, school.principalPhone)
        setUpContact(holder.principalEmail, school.principalEmail)

        // Fees Contact
        setUpContact(holder.feesPhone, school.feesPhone)
        setUpContact(holder.feesEmail, school.feesEmail)

        // General School Contact
        setUpContact(holder.schoolPhone, school.schoolPhone)
        setUpContact(holder.schoolEmail, school.schoolEmail)
    }

    private fun setUpContact(contactView: TextView, contactInfo: String) {
        if (!TextUtils.isEmpty(contactInfo)) {
            contactView.text = contactInfo
            contactView.setOnClickListener {
                if (contactInfo.contains("@")) {
                    // If it's an email address
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$contactInfo"))
                    it.context.startActivity(intent)
                } else {
                    // If it's a phone number
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactInfo"))
                    it.context.startActivity(intent)
                }
            }
        } else {
            contactView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = schools.size
}
