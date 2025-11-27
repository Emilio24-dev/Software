package com.example.registro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView
import com.example.registro.IA.IaClient
import com.example.registro.IA.YoloDetection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@UnstableApi
class LiveActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    data class Camera(val name: String, val url: String)

    private lateinit var containerCamaras: LinearLayout
    private lateinit var btnBackInicio: TextView
    private lateinit var btnAgregarCamara: Button

    private val cameras = mutableListOf<Camera>()
    private val players = mutableListOf<ExoPlayer>()

    // Handler para el loop de detecciones
    private val detectionHandler = Handler(Looper.getMainLooper())
    private val detectionIntervalMs = 1000L // cada 1 segundo

    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid
    private val db get() = FirebaseDatabase.getInstance().getReference("Usuarios")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)

        containerCamaras = findViewById(R.id.containerCamaras)
        btnBackInicio = findViewById(R.id.btnBackInicio)
        btnAgregarCamara = findViewById(R.id.btnAgregarCamara)

        // Cargar cámaras del usuario
        loadCamerasFromFirebase {
            renderAllCameras()
        }

        btnBackInicio.setOnClickListener {
            val intent = Intent(this, Inicio::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        btnAgregarCamara.setOnClickListener {
            mostrarDialogoAgregarCamara()
        }
    }

    // ============================================================
    //  FIREBASE: Cargar y guardar cámaras por cuenta (UID)
    // ============================================================

    private fun loadCamerasFromFirebase(onLoaded: () -> Unit) {
        cameras.clear()

        val uid = userId ?: return onLoaded()

        db.child(uid).child("camaras")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (camSnap in snapshot.children) {
                        val name = camSnap.child("name").value?.toString() ?: continue
                        val url = camSnap.child("url").value?.toString() ?: continue
                        cameras.add(Camera(name, normalizeRtspUrl(url)))
                    }
                }
                onLoaded()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar cámaras", Toast.LENGTH_SHORT).show()
                onLoaded()
            }
    }

    private fun saveCamerasToFirebase() {
        val uid = userId ?: return

        val map = cameras.mapIndexed { index, cam ->
            index.toString() to mapOf(
                "name" to cam.name,
                "url" to cam.url
            )
        }.toMap()

        db.child(uid).child("camaras").setValue(map)
    }

    // ============================================================
    //                       UI y RENDER
    // ============================================================

    private fun renderAllCameras() {
        containerCamaras.removeAllViews()
        releasePlayers()

        cameras.forEachIndexed { index, cam ->
            addCameraView(cam, index)
        }
    }

    private fun addCameraView(camera: Camera, index: Int) {
        // Usamos item_cam.xml
        val card = layoutInflater.inflate(
            R.layout.item_cam,
            containerCamaras,
            false
        ) as LinearLayout

        val tvCam = card.findViewById<TextView>(R.id.tvCam)
        val tvInfo = card.findViewById<TextView>(R.id.tvInfo)
        val playerView = card.findViewById<PlayerView>(R.id.playerView)
        val overlay = card.findViewById<YoloDetectionsOverlayView>(R.id.overlayDetections)

        tvCam.text = camera.name
        tvInfo.text = "Conectando..."

        containerCamaras.addView(card)

        // Abrir cámara en pantalla completa (tap normal)
        card.setOnClickListener {
            val intent = Intent(this, SingleCameraActivity::class.java).apply {
                putExtra("cam_name", camera.name)
                putExtra("cam_url", camera.url)
            }
            startActivity(intent)
        }

        // Eliminar cámara (long press)
        card.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar cámara")
                .setMessage("¿Eliminar \"${camera.name}\"?")
                .setPositiveButton("Sí") { _, _ ->
                    val currentIndex = cameras.indexOfFirst { it == camera }
                    if (currentIndex != -1) {
                        cameras.removeAt(currentIndex)
                        saveCamerasToFirebase()
                        renderAllCameras()
                    }
                }
                .setNegativeButton("No", null)
                .show()
            true
        }

        // Player
        val player = ExoPlayer.Builder(this).build().also { p ->
            playerView.player = p

            p.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Toast.makeText(
                        this@LiveActivity,
                        "Error en ${camera.name}: ${error.errorCodeName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    tvInfo.text = "Error: ${error.errorCodeName}"
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_READY) {
                        tvInfo.text = "Transmitiendo..."
                    }
                }
            })
        }

        players.add(player)
        reproducirUrlEnPlayer(camera.url, player)

        // Iniciar loop de detecciones para esta cámara
        startDetectionLoopForCamera(playerView, overlay)
    }

    private fun reproducirUrlEnPlayer(url: String, player: ExoPlayer) {
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(url))
            .setMimeType(MimeTypes.APPLICATION_RTSP)
            .build()

        val rtspFactory = RtspMediaSource.Factory()
            .setForceUseRtpTcp(true)
            .setTimeoutMs(10_000)

        val mediaSource = rtspFactory.createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    // ============================================================
    //   LOOP DE DETECCIONES (envía frames al microservicio YOLO)
    // ============================================================

    private fun startDetectionLoopForCamera(
        playerView: PlayerView,
        overlay: YoloDetectionsOverlayView
    ) {
        val runnable = object : Runnable {
            override fun run() {
                try {
                    // 1. Capturar un frame del PlayerView
                    val bitmap = try {
                        val surfaceView = playerView.videoSurfaceView
                        if (surfaceView != null) {
                            surfaceView.drawToBitmap()
                        } else {
                            playerView.drawToBitmap()
                        }
                    } catch (e: Exception) {
                        playerView.drawToBitmap()
                    }

                    // 2. Mandar al microservicio usando detectFromBitmap
                    IaClient.detectFromBitmap(bitmap) { ok, detections, imageWidth, imageHeight, _ ->
                        if (ok) {
                            val boxes = detectionsToBoxes(detections, imageWidth, imageHeight)
                            runOnUiThread {
                                overlay.setDetections(boxes)
                            }
                        } else {
                            runOnUiThread {
                                overlay.clearDetections()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    // Volver a ejecutar después de detectionIntervalMs
                    detectionHandler.postDelayed(this, detectionIntervalMs)
                }
            }
        }

        detectionHandler.post(runnable)
    }

    /**
     * Convierte las detecciones del microservicio (YoloDetection) a
     * cajas listas para dibujar en el overlay (YoloBox).
     */
    private fun detectionsToBoxes(
        detections: List<YoloDetection>,
        imageWidth: Int,
        imageHeight: Int
    ): List<YoloBox> {
        val result = mutableListOf<YoloBox>()
        if (imageWidth <= 0 || imageHeight <= 0) return result

        for (det in detections) {
            result.add(
                YoloBox(
                    x1 = det.x1,
                    y1 = det.y1,
                    x2 = det.x2,
                    y2 = det.y2,
                    label = det.className,
                    confidence = det.confidence,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )
            )
        }
        return result
    }

    // ============================================================
    //               DIÁLOGO PARA AGREGAR CÁMARAS
    // ============================================================

    private fun mostrarDialogoAgregarCamara() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
        }

        val inputNombre = EditText(this).apply { hint = "Nombre de la cámara" }
        val inputUrl = EditText(this).apply { hint = "rtsp://usuario:pass@ip:554/Streaming/Channels/101" }

        layout.addView(inputNombre)
        layout.addView(inputUrl)

        AlertDialog.Builder(this)
            .setTitle("Agregar cámara")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val urlRaw = inputUrl.text.toString().trim()

                if (nombre.isEmpty() || urlRaw.isEmpty()) {
                    Toast.makeText(this, "Debes llenar ambos campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val url = normalizeRtspUrl(urlRaw)

                cameras.add(Camera(nombre, url))
                saveCamerasToFirebase()
                renderAllCameras()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ============================================================
    //                       UTILIDADES
    // ============================================================

    private fun normalizeRtspUrl(input: String): String {
        val trimmed = input.trim()
        if (!trimmed.startsWith("rtsp://")) return trimmed

        val noScheme = trimmed.removePrefix("rtsp://")
        val atIndex = noScheme.lastIndexOf('@')
        if (atIndex == -1) return trimmed

        val userInfoRaw = noScheme.substring(0, atIndex)
        val hostAndPath = noScheme.substring(atIndex + 1)

        val colonIndex = userInfoRaw.indexOf(':')
        if (colonIndex == -1) return trimmed

        val userRaw = userInfoRaw.substring(0, colonIndex)
        val passRaw = userInfoRaw.substring(colonIndex + 1)

        val userEnc = Uri.encode(Uri.decode(userRaw))
        val passEnc = Uri.encode(Uri.decode(passRaw))

        return "rtsp://$userEnc:$passEnc@$hostAndPath"
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun releasePlayers() {
        players.forEach { it.release() }
        players.clear()
    }

    override fun onStart() {
        super.onStart()
        players.forEach { it.playWhenReady = true }
    }

    override fun onStop() {
        super.onStop()
        players.forEach { it.playWhenReady = false }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelar todos los loops de detección
        detectionHandler.removeCallbacksAndMessages(null)
        releasePlayers()
    }
}
