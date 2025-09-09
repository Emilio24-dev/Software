package com.daniela.envivo

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class LiveActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    // Puedes sobreescribirla pasando un extra "rtspUrl" en el Intent
    private var rtspUrl: String = "rtsp://admin:1%402b3c4d@192.168.1.64:554/Streaming/Channels/102"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)

        playerView = findViewById(R.id.playerView)
        // Si viene por Intent, úsala
        intent.getStringExtra("rtspUrl")?.let { rtspUrl = it }
    }

    private fun initializePlayer() {
        val exo = ExoPlayer.Builder(this).build()
        playerView.player = exo

        val item = MediaItem.Builder()
            .setUri(Uri.parse(rtspUrl))
            .setMimeType(MimeTypes.APPLICATION_RTSP)
            .build()

        exo.setMediaItem(item)
        exo.prepare()
        exo.playWhenReady = true

        exo.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(
                    this@LiveActivity,
                    "No se pudo reproducir RTSP: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        player = exo
    }

    private fun releasePlayer() {
        playerView.player = null
        player?.release()
        player = null
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }
}
