package com.example.hackathon

import Emergency
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class EmergencyDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var textFullName: TextView
    private lateinit var titleDetails: TextView
    private lateinit var textPhoneNumber: TextView
    private lateinit var textIdNumber: TextView
    private lateinit var database: DatabaseReference
    private var googleMap: GoogleMap? = null
    private var userLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_details)
//        supportActionBar?.hide() // Uncomment if you want to remove the big header
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        textFullName = findViewById(R.id.textFullName)
        textPhoneNumber = findViewById(R.id.textPhoneNumber)
        textIdNumber = findViewById(R.id.textIdNumber)
        titleDetails = findViewById(R.id.titleDetails)
        val reportId = intent.getStringExtra("reportId") ?: return
        database = FirebaseDatabase.getInstance().getReference("emergency_reports").child(reportId)
        // Load the Map Fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val btnEmergencyDialog = findViewById<Button>(R.id.btnEmergencyDialog) // Button to redirect

        btnEmergencyDialog.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java) // Change this to DialogActivity
            intent.putExtra("usertype", "officer")
            intent.putExtra("reportId", reportId) // Pass the reportId to DialogActivity
            startActivity(intent) // Redirect to DialogActivity
//            startActivity(ChatActivity)
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emergency = snapshot.getValue(Emergency::class.java)
                if (emergency != null) {
                    titleDetails.text = "Вызов: ${emergency.reportId}"
                    textFullName.text = "Имя: ${emergency.fullName}"
                    textPhoneNumber.text = "Номер телефона: ${emergency.phoneNumber}"
                    textIdNumber.text = "ИИН: ${emergency.idNumber}"

                    if (emergency.location != null) {
                        userLocation = LatLng(emergency.location.latitude, emergency.location.longitude)
                        updateMap() // Call function to update the map
                    } else {
                        Log.e("MAP", "NO LOCATION DATA AVAILABLE")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DB_ERROR", "Database Error: ${error.message}")
            }
        })
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMapToolbarEnabled = true
        }
        updateMap() // Try updating the map if location is already available
    }

    private fun updateMap() {
        googleMap?.let { map ->
            userLocation?.let { location ->
                map.clear() // Clear previous markers
                map.addMarker(MarkerOptions().position(location).title("Местоположение пользователя"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            } ?: Log.e("MAP", "User location is null")
        } ?: Log.e("MAP", "Google Map is not ready yet!")
    }
}
