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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PlayerActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Bloque que traduce las palabras de español a ingles
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

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

        // Botón "< Grabaciones del día"
        findViewById<TextView>(R.id.tvBackPlayer).setOnClickListener {
            finish()
        }

        // Datos que vienen de GrabacionesDetalleActivity
        playlist = intent.getStringArrayListExtra("playlist")
        label = intent.getStringExtra("label")
        nvrId = intent.getStringExtra("nvrId")

        // Fallback: si no viene nvrId, usamos el uid del usuario logueado
        if (nvrId.isNullOrEmpty()) {
            val user = FirebaseAuth.getInstance().currentUser
            nvrId = user?.uid
        }

        if (playlist.isNullOrEmpty() || nvrId.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "No se pudo cargar la grabación.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        tvFecha.text = label ?: "Grabaciones del día"

        // Si es sólo un clip, deducimos nombre de la cámara
        val firstName = playlist!!.first()
        val camSlug = firstName.substringBeforeLast(".").substringAfterLast("_")
        val camLabel = camSlug.replace("_", " ").uppercase()
        tvCam.text = camLabel // Ej: "SALA", "CAM1", etc.

        // Cargar playlist respetando el orden
        cargarPlaylistEnOrden(playlist!!, nvrId!!)
    }

    /**
     * Descarga las URLs de la playlist y las mete al player
     * en el MISMO orden de la lista 'names'.
     */
    private fun cargarPlaylistEnOrden(names: List<String>, nvrId: String) {
        val storageRoot = Firebase.storage.reference
            .child("recordings")
            .child(nvrId)

        // Lista de MediaItem con la misma longitud que la playlist
        val mediaItems = MutableList<MediaItem?>(names.size) { null }
        var pendientes = names.size

        for ((index, fileName) in names.withIndex()) {
            val ref = storageRoot.child(fileName)

            ref.downloadUrl
                .addOnSuccessListener { uri: Uri ->
                    mediaItems[index] = MediaItem.fromUri(uri)
                    pendientes--

                    if (pendientes == 0) {
                        // Ya tenemos todas las URLs → armamos el player en orden
                        inicializarPlayerConMediaItems(mediaItems.filterNotNull())
                    }
                }
                .addOnFailureListener {
                    pendientes--
                    Toast.makeText(
                        this,
                        "Error cargando $fileName",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (pendientes == 0) {
                        val listOk = mediaItems.filterNotNull()
                        if (listOk.isNotEmpty()) {
                            inicializarPlayerConMediaItems(listOk)
                        } else {
                            Toast.makeText(
                                this,
                                "No se pudo cargar ningún clip.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    }
                }
        }
    }

    private fun inicializarPlayerConMediaItems(items: List<MediaItem>) {
        if (items.isEmpty()) return

        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo

            // Añadir en orden
            for (item in items) {
                exo.addMediaItem(item)
            }

            exo.prepare()
            exo.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
