package com.example.registro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ServicioActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    private val correoSoporte = "sentrifox2025@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_servicio)

        val tvBack = findViewById<TextView>(R.id.tvBackAyuda)
        val btnContactanos = findViewById<Button>(R.id.btnContactanos)
        val etConsulta = findViewById<EditText>(R.id.etConsultaServicio)

        // Flecha / texto para regresar
        tvBack.setOnClickListener { finish() }

        // Botón "Contáctanos"
        btnContactanos.setOnClickListener {

            val consulta = etConsulta.text.toString().trim()

            if (consulta.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor escribe en qué podemos ayudarte.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Info extra del usuario (igual que en reportes/sugerencias)
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
                Consulta del usuario:

                $consulta

                $infoExtra
            """.trimIndent()

            // 1) Intent principal: ACTION_SENDTO con mailto:
            val intentSendTo = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")                           // OJO: solo "mailto:"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(correoSoporte))  // destinatario
                putExtra(Intent.EXTRA_SUBJECT, "Soporte Sentri - Consulta de usuario")
                putExtra(Intent.EXTRA_TEXT, cuerpoCorreo)
            }

            val pm = packageManager

            if (intentSendTo.resolveActivity(pm) != null) {
                startActivity(Intent.createChooser(intentSendTo, "Contactar con..."))
                etConsulta.text?.clear()
                Toast.makeText(
                    this,
                    "Tu consulta ha sido preparada, envíala desde tu app de correo.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // 2) Fallback: ACTION_SEND (más genérico)
                val intentSend = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(correoSoporte))
                    putExtra(Intent.EXTRA_SUBJECT, "Soporte Sentri - Consulta de usuario")
                    putExtra(Intent.EXTRA_TEXT, cuerpoCorreo)
                }

                if (intentSend.resolveActivity(pm) != null) {
                    startActivity(Intent.createChooser(intentSend, "Contactar con..."))
                    etConsulta.text?.clear()
                    Toast.makeText(
                        this,
                        "Tu consulta ha sido preparada, envíala desde tu app de correo.",
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
}