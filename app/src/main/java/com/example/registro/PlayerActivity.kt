package com.example.registro

import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private var playlist: ArrayList<String>? = null
    private var label: String? = null
    private var nvrId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)

        val tvFecha = findViewById<TextView>(R.id.tvFechaGrabacion)
        val tvCam = findViewById<TextView>(R.id.tvCamGrabacion)

        // Botón volver
        findViewById<TextView>(R.id.tvBackPlayer).setOnClickListener {
            finish()
        }

        // Datos recibidos
        playlist = intent.getStringArrayListExtra("playlist")
        label = intent.getStringExtra("label")
        nvrId = intent.getStringExtra("nvrId")

        // Fallback del nvrId
        if (nvrId.isNullOrEmpty()) {
            val user = FirebaseAuth.getInstance().currentUser
            nvrId = user?.uid
        }

        if (playlist.isNullOrEmpty() || nvrId.isNullOrEmpty()) {
            Toast.makeText(this, "No se pudo cargar la grabación.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Título arriba
        tvFecha.text = label ?: "Grabaciones del día"

        // ⚠ Obtener la cámara desde el primer clip del playlist
        val firstName = playlist!!.first()
        val base = firstName.substringBeforeLast(".")
        val camSlug = base.substringAfterLast("_") // ejemplo sala → SALA

        tvCam.text = camSlug.replace("_", " ").uppercase()

        inicializarPlayerConPlaylist(playlist!!, nvrId!!)
    }

    private fun inicializarPlayerConPlaylist(names: List<String>, nvrId: String) {

        val storageRoot = Firebase.storage.reference
            .child("recordings")
            .child(nvrId)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        var started = false

        for (fileName in names) {
            val ref = storageRoot.child(fileName)

            ref.downloadUrl
                .addOnSuccessListener { uri ->
                    val item = MediaItem.fromUri(uri)
                    player?.addMediaItem(item)

                    if (!started) {
                        started = true
                        player?.prepare()
                        player?.playWhenReady = true
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error cargando $fileName", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
