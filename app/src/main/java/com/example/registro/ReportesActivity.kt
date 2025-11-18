package com.example.registro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ReportesActivity : AppCompatActivity() {

    private val correoSoporte = "sentrifox2025@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        val tvBack = findViewById<TextView>(R.id.tvBackReportar)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcionSoporte)
        val btnEnviar = findViewById<Button>(R.id.btnEnviarSoporte)

        tvBack.setOnClickListener { finish() }

        btnEnviar.setOnClickListener {
            val descripcion = etDescripcion.text.toString().trim()

            if (descripcion.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor escribe una descripción del problema.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Info extra del usuario (si está autenticado)
            val user = FirebaseAuth.getInstance().currentUser
            val correoUsuario = user?.email ?: "usuario no autenticado"

            val infoExtra = """
                --------------------------
                Datos técnicos
                Correo usuario: $correoUsuario
                Dispositivo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                Android: ${android.os.Build.VERSION.RELEASE}
            """.trimIndent()

            val cuerpoCorreo = "$descripcion\n\n$infoExtra"



            // 1) Intent principal: ACTION_SENDTO con mailto:
            val intentSendTo = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")                           // importante: solo "mailto:"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(correoSoporte))  // destinatario
                putExtra(Intent.EXTRA_SUBJECT, "Soporte Sentri - Reporte de problema")
                putExtra(Intent.EXTRA_TEXT, cuerpoCorreo)
            }

            val pm = packageManager

            if (intentSendTo.resolveActivity(pm) != null) {
                // Hay app que maneja ACTION_SENDTO (Gmail, etc.)
                startActivity(Intent.createChooser(intentSendTo, "Enviar reporte con..."))
                etDescripcion.text?.clear()
            } else {
                // 2) Fallback: ACTION_SEND (más genérico)
                val intentSend = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(correoSoporte))
                    putExtra(Intent.EXTRA_SUBJECT, "Soporte Sentri - Reporte de problema")
                    putExtra(Intent.EXTRA_TEXT, cuerpoCorreo)
                }

                if (intentSend.resolveActivity(pm) != null) {
                    startActivity(Intent.createChooser(intentSend, "Enviar reporte con..."))
                    etDescripcion.text?.clear()
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
}