package com.example.parent_connect.schoolgalary

import android.graphics.Matrix
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.parent_connect.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar  // Reference for the loader
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var matrix: Matrix
    private var scaleFactor = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        imageView = findViewById(R.id.full_screen_image_view)
        progressBar = findViewById(R.id.progress_bar)  // Initialize the progress bar
        matrix = Matrix()

        // Get the image URL passed from the previous activity
        val imageUrl = intent.getStringExtra("imageUrl")

        // Load the image into the ImageView (Picasso or Glide can be used)
        Picasso.get().load(imageUrl).into(imageView, object : Callback {
            override fun onSuccess() {
                // Hide the ProgressBar when image is successfully loaded
                progressBar.visibility = ProgressBar.GONE
            }

            override fun onError(e: Exception?) {
                // Optionally handle error (e.g., show a toast)
                progressBar.visibility = ProgressBar.GONE
            }
        })

        // Set up GestureDetector for double tap
        gestureDetector = GestureDetector(this, GestureListener())

        // Set up ScaleGestureDetector for pinch zoom
        scaleGestureDetector = ScaleGestureDetector(this, ScaleGestureListener())

        imageView.setOnTouchListener { v, event ->
            scaleGestureDetector.onTouchEvent(event)  // Handle pinch zoom
            gestureDetector.onTouchEvent(event)  // Handle gestures like double tap
            true
        }
    }

    // GestureListener to handle double-tap zoom
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        // Handle double-tap zoom (optional)
        override fun onDoubleTap(e: MotionEvent): Boolean {
            scaleFactor *= 2f
            matrix.setScale(scaleFactor, scaleFactor)
            imageView.imageMatrix = matrix
            return true
        }
    }

    // ScaleGestureListener to handle pinch zoom
    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 10f)

            matrix.setScale(scaleFactor, scaleFactor)
            imageView.imageMatrix = matrix
            return true
        }
    }

    // Handle pan (dragging the image)
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            finish()  // Close the activity on touch release
        }
        return super.onTouchEvent(event)
    }
}
