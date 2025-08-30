package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class registro : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "RegistroActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val goToLogin = findViewById<TextView>(R.id.goToLogin)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            when {
                name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showToast("Por favor, completa todos los campos.")
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showToast("Correo electrónico inválido.")
                }
                !phone.matches(Regex("^[0-9]{8,15}$")) -> {
                    showToast("Número de teléfono inválido. Usa solo dígitos (mínimo 8).")
                }
                !Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$").matches(password) -> {
                    showToast("La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula y un número.")
                }

                password != confirmPassword -> {
                    showToast("Las contraseñas no coinciden.")
                }
                else -> {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid

                                if (userId != null) {
                                    val user = mapOf(
                                        "nombre" to name,
                                        "email" to email,
                                        "telefono" to phone
                                    )

                                    Log.d(TAG, "Usuario creado con ID: $userId")
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                                    FirebaseDatabase.getInstance().getReference("Usuarios")
                                        .child(userId)
                                        .setValue(user)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Datos guardados en la base de datos")
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Log.e(TAG, "Error al guardar datos: ${it.message}")
                                            showToast("Error al guardar datos: ${it.message}")
                                        }
                                } else {
                                    Log.e(TAG, "userId es null")
                                    showToast("Error: no se pudo obtener el ID del usuario.")
                                }
                            } else {
                                Log.e(TAG, "Fallo en createUser: ${task.exception?.message}")
                                showToast("Error al registrar: ${task.exception?.message}")
                            }
                        }
                }
            }
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

