package com.example.parent_connect.reportcard

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parent_connect.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class ReportCardActivity : AppCompatActivity() {

    private lateinit var reportCardRecyclerView: RecyclerView
    private lateinit var wardSelectorSpinner: Spinner
    private lateinit var addExamPdfButton: FloatingActionButton
    private lateinit var classSelectorSpinner: Spinner
    private lateinit var studentSelectorSpinner: Spinner
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportcard)

        // Initialize views
        reportCardRecyclerView = findViewById(R.id.reportCardRecyclerView)
        wardSelectorSpinner = findViewById(R.id.wardSelectorSpinner)
        addExamPdfButton = findViewById(R.id.addExamPdfButton)
        classSelectorSpinner = findViewById(R.id.classSelectorSpinner)
        studentSelectorSpinner = findViewById(R.id.studentSelectorSpinner)
        progressBar = findViewById(R.id.loadingProgressBar)

        // Set up RecyclerView
        reportCardRecyclerView.layoutManager = LinearLayoutManager(this)

        // Get the user role from the intent
        val role = intent.getStringExtra("USER_ROLE") ?: ""

        // Hide/show UI components based on user role
        if (role == "Parent") {
            wardSelectorSpinner.visibility = View.VISIBLE
            classSelectorSpinner.visibility = View.GONE
            studentSelectorSpinner.visibility = View.GONE
            addExamPdfButton.visibility = View.GONE
        } else if (role == "Teacher") {
            wardSelectorSpinner.visibility = View.GONE
            classSelectorSpinner.visibility = View.VISIBLE
            studentSelectorSpinner.visibility = View.VISIBLE
            addExamPdfButton.setOnClickListener {
                openPdfPicker()
            }
        } else {
            wardSelectorSpinner.visibility = View.GONE
            classSelectorSpinner.visibility = View.VISIBLE
            studentSelectorSpinner.visibility = View.VISIBLE
            addExamPdfButton.visibility = View.GONE
        }

        // Set up Spinner (Assuming you have a list of wards)
        val wardsArray = resources.getStringArray(R.array.wards_array)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, wardsArray)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        wardSelectorSpinner.adapter = spinnerAdapter

        // Load data when a ward is selected
        wardSelectorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedWard = wardsArray[position]
                loadReportsForWard(selectedWard)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection
            }
        }
    }

    private fun openPdfPicker() {
        // Open file picker to select a PDF file
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/pdf"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, 100)  // Request code for selecting a PDF
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            uri?.let { checkAndUploadPdf(it) }
        }
    }

    private fun checkAndUploadPdf(pdfUri: Uri) {
        // Check if the PDF is already uploaded by searching for its URL
        val db = FirebaseFirestore.getInstance()
        val selectedWard = wardSelectorSpinner.selectedItem.toString()

        db.collection("examReports")
            .whereEqualTo("ward", selectedWard)
            .whereEqualTo("pdfUrl", pdfUri.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // If no existing document found, upload the PDF
                    uploadPdfToFirebase(pdfUri)
                } else {
                    // PDF already uploaded, notify the user
                    Toast.makeText(this, "This PDF has already been uploaded.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadPdfToFirebase(pdfUri: Uri) {
        progressBar.visibility = View.VISIBLE // Show ProgressBar when starting upload

        // Get Firebase Storage reference
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val fileName = "exam_report_${System.currentTimeMillis()}.pdf"
        val pdfRef = storageReference.child("exam_pdfs/$fileName")

        // Upload the PDF
        pdfRef.putFile(pdfUri)
            .addOnSuccessListener {
                // Get the download URL
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    progressBar.visibility = View.GONE // Hide ProgressBar on success
                    savePdfUrlToFirestore(uri.toString())
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE // Hide ProgressBar on failure
                Toast.makeText(this, "Failed to upload PDF", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePdfUrlToFirestore(pdfUrl: String) {
        // Get Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Assuming you have the ward selected
        val selectedWard = wardSelectorSpinner.selectedItem.toString()

        val examName = "Sample Exam"  // Replace this with dynamic input if needed

        // Create a map to save the PDF data
        val pdfData = mapOf(
            "ward" to selectedWard,
            "pdfUrl" to pdfUrl,
            "timestamp" to System.currentTimeMillis(),
            "examName" to examName
        )

        // Save data to Firestore
        db.collection("examReports")
            .add(pdfData)
            .addOnSuccessListener {
                Toast.makeText(this, "PDF uploaded successfully", Toast.LENGTH_SHORT).show()
                loadReportsForWard(selectedWard) // Reload the reports to reflect the new one
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save PDF URL", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadReportsForWard(ward: String) {
        progressBar.visibility = View.VISIBLE // Show ProgressBar when starting to load reports

        val db = FirebaseFirestore.getInstance()

        val role = intent.getStringExtra("USER_ROLE") ?: ""

        // Filter reports based on the role
        val query = if (role == "Parent") {
            db.collection("examReports")
                .whereEqualTo("ward", ward)
        } else {
            db.collection("examReports")
                .whereEqualTo("ward", ward) // Teachers may see all reports for the ward
        }

        query.get()
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE // Hide ProgressBar once task is complete

                if (task.isSuccessful) {
                    // Convert the immutable list to mutable list
                    val reports = task.result?.toObjects(ExamReport::class.java)?.toMutableList()
                    val adapter = ReportCardAdapter(
                        reports ?: mutableListOf(),
                        { report ->
                            // Handle whole item click (for details)
                            Toast.makeText(this, "Clicked on ${report.examName}", Toast.LENGTH_SHORT).show()
                        },
                        { pdfUrl ->
                            // Handle "View Report" button click (for viewing PDF)
                            checkIfFileExists(pdfUrl)
                        },
                        role,  // Pass the user role here
                        { report ->
                            // Handle "Delete Report" button click (for deleting the report)
                            deleteReport(report) // Pass the entire ExamReport object for deletion
                        }
                    )
                    reportCardRecyclerView.adapter = adapter
                } else {
                    Toast.makeText(this, "Failed to load reports", Toast.LENGTH_SHORT).show()
                }
            }
    }



    // Function to delete a report
    private fun deleteReport(report: ExamReport) {
        val db = FirebaseFirestore.getInstance()
        db.collection("examReports")
            .whereEqualTo("pdfUrl", report.pdfUrl)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("examReports").document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Report deleted successfully", Toast.LENGTH_SHORT).show()
                            loadReportsForWard(wardSelectorSpinner.selectedItem.toString())  // Reload reports
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to delete report", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun checkIfFileExists(pdfUrl: String) {
        val fileName = "exam_report_${System.currentTimeMillis()}.pdf"
        val localFile = File(getExternalFilesDir(null), fileName)

        if (localFile.exists()) {
            openPdf(localFile)
        } else {
            progressBar.visibility = View.VISIBLE // Show ProgressBar when starting to download
            downloadFile(pdfUrl, localFile)
        }
    }

    private fun downloadFile(pdfUrl: String, localFile: File) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)

        storageReference.getFile(localFile).addOnSuccessListener {
            progressBar.visibility = View.GONE // Hide ProgressBar after successful download
            openPdf(localFile)
        }.addOnFailureListener {
            progressBar.visibility = View.GONE // Hide ProgressBar if download fails
            Toast.makeText(this, "Failed to download PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPdf(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(this, "com.example.parent_connect.fileprovider", file)
        intent.setDataAndType(fileUri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No app found to open the PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
