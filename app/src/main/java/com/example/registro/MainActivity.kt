package com.example.registro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var cbMantenerSesion: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        emailInput = findViewById(R.id.editTextUsername)
        passwordInput = findViewById(R.id.editTextPassword)
        cbMantenerSesion = findViewById(R.id.cbMantenerSesion)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val mantenerSesion = prefs.getBoolean("mantenerSesion", false)

        // Si el usuario eligió mantener sesión y sigue logueado, lo mandamos a Inicio
        if (mantenerSesion && auth.currentUser != null) {
            startActivity(Intent(this, Inicio::class.java))
            finish()
            return
        }

        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val registerText = findViewById<TextView>(R.id.textViewRegisterLink)
        val registerTextPrefix = findViewById<TextView>(R.id.textViewRegister)
        val forgotPasswordText = findViewById<TextView>(R.id.textViewForgotPassword)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Guardar preferencia de mantener sesión
                        prefs.edit().putBoolean("mantenerSesion", cbMantenerSesion.isChecked).apply()

                        Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Inicio::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Registro
        val goToRegistro = {
            val intent = Intent(this, registro::class.java)
            startActivity(intent)
        }
        registerText.setOnClickListener { goToRegistro() }
        registerTextPrefix.setOnClickListener { goToRegistro() }

        // Recuperar contraseña
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ContrasenaActivity::class.java)
            startActivity(intent)
        }
    }
}
