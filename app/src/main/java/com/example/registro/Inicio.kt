package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.widget.Toast

class Inicio : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // ---------- MENÚ SUPERIOR ----------
        val itemperfil: ImageButton = findViewById(R.id.itemPerfil)
        val itemhome: ImageButton = findViewById(R.id.itemHome)
        val itemsetts: ImageButton = findViewById(R.id.itemSetts)

        itemsetts.setOnClickListener {
            startActivity(Intent(this, SettActivity::class.java))
        }

        itemperfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        itemhome.setOnClickListener {
            // No hace nada porque ya estás en Home
        }

        // ---------- BOTONES PRINCIPALES ----------
        val camButton: ImageButton = findViewById(R.id.btCam)
        val recButton: ImageButton = findViewById(R.id.btRec)
        val notButton: ImageButton = findViewById(R.id.btNot)

        // Credenciales Hikvision (como ya lo tenías)
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            this,
            "hc_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        prefs.edit()
            .putString("HC_USER", "+50375208922")
            .putString("HC_PASS", "1@2b3c4d")
            .apply()

        // ---------- EN VIVO ----------
        camButton.setOnClickListener {
            startActivity(Intent(this, LiveActivity::class.java))
        }

        // ---------- GRABACIONES (TOQUE NORMAL) ----------
        recButton.setOnClickListener {
            startActivity(Intent(this, grabaciones::class.java))
        }

        // ---------- VINCULAR NVR (DEJAR PRESIONADO) ----------
        recButton.setOnLongClickListener {
            startActivity(Intent(this, VincularNvrActivity::class.java))
            Toast.makeText(this, "Abriendo Vinculación de NVR…", Toast.LENGTH_SHORT).show()
            true
        }

        // ---------- ALERTAS ----------
        notButton.setOnClickListener {
            startActivity(Intent(this, AlertasActivity::class.java))
        }
    }
}
