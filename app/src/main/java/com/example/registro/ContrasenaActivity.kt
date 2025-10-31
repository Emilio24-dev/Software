package com.example.registro

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ContrasenaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contrasena)

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Referencias a los elementos del layout
        val emailInput = findViewById<EditText>(R.id.resetEmailEditText)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val backToLogin = findViewById<TextView>(R.id.textViewBackToLogin)

        // Acción botón "Enviar enlace"
        resetButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Se envió un enlace a tu correo para restablecer la contraseña",
                            Toast.LENGTH_LONG
                        ).show()
                        finish() // volver al login
                    } else {
                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Acción "Volver al login"
        backToLogin.setOnClickListener {
            finish() // Cierra esta activity y regresa al login
        }
    }
}
