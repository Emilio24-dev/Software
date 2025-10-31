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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log
import android.widget.Toast
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

        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            /* context = */ this,
            /* fileName = */ "hc_prefs",
            /* masterKey = */ masterKey,
            /* keyScheme = */ EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            /* valueScheme = */ EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

// ⚠
        prefs.edit()
            .putString("HC_USER", "+50375208922")
            .putString("HC_PASS", "1@2b3c4d")
            .apply()
// ====== FIN guardar credenciales ======
        recButton.setOnClickListener {
            startActivity(Intent(this, grabaciones::class.java))
        }



        // 🚀 Acceso en vivo hikguiño
        camButton.setOnClickListener {
            startActivity(Intent(this, LiveActivity::class.java))
        }

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


        recButton.setOnClickListener {
            // abrir la pantalla de las grabaciones (la lista de fechas)
            startActivity(
                Intent(
                    this@Inicio,
                    grabaciones::class.java
                )
            )
        }

        // 👉 Botón Notificaciones (ejemplo)
        notButton.setOnClickListener {
            // Aquí puedes enlazar otra Activity (ejemplo: NotificacionesActivity)
            startActivity(Intent(this, AlertasActivity::class.java))




        }
    }
}
