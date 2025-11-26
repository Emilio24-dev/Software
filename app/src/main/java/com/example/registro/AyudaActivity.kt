package com.example.registro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class AyudaActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ayuda)

        val tvBack = findViewById<TextView>(R.id.tvBackAyudaManual)
        tvBack.setOnClickListener { finish() }

        val btnVideoReportes = findViewById<Button>(R.id.btnVideoReportes)
        val urlVideoReportes = "https://youtube.com/shorts/uK8S0OLFZZs?si=nBJrcePfxRJ24Zhk"

        val btnVideoSugerencias = findViewById<Button>(R.id.btnVideoSuger)
        val urlVideoSugerencias = "https://youtube.com/shorts/FFoD7x85Bt4?si=b_bEW7BBS-EBcMi7"

        val btnVideoConsultas = findViewById<Button>(R.id.btnVideoConsul)
        val urlVideoConsultas = "https://youtube.com/shorts/br-n6EOHTaI?si=W9HLUHktiyozE47F"

        val btnVideoIdioma = findViewById<Button>(R.id.btnVideoLenguage)
        val urlVideoIdioma = "https://youtube.com/shorts/DpEibSmDkbY?si=t0ZLzuXmmoNTabaL"

        btnVideoReportes.setOnClickListener {
            abrirVideo(urlVideoReportes)
        }

        btnVideoSugerencias.setOnClickListener {
            abrirVideo(urlVideoSugerencias)
        }

        btnVideoConsultas.setOnClickListener {
            abrirVideo(urlVideoConsultas)
        }

        btnVideoIdioma.setOnClickListener {
            abrirVideo(urlVideoIdioma)
        }



    }

    private fun abrirVideo(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        // Android intentará abrir YouTube; si no, el navegador
        startActivity(intent)
    }
}