package com.example.range
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var geofencingClient: GeofencingClient  // Add this line
    private lateinit var btnGetLocation: Button
    private lateinit var tvLocation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)  // Initialize GeofencingClient
        btnGetLocation = findViewById(R.id.btnGetLocation)
        tvLocation = findViewById(R.id.tvLocation)

        locationRequest = LocationRequest.create().apply {
            interval = 500 // Update interval in milliseconds
            fastestInterval = 100 // Fastest interval in milliseconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        btnGetLocation.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val lastLocation: Location = locationResult.lastLocation!!
                    tvLocation.text = "Latitude: ${lastLocation.latitude}, Longitude: ${lastLocation.longitude}"

                    // Check if the user is inside the geofence area
                    val isInsideGeofence = isInsideGeofenceArea(lastLocation.latitude, lastLocation.longitude)
                    if (isInsideGeofence) {
                        tvLocation.append("\nYou are inside the geofence area")
                    } else {
                        tvLocation.append("\nYou are outside the geofence area")
                    }
                }
            },
            null
        )
    }

    // Define your geofence area here
    private val labCorner1 = LatLng(16.92083, 73.34150)
    private val labCorner2 = LatLng(16.92075, 73.31450)
    private val labCorner3 = LatLng(16.92084, 73.31457)
    private val labCorner4 = LatLng(16.92075, 73.31461)

    private fun isInsideGeofenceArea(latitude: Double, longitude: Double): Boolean {
        return latitude >= labCorner1.latitude && latitude <= labCorner2.latitude &&
                longitude >= labCorner3.longitude && longitude <= labCorner4.longitude
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                tvLocation.text = "Permission denied"
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
