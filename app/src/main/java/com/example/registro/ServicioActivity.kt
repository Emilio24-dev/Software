package com.example.registro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ServicioActivity : AppCompatActivity() {

    private val correoSoporte = "sentrifox2025@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_servicio)

        val tvBack = findViewById<TextView>(R.id.tvBackAyuda)
        val btnContactanos = findViewById<Button>(R.id.btnContactanos)

        // Flecha / texto para regresar
        tvBack.setOnClickListener { finish() }

        // Botón "Contáctanos"
        btnContactanos.setOnClickListener {

            // 1) Intent principal: ACTION_SENDTO con mailto:
            val intentSendTo = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")                           // OJO: solo "mailto:"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(correoSoporte))  // destinatario
                putExtra(Intent.EXTRA_SUBJECT, "Soporte Sentri - Solicitud de ayuda")
                putExtra(Intent.EXTRA_TEXT, "Hola, necesito ayuda con la aplicación Sentri.")
            }

            val pm = packageManager

            if (intentSendTo.resolveActivity(pm) != null) {
                startActivity(Intent.createChooser(intentSendTo, "Contactar con..."))
            } else {
                // 2) Fallback: ACTION_SEND (más genérico)
                val intentSend = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(correoSoporte))
                    putExtra(Intent.EXTRA_SUBJECT, "Soporte Sentri - Solicitud de ayuda")
                    putExtra(Intent.EXTRA_TEXT, "Hola, necesito ayuda con la aplicación Sentri.")
                }

                if (intentSend.resolveActivity(pm) != null) {
                    startActivity(Intent.createChooser(intentSend, "Contactar con..."))
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