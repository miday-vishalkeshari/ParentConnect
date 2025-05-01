package com.example.parent_connect.reportcard

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R
import java.io.File
import java.io.IOException

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var pdfImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        pdfImageView = findViewById(R.id.pdfImageView)

        val pdfUri = intent.getStringExtra("PDF_URL")
        pdfUri?.let { uri ->
            try {
                val file = File(uri) // Assuming you have the file in local storage
                val fileDescriptor: ParcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor)

                // Display the first page of the PDF
                showPage(0)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun showPage(pageIndex: Int) {
        val page: PdfRenderer.Page = pdfRenderer.openPage(pageIndex)

        val bitmap: Bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        pdfImageView.setImageBitmap(bitmap)

        page.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer.close()
    }
}
