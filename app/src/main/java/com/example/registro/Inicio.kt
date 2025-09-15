package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView


class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)


        // 🔹 Configuración del menú inferior
       val itemperfil: ImageButton = findViewById(R.id.itemPerfil)
        val itemhome: ImageButton = findViewById(R.id.itemHome)
        val itemsetts: ImageButton = findViewById(R.id.itemSetts)

        // 🔹 Configuración de botones principales
        val camButton: ImageButton = findViewById(R.id.btCam)
        val recButton: ImageButton = findViewById(R.id.btRec)
        val notButton: ImageButton = findViewById(R.id.btNot)

        // 👉 Botón Perfil → abre PerfilActivity
        itemperfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        // 👉 Botón Cámara → abre LiveActivity
        camButton.setOnClickListener {
            val intent = Intent(this, LiveActivity::class.java)
            // Si quieres abrir otra URL distinta de la que está por defecto:
            // intent.putExtra("rtspUrl", "rtsp://otra_ip/stream")
            startActivity(intent)
        }

        // 👉 Botón Archivos (ejemplo, si quieres enlazarlo luego)
        recButton.setOnClickListener {
            // Aquí puedes enlazar otra Activity (por ejemplo, una lista de grabaciones)
            // startActivity(Intent(this, RecordingsActivity::class.java))
        }

        // 👉 Botón Notificaciones (ejemplo)
        notButton.setOnClickListener {
            // Aquí puedes enlazar otra Activity (ejemplo: NotificacionesActivity)
            // startActivity(Intent(this, NotificationsActivity::class.java))
        }
    }
}
