package com.example.registro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SoporteActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soporte)

        val tvBack = findViewById<TextView>(R.id.tvBackSoporte)
        val rowReportar = findViewById<LinearLayout>(R.id.rowReportar)
        val rowAyuda = findViewById<LinearLayout>(R.id.rowAyuda)

        // Flecha de regreso
        tvBack.setOnClickListener { finish() }

        // Panel "Reportar un problema" -> abre pantalla para escribir y enviar
        rowReportar.setOnClickListener {
            val intent = Intent(this, ReportesActivity::class.java)
            startActivity(intent)
        }

        // Panel "Servicio de ayuda" -> abre pantalla de contacto
        rowAyuda.setOnClickListener {
            val intent = Intent(this, ServicioActivity::class.java)
            startActivity(intent)
        }
    }
}