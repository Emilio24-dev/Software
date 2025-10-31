package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class NuevaContrasenaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var back: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_contrasena)

        auth = FirebaseAuth.getInstance()
        back = findViewById(R.id.back)
        val user = auth.currentUser

        val etCurrentPass = findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPass = findViewById<EditText>(R.id.etNewPassword)
        val btnUpdatePass = findViewById<Button>(R.id.btnUpdatePassword)

        back.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }


        btnUpdatePass.setOnClickListener {
            val currentPass = etCurrentPass.text.toString().trim()
            val newPass = etNewPass.text.toString().trim()



            if (currentPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(user?.email!!, currentPass)
            user.reauthenticate(credential).addOnSuccessListener {
                user.updatePassword(newPass).addOnSuccessListener {
                    Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
