package com.example.hackathon

import User
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import java.util.*

import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        val FirstName = findViewById<EditText>(R.id.editFirstName)
        val FamilyName = findViewById<EditText>(R.id.editFamilyName)
        val SurName = findViewById<EditText>(R.id.editSurName)
        val BirthDate = findViewById<EditText>(R.id.editBirthDate)
        val phoneNumber = findViewById<EditText>(R.id.editPhoneNumber)
        val address = findViewById<EditText>(R.id.editAddress)
        val idNumber = findViewById<EditText>(R.id.editIdNumber)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val radioMale = findViewById<RadioButton>(R.id.radioMale)
        val radioFemale = findViewById<RadioButton>(R.id.radioFemale)
        val editBirthDate = findViewById<EditText>(R.id.editBirthDate)

        editBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                editBirthDate.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            }, year, month, day)

            datePicker.show()
        }

        btnRegister.setOnClickListener {
            val FirstName = FirstName.text.toString().trim()
            val FamilyName = FamilyName.text.toString().trim()
            val SurName = SurName.text.toString().trim()
            val BirthDate = BirthDate.text.toString().trim()
            val phone = phoneNumber.text.toString().trim()
            val addr = address.text.toString().trim()
            val id = idNumber.text.toString().trim()


            if (FirstName.isEmpty() || FamilyName.isEmpty() || phone.isEmpty() || addr.isEmpty() || id.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = if (radioMale.isChecked) "Мужской" else "Женский"  // Get the user type (normal user or officer)

            // Create unique user ID in Firebase
            val userId = database.child("users").push().key ?: return@setOnClickListener
            val user = User(FirstName, FamilyName, SurName, BirthDate, phone, addr, id, gender)

            // Push user data to Firebase
            database.child("users").child(userId).setValue(user)
                .addOnSuccessListener {
                    // Handle success
                    val editor = sharedPreferences.edit()
                    editor.putString("FirstName", user.FirstName)
                    editor.putString("FamilyName", user.FamilyName)
                    editor.putString("SurName", user.SurName)
                    editor.putString("BirthDate", user.BirthDate)
                    editor.putString("phoneNumber", user.phoneNumber)
                    editor.putString("address", user.address)
                    editor.putString("idNumber", user.idNumber)
                    editor.putString("gender", user.gender)
                    editor.putBoolean("isRegistered", true)
                    editor.apply()

                    Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    // Handle failure
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}
