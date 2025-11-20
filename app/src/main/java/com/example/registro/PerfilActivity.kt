package com.example.registro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class PerfilActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var btnInicio: TextView
    private lateinit var etNombre: EditText
    private lateinit var etTelefono: EditText
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        etNombre = findViewById(R.id.etNombre)
        etTelefono = findViewById(R.id.etTelefono)
        etEmail = findViewById(R.id.etEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogout = findViewById(R.id.btnLogout)
        btnInicio = findViewById(R.id.btninicio)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Usuarios")

        val user = auth.currentUser
        user?.let {
            etEmail.setText(it.email)
            etEmail.isEnabled = false // Solo lectura

            val userId = it.uid
            database.child(userId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val nombre = snapshot.child("nombre").value?.toString() ?: ""
                        val telefono = snapshot.child("telefono").value?.toString() ?: ""

                        etNombre.setText(nombre)
                        etTelefono.setText(telefono)

                        etNombre.isEnabled = false
                        etTelefono.isEnabled = false
                    } else {
                        etNombre.setText("No disponible")
                        etTelefono.setText("No disponible")
                    }
                }
                .addOnFailureListener { error ->
                    etNombre.setText("Error")
                    etTelefono.setText("Error")
                }
        }

        btnInicio.setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, NuevaContrasenaActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
