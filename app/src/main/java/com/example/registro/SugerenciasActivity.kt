package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SugerenciasActivity : AppCompatActivity() {

    private val correoSoporte = "sentrifox2025@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sugerencias)

        val tvBack = findViewById<TextView>(R.id.tvBackSuger)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcionSugerencia)
        val btnEnviar = findViewById<Button>(R.id.btnEnviarSoporte)

        // Flecha / texto para regresar
        tvBack.setOnClickListener { finish() }

        // Botón Enviar sugerencia
        btnEnviar.setOnClickListener {
            val descripcion = etDescripcion.text.toString().trim()

            if (descripcion.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor escribe tu sugerencia.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Info extra (igual que en reportes)
            val user = FirebaseAuth.getInstance().currentUser
            val correoUsuario = user?.email ?: "usuario no autenticado"

            val infoExtra = """
                --------------------------
                Datos técnicos
                Correo usuario: $correoUsuario
                Dispositivo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                Android: ${android.os.Build.VERSION.RELEASE}
            """.trimIndent()

            val cuerpoCorreo = """
                Sugerencia del usuario:
                
                $descripcion

                $infoExtra
            """.trimIndent()

            // Intent SOLO para apps de correo (igual que en ReportesActivity)
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(correoSoporte))
                putExtra(Intent.EXTRA_SUBJECT, "Sentri - Nueva sugerencia de usuario")
                putExtra(Intent.EXTRA_TEXT, cuerpoCorreo)
            }

            val pm = packageManager
            if (emailIntent.resolveActivity(pm) != null) {
                startActivity(Intent.createChooser(emailIntent, "Selecciona tu app de correo"))
                etDescripcion.text?.clear()

                Toast.makeText(
                    this,
                    "Tu sugerencia ha sido preparada y enviada al correo.",
                    Toast.LENGTH_LONG
                ).show()


            } else {
                Toast.makeText(
                    this,
                    "No se encontró ninguna aplicación de correo en el dispositivo.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}