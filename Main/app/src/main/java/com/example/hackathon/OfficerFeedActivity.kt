package com.example.hackathon

import Emergency
import EmergencyAdapter
//import EmergencyDetailsActivity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class OfficerFeedActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmergencyAdapter
    private lateinit var emergenciesList: MutableList<Emergency>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_officer_feed)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewEmergencies)
        emergenciesList = mutableListOf()
        adapter = EmergencyAdapter(emergenciesList) { emergency -> acceptEmergencyReport(emergency) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().reference.child("emergency_reports")

        // Listen for changes in emergency reports
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val emergency = snapshot.getValue(Emergency::class.java)
                if (emergency != null && emergency.status != "accepted") { // Ignore accepted reports
                    emergenciesList.add(emergency)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedEmergency = snapshot.getValue(Emergency::class.java)
                if (updatedEmergency != null) {
                    val index = emergenciesList.indexOfFirst { it.reportId == updatedEmergency.reportId }
                    if (index != -1) {
                        if (updatedEmergency.status == "accepted") {
                            // Remove the report if it gets accepted
                            emergenciesList.removeAt(index)
                        } else {
                            // Update the report if it's still pending
                            emergenciesList[index] = updatedEmergency
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedEmergency = snapshot.getValue(Emergency::class.java)
                if (removedEmergency != null) {
                    emergenciesList.removeAll { it.reportId == removedEmergency.reportId }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })



    }

    private fun acceptEmergencyReport(emergency: Emergency) {
        if (emergency.reportId.isNullOrEmpty()) {
            return
        }

        val reportRef = FirebaseDatabase.getInstance()
            .getReference("emergency_reports")
            .child(emergency.reportId)

        reportRef.child("status").setValue("accepted").addOnSuccessListener {
            val intent = Intent(this, EmergencyDetailsActivity::class.java)
            intent.putExtra("reportId", emergency.reportId)
            startActivity(intent)
        }.addOnFailureListener { e ->
        }
    }


    private fun clearLocalData() {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.clear()
        editor.apply()
    }
}
