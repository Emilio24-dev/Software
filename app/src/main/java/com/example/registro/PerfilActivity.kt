package com.example.registro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.app.AlertDialog

class PerfilActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

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
    private lateinit var btnDeleteAccount: Button


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
        btnDeleteAccount = findViewById(R.id.btnDelete)

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

        // ====== ELIMINAR CUENTA ======
        btnDeleteAccount.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = user.uid

            AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("¿Seguro que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar") { _, _ ->
                    // 1. Borrar datos del usuario en Realtime Database
                    database.child(userId).removeValue()
                        .addOnCompleteListener {
                            // 2. Borrar el usuario de Firebase Auth
                            user.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Cuenta eliminada correctamente",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Volver a la pantalla de login y limpiar el back stack
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Error al eliminar la cuenta: ${task.exception?.localizedMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
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
