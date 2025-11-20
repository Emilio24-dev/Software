package com.example.registro

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView

@UnstableApi
class SingleCameraActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var btnBackLista: TextView

    private var camName: String = "Cámara"
    private var camUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_camera)

        playerView = findViewById(R.id.playerViewSingle)
        btnBackLista = findViewById(R.id.btnBackLista)

        // Recibe los datos enviados desde LiveActivity
        camName = intent.getStringExtra("cam_name") ?: "Cámara"
        camUrl = intent.getStringExtra("cam_url") ?: ""

        if (camUrl.isBlank()) {
            Toast.makeText(this, "URL de cámara no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnBackLista.setOnClickListener { finish() }

        initPlayer()
    }

    private fun initPlayer() {
        val exo = ExoPlayer.Builder(this).build().also { p ->
            playerView.player = p

            p.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    val cause = error.cause
                    val debugMsg = buildString {
                        append("RTSP error ($camName): ${error.errorCodeName}")
                        if (cause != null) {
                            append(" | cause=${cause.message}")
                        }
                    }
                    Log.e("SingleCamera", debugMsg)

                    Toast.makeText(
                        this@SingleCameraActivity,
                        "Error en $camName: ${error.errorCodeName}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        player = exo

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(camUrl))
            .setMimeType(MimeTypes.APPLICATION_RTSP)
            .build()

        val rtspFactory = RtspMediaSource.Factory()
            .setForceUseRtpTcp(true)
            .setTimeoutMs(10_000)

        val mediaSource = rtspFactory.createMediaSource(mediaItem)

        exo.setMediaSource(mediaSource)
        exo.prepare()
        exo.playWhenReady = true
    }

    override fun onStart() {
        super.onStart()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
