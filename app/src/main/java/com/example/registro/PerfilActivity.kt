package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var etEmail: EditText
    private lateinit var etNombre: EditText
    private lateinit var etTelefono: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var btnInicio: TextView
    private lateinit var btnEditar: Button
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // ====== INICIALIZAR FIREBASE ======
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Usuarios")

        // ====== VISTAS ======
        etNombre = findViewById(R.id.etNombre)
        etTelefono = findViewById(R.id.etTelefono)
        etEmail = findViewById(R.id.etEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogout = findViewById(R.id.btnLogout)
        btnInicio = findViewById(R.id.btninicio)
        btnEditar = findViewById(R.id.btnEditar)
        btnGuardar = findViewById(R.id.btnGuardar)

        // ====== CARGAR DATOS DEL USUARIO ======
        val user = auth.currentUser

        user?.let { u ->
            etEmail.setText(u.email)
            etEmail.isEnabled = false

            val userId = u.uid

            database.child(userId).get()
                .addOnSuccessListener { snapshot ->
                    val nombre = snapshot.child("nombre").value?.toString() ?: ""
                    val telefono = snapshot.child("telefono").value?.toString() ?: ""

                    etNombre.setText(nombre)
                    etTelefono.setText(telefono)

                    etNombre.isEnabled = false
                    etTelefono.isEnabled = false
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
        }

        // ====== BOTÓN INICIO ======
        btnInicio.setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
        }

        // ====== CAMBIAR CONTRASEÑA ======
        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, NuevaContrasenaActivity::class.java))
        }

        // ====== CERRAR SESIÓN ======
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // ====== ACTIVAR EDICIÓN ======
        btnEditar.setOnClickListener {
            etNombre.isEnabled = true
            etTelefono.isEnabled = true
            Toast.makeText(this, "Edita los datos y presiona Guardar", Toast.LENGTH_SHORT).show()
        }

        // ====== GUARDAR CAMBIOS ======
        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoTelefono = etTelefono.text.toString().trim()

            if (nuevoNombre.isEmpty() || nuevoTelefono.isEmpty()) {
                Toast.makeText(this, "No puedes dejar campos vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "nombre" to nuevoNombre,
                "telefono" to nuevoTelefono
            )

            // 👇 Aquí SOLO usamos 'database', NO lo reasignamos
            database.child(userId).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                    etNombre.isEnabled = false
                    etTelefono.isEnabled = false
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar los cambios", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
