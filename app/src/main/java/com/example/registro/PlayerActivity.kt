package com.example.registro

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PlayerActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    private lateinit var pv: PlayerView
    private var player: ExoPlayer? = null

    // opcional UI extra para mostrar IA y fecha
    private var tvTagIA: TextView? = null
    private var tvFecha: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        pv = findViewById(R.id.playerView)
        tvTagIA = findViewById(R.id.tvTagIA)
        tvFecha = findViewById(R.id.tvFechaIA)

        player = ExoPlayer.Builder(this).build()
        pv.player = player

        val storageRefName = intent.getStringExtra("storageRef")
        val tagIA = intent.getStringExtra("aiTag")
        val fechaClip = intent.getStringExtra("fecha")
        val localUrl = intent.getStringExtra("url")

        tvTagIA?.text = tagIA ?: "Evento"
        tvFecha?.text = fechaClip ?: ""

        when {
            !storageRefName.isNullOrEmpty() -> reproducirDesdeFirebase(storageRefName)
            !localUrl.isNullOrEmpty() -> reproducirDesdeLocal(localUrl)
            else -> Toast.makeText(this, "No se encontró la grabación", Toast.LENGTH_LONG).show()
        }
    }

    private fun reproducirDesdeLocal(url: String) {
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    private fun reproducirDesdeFirebase(storageRefName: String) {
        val ref = Firebase.storage.reference.child("live").child(storageRefName)
        ref.downloadUrl
            .addOnSuccessListener { uri ->
                val mediaItem = MediaItem.fromUri(uri)
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.playWhenReady = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando clip: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}

