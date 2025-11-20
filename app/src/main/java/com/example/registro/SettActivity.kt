package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.jvm.java

class SettActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sett)

        // ← Atrás
        findViewById<TextView>(R.id.tvBackConfig)?.setOnClickListener { finish() }

        // Idioma
        findViewById<Button>(R.id.btnIdioma)?.setOnClickListener {
            startActivity(Intent(this, IdiomaActivity::class.java))
        }

        // ===== Soporte (diagnóstico + navegación) =====
        val btnSoporte = findViewById<Button>(R.id.btnSoporte)
        if (btnSoporte == null) {
            Toast.makeText(this, "btnSoporte = null. Revisa el layout de activity_sett.xml", Toast.LENGTH_LONG).show()
            Log.e("SettActivity", "btnSoporte es null. ¿El id @+id/btnSoporte existe en activity_sett.xml?")
        } else {
            btnSoporte.setOnClickListener {
                Toast.makeText(this, "Click en Soporte", Toast.LENGTH_SHORT).show()
                try {
                    startActivity(Intent(this, SoporteActivity::class.java))
                } catch (e: Exception) {
                    Toast.makeText(this, "No se pudo abrir Soporte: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("SettActivity", "Error abriendo SoporteActivity", e)
                }
            }
        }
        // ===== fin soporte =====

        findViewById<Button>(R.id.btnSugerencias)?.setOnClickListener {
            startActivity(Intent(this, SugerenciasActivity::class.java))
        }

        // Notificaciones (si la tienes)
        findViewById<Button>(R.id.btnNotificaciones)?.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }

        // Tema / Cerrar sesión (pendiente)
        //findViewById<Button>(R.id.btnTema)?.setOnClickListener { /* TODO */ }
        findViewById<Button>(R.id.btnCerrarSesion)?.setOnClickListener { /* TODO */ }



        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
