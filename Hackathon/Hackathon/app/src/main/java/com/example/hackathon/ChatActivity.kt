package com.example.hackathon

import Message
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var reportId: String
    private lateinit var userType: String
    private var messagesRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make status bar transparent
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_chat)

        // Initialize UI components
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        editMessage = findViewById(R.id.editMessage)
        btnSend = findViewById(R.id.btnSend)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Get reportId and userType from Intent
        reportId = intent.getStringExtra("reportId") ?: run {
            Log.e("ChatActivity", "Error: Missing reportId")
            finish()
            return
        }
        userType = intent.getStringExtra("usertype") ?: run {
            Log.e("ChatActivity", "Error: Missing usertype")
            finish()
            return
        }

        // Initialize Firebase reference
        messagesRef = FirebaseDatabase.getInstance()
            .getReference("emergency_reports")
            .child(reportId)
            .child("messages")

        // Set up RecyclerView
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter()
        recyclerViewMessages.adapter = chatAdapter

        // Back button action
        btnBack.setOnClickListener { finish() }

        // Listen for real-time messages
        setupMessageListener()

        // Send button action
        btnSend.setOnClickListener { sendMessage() }
    }

    private fun setupMessageListener() {
        messagesRef?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    chatAdapter.addMessage(it)
                } ?: Log.e("Firebase", "Error: Received null message")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error listening to messages: ${error.message}")
            }
        })
    }

    private fun sendMessage() {
        var prefix = "Пользователь: "
        if (userType == "officer"){
            prefix = "Полиция: "
        }
        else{
            prefix = "Пользователь: "
        }
        val messageText = prefix + editMessage.text.toString().trim()

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val messageId = messagesRef?.push()?.key ?: UUID.randomUUID().toString()
        val message = mapOf(
            "sender" to userType,
            "messageContent" to messageText,
            "timestamp" to getCurrentTimestamp(),
            "messageId" to messageId // Ensure messageId is always set
        )

        messagesRef?.child(messageId)?.setValue(message)
            ?.addOnSuccessListener { Log.d("Firebase", "Message sent successfully") }
            ?.addOnFailureListener { error -> Log.e("Firebase", "Failed to send message: ${error.message}") }

        editMessage.text.clear()
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
