package com.example.registro

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Calendar
import java.util.TimeZone

class PlayerFullscreenActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var fullscreenPlayerView: PlayerView
    private lateinit var btnClose: ImageView
    private lateinit var tvTitleCam: TextView

    private val storage = Firebase.storage.reference.child("live")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // si quieres forzar landscape siempre, descomenta:
        // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        setContentView(R.layout.activity_fullscreen_player)

        fullscreenPlayerView = findViewById(R.id.fullscreenPlayerView)
        btnClose = findViewById(R.id.btnClose)
        tvTitleCam = findViewById(R.id.tvTitleCam)

        // cuál cam nos pidieron ver
        val cam = intent.getIntExtra("cam", 1)
        tvTitleCam.text = "CAM $cam"

        // Este es el archivo EXACTO que estaba sonando en LiveActivity
        val passedFileName = intent.getStringExtra("fileName")

        // Creamos player dedicado a fullscreen
        player = ExoPlayer.Builder(this).build()
        fullscreenPlayerView.player = player

        // Decidimos qué archivo reproducir:
        val finalFileName = passedFileName ?: run {
            // fallback (casi nunca debería pasar) -> calculamos según hora
            if (cam == 1) pickFileNameCam1Fallback() else pickFileNameCam2Fallback()
        }

        // Cargamos el clip en el player del fullscreen
        storage.child(finalFileName).downloadUrl
            .addOnSuccessListener { uri ->
                val item = MediaItem.fromUri(uri)
                player?.setMediaItem(item)
                player?.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                player?.prepare()
                player?.playWhenReady = true
            }
            .addOnFailureListener { e ->
                // si falla firebase, pues no reproducimos y ni modo
                // podrías hacer un Toast aquí si querés debug:
                // Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

        btnClose.setOnClickListener {
            player?.release()
            player = null
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    // ---------- FALLBACK CAM1 ----------
    // mismas reglas nuevas que ya pusimos en LiveActivity.pickFileNameCam1()
    private fun pickFileNameCam1Fallback(): String {
        // CAM1:
        // Morning: 06:00 - 10:40 -> C0309 / C0311 alternando par/impar minuto
        // Tarde:   10:40 - 21:00 -> C0315 / C0314 alternando par/impar
        // NocheA:  21:00 - 23:50 -> C0337
        // NocheB:  23:51 - 06:59 -> C0321
        // fallback: C0337

        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("America/El_Salvador")

        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        val totalMin = h * 60 + m

        fun between(h1:Int, m1:Int, h2:Int, m2:Int): Boolean {
            val a = h1*60 + m1
            val b = h2*60 + m2
            return totalMin in a..b
        }

        val morning = between(6,0,10,40)
        val tarde   = between(10,40,21,0)
        val nocheA  = between(21,0,23,50)
        val nocheB1 = between(23,51,23,59)
        val nocheB2 = between(0,0,6,59)

        return when {
            morning -> if (m % 2 == 0) "C0309.MP4" else "C0311.MP4"
            tarde   -> if (m % 2 == 0) "C0315.MP4" else "C0314.MP4"
            nocheA  -> "C0337.MP4"
            (nocheB1 || nocheB2) -> "C0321.MP4"
            else -> "C0337.MP4"
        }
    }

    // ---------- FALLBACK CAM2 ----------
    // mismas reglas nuevas que ya pusimos en LiveActivity.pickFileNameCam2()
    private fun pickFileNameCam2Fallback(): String {
        // CAM2:
        // Morning: 06:00 - 10:39 -> C0318
        // Tarde:   10:40 - 18:09 -> C0319
        // Noche1:  18:10 - 20:50 -> C0333
        // Noche2:  20:51 - 05:59 -> C0325
        // fallback: C0319

        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("America/El_Salvador")

        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        val totalMin = h * 60 + m

        fun between(h1:Int, m1:Int, h2:Int, m2:Int): Boolean {
            val a = h1*60 + m1
            val b = h2*60 + m2
            return totalMin in a..b
        }

        val morning = between(6,0,10,39)
        val tarde   = between(10,40,18,9)
        val noche1  = between(18,10,20,50)
        val noche2a = between(20,51,23,59)
        val noche2b = between(0,0,5,59)

        return when {
            morning -> "C0318.MP4"
            tarde   -> "C0319.MP4"
            noche1  -> "C0333.MP4"
            (noche2a || noche2b) -> "C0325.MP4"
            else -> "C0319.MP4"
        }
    }
}

