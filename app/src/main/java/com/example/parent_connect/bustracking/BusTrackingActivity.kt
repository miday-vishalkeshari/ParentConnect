package com.example.parent_connect.bustracking

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.parent_connect.R
import com.example.parent_connect.databinding.ActivityBusTrackingBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.widget.TextView
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.Marker
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import android.location.Geocoder
import android.location.Address
import org.json.JSONException
import java.util.Locale

class BusTrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityBusTrackingBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var busMarker: Marker? = null
    private var polyline: Polyline? = null  // Variable to hold the current polyline

    // Declare TextView variables globally
    private lateinit var busLocationDetailsTextView: TextView
    private lateinit var homeLocationDetailsTextView: TextView


    companion object {
        private const val TAG = "BusTrackingActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBusTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the TextViews
        busLocationDetailsTextView = findViewById(R.id.busLocationDetails)
        homeLocationDetailsTextView = findViewById(R.id.homeLocationDetails)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize location request
        locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5 seconds interval
            fastestInterval = 2000 // 2 seconds fastest interval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    updateBusLocation(location)
                }
            }
        }
    }

    private fun updateBusLocation(location: Location) {
        val newLocation = LatLng(location.latitude, location.longitude)

        // Use Geocoder to fetch the address for the bus location
        val geocoder = Geocoder(this, Locale.getDefault())
        var address: Address? = null
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                address = addresses[0]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Get a readable address (Street name or similar)
        val addressText = address?.getAddressLine(0) ?: "Unknown Location"

        if (busMarker == null) {
            val busIcon = BitmapFactory.decodeResource(resources, R.drawable.bus_icon)
            val resizedBusIcon = Bitmap.createScaledBitmap(busIcon, 100, 100, false)
            val busBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedBusIcon)

            busMarker = mMap.addMarker(
                MarkerOptions()
                    .position(newLocation)
                    .title("Bus")
                    .icon(busBitmapDescriptor)
            )
        } else {
            busMarker?.position = newLocation
        }

        // Update the UI with the bus location details
        val busDetails = "$addressText"
        busLocationDetailsTextView.text = busDetails

        // Optionally move the camera to follow the bus
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15f))

        // Home location details (You can also update this dynamically if needed)
        val homeLatitude = 18.5204  // Replace with your home latitude
        val homeLongitude = 73.8567 // Replace with your home longitude
        val homeDetails = " Latitude = $homeLatitude, Longitude = $homeLongitude"
        homeLocationDetailsTextView.text = homeDetails

        // Fetch directions from the current location to Pune Station
        val destination = LatLng(18.5193, 73.8558)  // Example destination (Pune Station)
        getDirections(newLocation, destination)

        // Optionally, adjust the camera to show both bus and home locations
        val bounds = LatLngBounds.builder()
            .include(newLocation)  // Add bus location
            .include(LatLng(18.5204, 73.8567))  // Add home location (replace with your actual home location)

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), 100)
        mMap.animateCamera(cameraUpdate)
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        // Retrieve the Directions API key from strings.xml
        val DIRECTIONS_API_KEY: String = getString(R.string.google_maps_key)

        // Construct the URL for the Directions API
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$DIRECTIONS_API_KEY"
        Log.d(TAG, "Directions API URL: $url")

        // Start a background thread to fetch directions
        Thread {
            var responseData: String? = null
            var responseSuccessful = false

            try {
                // Create OkHttpClient and make the request
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                // Check if the response is successful
                if (response.isSuccessful) {
                    responseData = response.body?.string()
                    responseSuccessful = true
                } else {
                    Log.e(TAG, "Error fetching directions: Response unsuccessful")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error fetching directions: ${e.message}")
            }

            if (responseSuccessful && responseData != null) {
                try {
                    // Parse the response data (JSON)
                    val jsonResponse = JSONObject(responseData)
                    val routes = jsonResponse.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val legs = route.getJSONArray("legs")
                        val steps = legs.getJSONObject(0).getJSONArray("steps")

                        // Prepare polyline options for the route
                        val polylineOptions = PolylineOptions()

                        // Extract polyline points from the steps
                        for (i in 0 until steps.length()) {
                            val step = steps.getJSONObject(i)
                            val polyline = step.getJSONObject("polyline")
                            val points = decodePoly(polyline.getString("points"))

                            // Log each point in the polyline
                            Log.d(TAG, "Points in step $i: $points")

                            // Add points to polyline options
                            for (point in points) {
                                polylineOptions.add(LatLng(point.latitude, point.longitude))
                            }
                        }

                        Log.d(TAG, "Polyline has ${polylineOptions.points.size} points")

                        // Update the UI on the main thread
                        runOnUiThread {
                            // Remove any existing polyline
                            polyline?.remove()

                            // Add the new polyline to the map
                            polyline = mMap.addPolyline(polylineOptions)

                            // Optionally, adjust the camera to show the entire route
                            val bounds = LatLngBounds.builder()
                            bounds.include(origin)
                            bounds.include(destination)
                            for (latLng in polylineOptions.points) {
                                bounds.include(latLng)
                            }
                            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), 100)
                            mMap.animateCamera(cameraUpdate)
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Error parsing directions response: ${e.message}")
                }
            } else {
                // Handle response failure
                runOnUiThread {
                    // Optionally show an error message to the user
                    Toast.makeText(applicationContext, "Error fetching directions.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // Decode polyline encoded string into LatLng list
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) -(result shr 1) else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) -(result shr 1) else (result shr 1)
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }

    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val busIcon = BitmapFactory.decodeResource(resources, R.drawable.bus_icon)
        val resizedBusIcon = Bitmap.createScaledBitmap(busIcon, 100, 100, false)
        val busBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedBusIcon)

        // Enable location layer
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true

            // Get current location and move camera to it
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    busMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(location.latitude, location.longitude))
                            .title("Bus")
                            .icon(busBitmapDescriptor)
                    )
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)) // Focus the camera to current location with zoom level 15
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        // Optionally, set map type and other settings
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.uiSettings.isZoomControlsEnabled = true

        // Home marker code
        val homeLatitude = 18.5204  // Replace with the home latitude
        val homeLongitude = 73.8567 // Replace with the home longitude
        val homeIcon = BitmapFactory.decodeResource(resources, R.drawable.home_icon) // Replace with your home icon

        val resizedHomeIcon = Bitmap.createScaledBitmap(homeIcon, 100, 100, false)
        val homeBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedHomeIcon)

        // Add home marker on the map
        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(homeLatitude, homeLongitude))
                .title("Home")
                .icon(homeBitmapDescriptor)
        )
    }


    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}
