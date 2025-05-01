package com.example.parent_connect.reportcard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R

// Data class to represent an Exam Report
data class ExamReport(
    val examName: String = "",
    val examDate: String = "",
    val pdfUrl: String = ""
)
class ReportCardAdapter(
    private val reports: MutableList<ExamReport>,  // List of exam reports
    private val onReportClick: (ExamReport) -> Unit,  // Callback for clicking a report item
    private val onViewReportClick: (String) -> Unit,  // Callback for viewing the PDF
    private val userRole: String,  // Added missing comma here
    private val onDeleteReportClick: (ExamReport) -> Unit  // Callback for deleting a report
) : RecyclerView.Adapter<ReportCardAdapter.ReportViewHolder>() {

    // ViewHolder to hold and bind the views for each report item
    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reportName: TextView = itemView.findViewById(R.id.reportName)
        val reportDate: TextView = itemView.findViewById(R.id.reportDate)
        val viewReportButton: ImageButton = itemView.findViewById(R.id.viewReportButton)
        val deleteReportButton: ImageButton = itemView.findViewById(R.id.deleteReportButton)

        // Bind data to the views
        fun bind(report: ExamReport) {
            Log.d("ReportCardAdapter", "Exam Name: ${report.examName}, Exam Date: ${report.examDate}")
            reportName.text = report.examName
            reportDate.text = report.examDate

            // Set up click listener for the 'View Report' button (ImageButton)
            viewReportButton.setOnClickListener {
                onViewReportClick(report.pdfUrl)
            }

            // Set the delete button visibility based on the user role
            deleteReportButton.visibility = if (userRole == "Teacher") View.VISIBLE else View.GONE

            // Set up click listener for the 'Delete Report' button (ImageButton)
            deleteReportButton.setOnClickListener {
                onDeleteReportClick(report) // Pass the entire ExamReport object for deletion
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report_card, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount(): Int = reports.size

    // Optional: A method to update the list of reports after deletion
    fun removeReport(report: ExamReport) {
        val position = reports.indexOf(report)
        if (position != -1) {
            reports.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
