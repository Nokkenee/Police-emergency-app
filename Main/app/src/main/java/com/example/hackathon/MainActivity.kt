package com.example.hackathon

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
//import android.location.Location
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.Manifest
import android.os.Looper
import com.google.android.gms.location.*

import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.hackathon.ui.theme.HackathonTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.location.Location  // Ensure this is included
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast

class MainActivity : ComponentActivity() {
    private lateinit var reportId: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var sharedPreferences: SharedPreferences
    private var timer: CountDownTimer? = null
    private lateinit var callSlider: ImageView
    private lateinit var slideToCallLayout: RelativeLayout
    private lateinit var slideText: TextView
    private var isCalling = false
    private var initialX = 0f  // Store initial X position
    private lateinit var statusFeed: LinearLayout
    private lateinit var statusSending: TextView
    private lateinit var statusSent: TextView
    private lateinit var statusAccepted: TextView
    private lateinit var statusNotAccepted: TextView
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
//        startActivity(Intent(this, OfficerFeedActivity::class.java))
//        finish() // Close MainActivity

        super.onCreate(savedInstanceState)
        var innactive = false
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isRegistered = sharedPreferences.getBoolean("isRegistered", false)
//        val isRegistered = false
        if (!isRegistered) {
//            // Redirect to RegisterActivity if not registered
            startActivity(Intent(this, RegisterActivity::class.java))
            finish() // Close MainActivity
            return
        }
//        clearLocalData()
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
//
        btnEmergency.setOnClickListener {
            if(!innactive) {
                btnEmergency.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                btnEmergency.setBackgroundResource(R.drawable.hollow_button)
                innactive = true
                startStopwatch()
                sendEmergencyAlert()
                startLocationUpdates()
                expandStatusFeed()
            }
        }

        val btnEmergencyDialog = findViewById<Button>(R.id.btnEmergencyDialog) // Button to redirect
        btnEmergencyDialog.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java) // Change this to DialogActivity
            intent.putExtra("usertype", "Basicuser")
            intent.putExtra("reportId", reportId) // Pass the reportId to DialogActivity
            startActivity(intent) // Redirect to DialogActivity
//            startActivity(ChatActivity)
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            startActivity(Intent(this, OfficerFeedActivity::class.java))
            finish()  // Optionally finish the MainActivity to prevent back navigation
        }

        callSlider = findViewById(R.id.callSlider)
        slideToCallLayout = findViewById(R.id.slideToCallLayout)
        slideText = findViewById(R.id.slideText)

        statusSending = findViewById(R.id.statusSending)
        statusSent = findViewById(R.id.statusSent)
        statusAccepted = findViewById(R.id.statusAccepted)
        statusNotAccepted = findViewById(R.id.statusNotAccepted)

        callSlider.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = v.x // Store starting position
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX - (180)
                    if (newX > 20 && newX < slideToCallLayout.width - v.width-20) {
                        v.x = newX
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (v.x >= slideToCallLayout.width - v.width - 30) {
                        // Fully slid → Make a call
                        makeEmergencyCall()
                    } else {
                        // Not fully slid → Reset position
                        resetSlider()
                    }
                }
            }
            true
        }
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000) // Replaces fastestInterval
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    sendLocationToFirebase(location)  // This is correct now
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    private fun sendLocationToFirebase(location: Location) {
        val database = FirebaseDatabase.getInstance().getReference("emergency_reports/$reportId/location")

        val locationData = Emergency.GeoLocation(
            latitude = location.latitude,
            longitude = location.longitude
        )

        database.setValue(locationData)
    }

    private fun expandStatusFeed() {

        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
        ObjectAnimator.ofFloat(btnEmergency, "translationY", -150f).setDuration(300).start()
        ObjectAnimator.ofFloat(slideToCallLayout, "translationY", 300f).setDuration(300).start()
        isExpanded = true
    }

    private fun makeEmergencyCall() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:112") // Kazakhstan Police Number
        startActivity(intent)
        resetSlider()
    }

    private fun resetSlider() {
        callSlider.animate().translationX(0f).setDuration(300).start()
        isCalling = false
    }

    private fun sendEmergencyAlert() {
        statusSending.visibility = View.VISIBLE
        statusSent.visibility = View.GONE

        val fullName = sharedPreferences.getString("FamilyName", "Unknown") + ' ' + sharedPreferences.getString("FirstName", "Unknown") + ' '+ sharedPreferences.getString("SurName", "")
        val phoneNumber = sharedPreferences.getString("phoneNumber", "Unknown")
        val address = sharedPreferences.getString("address", "Unknown")
        val idNumber = sharedPreferences.getString("idNumber", "Unknown")
        val birthDate = sharedPreferences.getString("BirthDate", "Unknown")
        val gender = sharedPreferences.getString("gender", "Unknown")
        val database = FirebaseDatabase.getInstance().reference
        val reportRef = database.child("emergency_reports").push() // Generate unique reportId
        reportId = reportRef.key ?: return // Get generated key
//        val emergencyDetails = "МЕНЯ ЕБУТ"
        // Save reportId locally
        sharedPreferences.edit().putString("reportId", reportId).apply()

        val emergencyData = mapOf(
            "reportId" to reportId,  // Store reportId in the database
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "address" to address,
            "idNumber" to idNumber,
            "birthDate" to birthDate,
            "gender" to gender,
            "status" to "Not processed"
        )

        statusSending.visibility = View.VISIBLE

        reportRef.setValue(emergencyData)
            .addOnSuccessListener {
                statusSending.visibility = View.GONE
                statusSent.visibility = View.VISIBLE
                statusNotAccepted.visibility = View.VISIBLE
                listenForOfficerResponse(reportId) // Now listen for the correct reportId
            }
            .addOnFailureListener {
                // Handle error (e.g., show a toast)
            }
    }

    private fun listenForOfficerResponse(reportId: String) {
        val reportRef = FirebaseDatabase.getInstance().getReference("emergency_reports").child(reportId)
        val btnEmergencyDialog = findViewById<Button>(R.id.btnEmergencyDialog) // Button to redirect

        reportRef.child("status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                if (status == "accepted") {
                    statusNotAccepted.visibility = View.GONE
                    statusAccepted.visibility = View.VISIBLE
                    btnEmergencyDialog.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }




    private fun startStopwatch() {
        var seconds = 0
        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
        timer = object : CountDownTimer(900000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                seconds+=1
                btnEmergency.text = (seconds/60/10).toString()+(seconds/60%10).toString() + ":" + (seconds/10%6).toString() + (seconds%10).toString()
            }

            override fun onFinish() {
                btnEmergency.text = "EMERGENCY"
            }
        }.start()
    }

    private fun clearLocalData() {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.clear()  // Clears all data in shared preferences
        editor.apply()
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HackathonTheme {
        Greeting("Android")
    }
}