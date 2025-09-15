package com.example.registro

import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser

class CambiarCorreoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etNuevoCorreo: EditText
    private lateinit var btnGuardarCorreo: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiar_correo)

        // Activar ActionBar con botón de regreso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Cambiar correo"

        auth = FirebaseAuth.getInstance()
        etNuevoCorreo = findViewById(R.id.etNuevoCorreo)
        btnGuardarCorreo = findViewById(R.id.btnGuardarCorreo)

        btnGuardarCorreo.setOnClickListener {
            val user = auth.currentUser
            val newEmail = etNuevoCorreo.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (user != null) {
                pedirReautenticacionYActualizar(user, newEmail)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Acción al presionar el botón de regreso
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun pedirReautenticacionYActualizar(user: FirebaseUser, newEmail: String) {
        val passwordInput = EditText(this)
        passwordInput.hint = "Ingresa tu contraseña actual"

        AlertDialog.Builder(this)
            .setTitle("Re-autenticación requerida")
            .setMessage("Para actualizar tu correo, ingresa tu contraseña actual")
            .setView(passwordInput)
            .setPositiveButton("Aceptar") { _, _ ->
                val password = passwordInput.text.toString().trim()
                if (password.isEmpty()) {
                    Toast.makeText(this, "Contraseña vacía", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        actualizarCorreo(user, newEmail)
                    } else {
                        Toast.makeText(
                            this,
                            "Error de re-autenticación: ${authTask.exception?.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarCorreo(user: FirebaseUser, newEmail: String) {
        user.updateEmail(newEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.sendEmailVerification().addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Se envió un correo de confirmación a $newEmail",
                            Toast.LENGTH_LONG
                        ).show()
                        finish() // volvemos a Perfil después de actualizar
                    } else {
                        val errorMsg =
                            emailTask.exception?.localizedMessage ?: "No se pudo enviar el correo de confirmación"
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                val exception = task.exception
                val errorCode = (exception as? FirebaseAuthException)?.errorCode
                val message = exception?.localizedMessage ?: "Error desconocido"

                when (errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" ->
                        Toast.makeText(this, "El correo $newEmail ya está en uso", Toast.LENGTH_LONG)
                            .show()

                    "ERROR_INVALID_EMAIL" ->
                        Toast.makeText(this, "El correo no es válido", Toast.LENGTH_LONG).show()

                    "ERROR_REQUIRES_RECENT_LOGIN" ->
                        Toast.makeText(
                            this,
                            "Debes reautenticarte nuevamente para cambiar el correo.",
                            Toast.LENGTH_LONG
                        ).show()

                    else ->
                        Toast.makeText(
                            this,
                            "Error al actualizar correo: $message",
                            Toast.LENGTH_LONG
                        ).show()
                }
            }
        }
    }
}
