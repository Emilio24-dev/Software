package com.example.registro

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class LiveActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    private var isRecording = false
    private var camSeleccionada = 1

    private lateinit var cardCam1: LinearLayout
    private lateinit var cardCam2: LinearLayout

    private lateinit var tvClockCam1: TextView
    private lateinit var videoCam1: PlayerView
    private lateinit var btnBack1: TextView
    private lateinit var btnReload1: TextView
    private lateinit var btnPlayPause1: TextView
    private lateinit var btnMute1: TextView
    private lateinit var btnReloadRight1: TextView
    private lateinit var btnForward1: TextView

    private lateinit var tvClockCam2: TextView
    private lateinit var videoCam2: PlayerView
    private lateinit var btnBack2: TextView
    private lateinit var btnReload2: TextView
    private lateinit var btnPlayPause2: TextView
    private lateinit var btnMute2: TextView
    private lateinit var btnReloadRight2: TextView
    private lateinit var btnForward2: TextView

    private lateinit var camFrame1: FrameLayout
    private lateinit var camFrame2: FrameLayout

    private lateinit var overlayIA1: FrameLayout
    private lateinit var overlayIA2: FrameLayout

    // === CAM1 MORNING ===
    private lateinit var iaCam1_c0309_perro: TextView
    private lateinit var iaCam1_c0311_carLeft: TextView
    private lateinit var iaCam1_c0311_carRight: TextView
    private lateinit var iaCam1_c0311_perrosLate: TextView

    // === CAM1 NOCHE ===
    private lateinit var iaCam1_c0337_carA: TextView
    private lateinit var iaCam1_c0337_carB: TextView
    private lateinit var iaCam1_c0337_carC: TextView
    private lateinit var iaCam1_c0337_carD: TextView
    private lateinit var iaCam1_c0321_vehicleCenter: TextView

    // === CAM2 MORNING ===
    private lateinit var iaCam2_c0318_sombrilla: TextView
    private lateinit var iaCam2_c0318_pajaro: TextView
    private lateinit var iaCam2_c0318_arbol: TextView

    // === CAM2 NOCHE ===
    private lateinit var iaCam2_c0333_ninaSilla: TextView
    private lateinit var iaCam2_c0325_alertaUnica: TextView

    private lateinit var btnRec: TextView
    private lateinit var btnBackHome: TextView

    private var player1: ExoPlayer? = null
    private var player2: ExoPlayer? = null

    private var lastFileCam1: String? = null
    private var lastFileCam2: String? = null

    private val uiHandler = Handler(Looper.getMainLooper())
    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClocks()
            uiHandler.postDelayed(this, 1000L)
        }
    }

    private val storage = Firebase.storage.reference.child("live")

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it == true }
        if (!granted) {
            Toast.makeText(this, "Permisos denegados :(", Toast.LENGTH_LONG).show()
        }
    }

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode != RESULT_OK || res.data == null) {
            Toast.makeText(this, "No se autorizó grabar pantalla", Toast.LENGTH_LONG).show()
            return@registerForActivityResult
        }

        val startIntent = Intent(this, ScreenRecordService::class.java).apply {
            putExtra("action", "START")
            putExtra("resultCode", res.resultCode)
            putExtra("dataIntent", res.data)
            putExtra("cam", camSeleccionada)
        }
        ContextCompat.startForegroundService(this, startIntent)

        isRecording = true
        applyRecordingUIState()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)

        cardCam1 = findViewById(R.id.cardCam1)
        cardCam2 = findViewById(R.id.cardCam2)

        tvClockCam1 = findViewById(R.id.tvClockCam1)
        videoCam1 = findViewById(R.id.videoCam1)
        btnBack1 = findViewById(R.id.btnBack1)
        btnReload1 = findViewById(R.id.btnReload1)
        btnPlayPause1 = findViewById(R.id.btnPlayPause1)
        btnMute1 = findViewById(R.id.btnMute1)
        btnReloadRight1 = findViewById(R.id.btnReloadRight1)
        btnForward1 = findViewById(R.id.btnForward1)

        tvClockCam2 = findViewById(R.id.tvClockCam2)
        videoCam2 = findViewById(R.id.videoCam2)
        btnBack2 = findViewById(R.id.btnBack2)
        btnReload2 = findViewById(R.id.btnReload2)
        btnPlayPause2 = findViewById(R.id.btnPlayPause2)
        btnMute2 = findViewById(R.id.btnMute2)
        btnReloadRight2 = findViewById(R.id.btnReloadRight2)
        btnForward2 = findViewById(R.id.btnForward2)

        camFrame1 = findViewById(R.id.camFrame1)
        camFrame2 = findViewById(R.id.camFrame2)
        overlayIA1 = findViewById(R.id.overlayIA1)
        overlayIA2 = findViewById(R.id.overlayIA2)

        // bind CAM1 MORNING
        iaCam1_c0309_perro = findViewById(R.id.iaCam1_c0309_perro)
        iaCam1_c0311_carLeft = findViewById(R.id.iaCam1_c0311_carLeft)
        iaCam1_c0311_carRight = findViewById(R.id.iaCam1_c0311_carRight)
        iaCam1_c0311_perrosLate = findViewById(R.id.iaCam1_c0311_perrosLate)

        // bind CAM1 NOCHE
        iaCam1_c0337_carA = findViewById(R.id.iaCam1_c0337_carA)
        iaCam1_c0337_carB = findViewById(R.id.iaCam1_c0337_carB)
        iaCam1_c0337_carC = findViewById(R.id.iaCam1_c0337_carC)
        iaCam1_c0337_carD = findViewById(R.id.iaCam1_c0337_carD)
        iaCam1_c0321_vehicleCenter = findViewById(R.id.iaCam1_c0321_vehicleCenter)

        // bind CAM2 MORNING
        iaCam2_c0318_sombrilla = findViewById(R.id.iaCam2_c0318_sombrilla)
        iaCam2_c0318_pajaro = findViewById(R.id.iaCam2_c0318_pajaro)
        iaCam2_c0318_arbol = findViewById(R.id.iaCam2_c0318_arbol)

        // bind CAM2 NOCHE
        iaCam2_c0333_ninaSilla = findViewById(R.id.iaCam2_c0333_ninaSilla)
        iaCam2_c0325_alertaUnica = findViewById(R.id.iaCam2_c0325_alertaUnica)

        btnRec = findViewById(R.id.btnRec)
        btnBackHome = findViewById(R.id.btnBackHome)

        player1 = ExoPlayer.Builder(this).build()
        videoCam1.player = player1

        player2 = ExoPlayer.Builder(this).build()
        videoCam2.player = player2

        playCurrentClipForCam1()
        playCurrentClipForCam2()

        uiHandler.post(clockRunnable)
        requestRecordPermissions()

        fun updateSelectedCamUI() {
            cardCam1.isSelected = camSeleccionada == 1
            cardCam2.isSelected = camSeleccionada == 2
        }
        updateSelectedCamUI()

        cardCam1.setOnClickListener {
            camSeleccionada = 1
            updateSelectedCamUI()
        }
        cardCam2.setOnClickListener {
            camSeleccionada = 2
            updateSelectedCamUI()
        }

        videoCam1.setOnClickListener { openFullScreen(1) }
        videoCam2.setOnClickListener { openFullScreen(2) }

        btnBackHome.setOnClickListener { finish() }

        btnRec.setOnClickListener {
            if (!isRecording) {
                // ENTRAR EN MODO GRABANDO
                isRecording = true
                applyRecordingUIState()
                Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show()

            } else {
                // SALIR DE MODO GRABANDO
                isRecording = false
                applyRecordingUIState()

                // parar servicio "grabación"
                val stopIntent = Intent(this, ScreenRecordService::class.java).apply {
                    putExtra("action", "STOP")
                }
                ContextCompat.startForegroundService(this, stopIntent)

                // Guardar los clips predefinidos en memoria (ClipRepo) bajo la fecha de HOY
                registrarClipEnHistorial()

                // Feedback al usuario
                Toast.makeText(
                    this,
                    "Evento almacenado correctamente",
                    Toast.LENGTH_LONG
                ).show()

                // Abrir la pantalla de grabaciones para ver la fecha de hoy con los clips
                startActivity(Intent(this, grabaciones::class.java))
            }
        }

        btnRec.setOnLongClickListener {
            startActivity(Intent(this, grabaciones::class.java))
            true
        }

        btnPlayPause1.setOnClickListener {
            val p = player1 ?: return@setOnClickListener
            if (p.isPlaying) {
                p.pause()
                btnPlayPause1.text = "▶"
            } else {
                p.play()
                btnPlayPause1.text = "⏸"
            }
        }

        btnMute1.setOnClickListener {
            val p = player1 ?: return@setOnClickListener
            if (p.volume > 0f) {
                p.volume = 0f
                btnMute1.text = "🔇"
            } else {
                p.volume = 1f
                btnMute1.text = "🔊"
            }
        }

        btnBack1.setOnClickListener {
            val p = player1 ?: return@setOnClickListener
            val newPos = (p.currentPosition - 5000L).coerceAtLeast(0L)
            p.seekTo(newPos)
        }

        btnForward1.setOnClickListener {
            val p = player1 ?: return@setOnClickListener
            val newPos = p.currentPosition + 5000L
            p.seekTo(newPos)
        }

        btnReload1.setOnClickListener { playCurrentClipForCam1(forceReload = true) }
        btnReloadRight1.setOnClickListener { playCurrentClipForCam1(forceReload = true) }

        btnPlayPause2.setOnClickListener {
            val p = player2 ?: return@setOnClickListener
            if (p.isPlaying) {
                p.pause()
                btnPlayPause2.text = "▶"
            } else {
                p.play()
                btnPlayPause2.text = "⏸"
            }
        }

        btnMute2.setOnClickListener {
            val p = player2 ?: return@setOnClickListener
            if (p.volume > 0f) {
                p.volume = 0f
                btnMute2.text = "🔇"
            } else {
                p.volume = 1f
                btnMute2.text = "🔊"
            }
        }

        btnBack2.setOnClickListener {
            val p = player2 ?: return@setOnClickListener
            val newPos = (p.currentPosition - 5000L).coerceAtLeast(0L)
            p.seekTo(newPos)
        }

        btnForward2.setOnClickListener {
            val p = player2 ?: return@setOnClickListener
            val newPos = p.currentPosition + 5000L
            p.seekTo(newPos)
        }

        btnReload2.setOnClickListener { playCurrentClipForCam2(forceReload = true) }
        btnReloadRight2.setOnClickListener { playCurrentClipForCam2(forceReload = true) }

        applyRecordingUIState()
    }

    override fun onResume() {
        super.onResume()
        isRecording = ScreenRecordService.isRecording || isRecording
        applyRecordingUIState()
    }

    override fun onDestroy() {
        super.onDestroy()
        uiHandler.removeCallbacks(clockRunnable)
        player1?.release()
        player1 = null
        player2?.release()
        player2 = null
    }

    // ======================
    // IA helpers según tu guion
    // ======================

    private fun hideAllIABoxes() {
        // CAM1 morning
        iaCam1_c0309_perro.visibility = View.GONE
        iaCam1_c0311_carLeft.visibility = View.GONE
        iaCam1_c0311_carRight.visibility = View.GONE
        iaCam1_c0311_perrosLate.visibility = View.GONE

        // CAM1 noche
        iaCam1_c0337_carA.visibility = View.GONE
        iaCam1_c0337_carB.visibility = View.GONE
        iaCam1_c0337_carC.visibility = View.GONE
        iaCam1_c0337_carD.visibility = View.GONE
        iaCam1_c0321_vehicleCenter.visibility = View.GONE

        // CAM2 morning
        iaCam2_c0318_sombrilla.visibility = View.GONE
        iaCam2_c0318_pajaro.visibility = View.GONE
        iaCam2_c0318_arbol.visibility = View.GONE

        // CAM2 noche
        iaCam2_c0333_ninaSilla.visibility = View.GONE
        iaCam2_c0325_alertaUnica.visibility = View.GONE
    }

    // CAM1 - MORNING
    private fun escenaCam1_C0309_perro() {
        hideAllIABoxes()
        overlayIA1.visibility = View.VISIBLE
        iaCam1_c0309_perro.visibility = View.VISIBLE
    }

    private fun escenaCam1_C0311_vehiculos() {
        hideAllIABoxes()
        overlayIA1.visibility = View.VISIBLE
        iaCam1_c0311_carLeft.visibility = View.VISIBLE
        iaCam1_c0311_carRight.visibility = View.VISIBLE
    }

    private fun escenaCam1_C0311_perrosLate() {
        hideAllIABoxes()
        overlayIA1.visibility = View.VISIBLE
        iaCam1_c0311_perrosLate.visibility = View.VISIBLE
    }

    // CAM1 - NOCHE
    private fun escenaCam1_C0337_4vehiculos() {
        hideAllIABoxes()
        overlayIA1.visibility = View.VISIBLE
        iaCam1_c0337_carA.visibility = View.VISIBLE
        iaCam1_c0337_carB.visibility = View.VISIBLE
        iaCam1_c0337_carC.visibility = View.VISIBLE
        iaCam1_c0337_carD.visibility = View.VISIBLE
    }

    private fun escenaCam1_C0321_vehicleCenter() {
        hideAllIABoxes()
        overlayIA1.visibility = View.VISIBLE
        iaCam1_c0321_vehicleCenter.visibility = View.VISIBLE
    }

    // CAM2 - MORNING
    private fun escenaCam2_C0318_sombrillaPajaroArbol() {
        hideAllIABoxes()
        overlayIA2.visibility = View.VISIBLE
        iaCam2_c0318_sombrilla.visibility = View.VISIBLE
        iaCam2_c0318_pajaro.visibility = View.VISIBLE
        iaCam2_c0318_arbol.visibility = View.VISIBLE
    }

    // CAM2 - NOCHE
    private fun escenaCam2_C0333_ninaSilla() {
        hideAllIABoxes()
        overlayIA2.visibility = View.VISIBLE
        iaCam2_c0333_ninaSilla.visibility = View.VISIBLE
    }

    private fun escenaCam2_C0325_alertaUnica() {
        hideAllIABoxes()
        overlayIA2.visibility = View.VISIBLE
        iaCam2_c0325_alertaUnica.visibility = View.VISIBLE
    }

    // ======================
    // UI de grabación fachada
    // ======================
    private fun applyRecordingUIState() {
        if (isRecording) {
            camFrame1.setBackgroundResource(R.drawable.borde_rojo)
            camFrame2.setBackgroundResource(R.drawable.borde_rojo)

            // overlays encendidos (IA activa visualmente)
            overlayIA1.visibility = View.VISIBLE
            overlayIA2.visibility = View.VISIBLE

            btnRec.text = "STOP"
            btnRec.setBackgroundColor(0xFF00AA00.toInt())
            btnRec.setTextColor(0xFFFFFFFF.toInt())
        } else {
            camFrame1.setBackgroundResource(R.drawable.borde_blanco)
            camFrame2.setBackgroundResource(R.drawable.borde_blanco)

            hideAllIABoxes()
            overlayIA1.visibility = View.GONE
            overlayIA2.visibility = View.GONE

            btnRec.text = "REC"
            btnRec.setBackgroundColor(0xFF8A1E1E.toInt())
            btnRec.setTextColor(0xFFFFFFFF.toInt())
        }
    }

    // ======================
    // RELOJ
    // ======================
    private fun updateClocks() {
        val now = getNowForElSalvador()
        tvClockCam1.text = now
        tvClockCam2.text = now
    }

    private fun getNowForElSalvador(): String {
        val cal = Calendar.getInstance()
        val tz = TimeZone.getTimeZone("America/El_Salvador")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = tz
        return sdf.format(cal.time)
    }

    // ======================
    // CLIPS CAM1
    // ======================
    private fun playCurrentClipForCam1(forceReload: Boolean = false) {
        val fileName = pickFileNameCam1()
        lastFileCam1 = fileName
        fetchUrlAndPlay(storage.child(fileName), player1, "Cam1", forceReload)
    }

    private fun pickFileNameCam1(): String {
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

        val morning = between(6,0,10,40)      // 06:00-10:40 -> C0309 / C0311
        val tarde   = between(10,40,21,0)     // 10:40-21:00 -> C0315 / C0314
        val nocheA  = between(21,0,23,50)     // 21:00-23:50 -> C0337
        val nocheB1 = between(23,51,23,59)    // 23:51-23:59 -> C0321
        val nocheB2 = between(0,0,6,59)       // 00:00-06:59 -> C0321

        return when {
            morning -> if (m % 2 == 0) "C0309.MP4" else "C0311.MP4"
            tarde   -> if (m % 2 == 0) "C0315.MP4" else "C0314.MP4"
            nocheA  -> "C0337.MP4"
            (nocheB1 || nocheB2) -> "C0321.MP4"
            else -> "C0337.MP4"
        }
    }

    // ======================
    // CLIPS CAM2
    // ======================
    private fun playCurrentClipForCam2(forceReload: Boolean = false) {
        val fileName = pickFileNameCam2()
        lastFileCam2 = fileName
        fetchUrlAndPlay(storage.child(fileName), player2, "Cam2", forceReload)
    }

    private fun pickFileNameCam2(): String {
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

        val morning = between(6,0,10,39)      // 06:00-10:39 -> C0318
        val tarde   = between(10,40,18,9)     // 10:40-18:09 -> C0319
        val noche1  = between(18,10,20,50)    // 18:10-20:50 -> C0333
        val noche2a = between(20,51,23,59)    // 20:51-23:59 -> C0325
        val noche2b = between(0,0,5,59)       // 00:00-05:59 -> C0325

        return when {
            morning -> "C0318.MP4"
            tarde   -> "C0319.MP4"
            noche1  -> "C0333.MP4"
            (noche2a || noche2b) -> "C0325.MP4"
            else -> "C0319.MP4"
        }
    }

    // ======================
    // Player utils
    // ======================
    private fun fetchUrlAndPlay(
        ref: com.google.firebase.storage.StorageReference,
        player: ExoPlayer?,
        camTag: String,
        forceReload: Boolean
    ) {
        ref.downloadUrl
            .addOnSuccessListener { uri ->
                val item = MediaItem.fromUri(uri)
                player?.setMediaItem(item)
                player?.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                player?.prepare()
                player?.playWhenReady = true

                if (player === player1) {
                    btnPlayPause1.text = if (player1?.isPlaying == true) "⏸" else "▶"
                } else if (player === player2) {
                    btnPlayPause2.text = if (player2?.isPlaying == true) "⏸" else "▶"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "$camTag error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun requestRecordPermissions() {
        val perms = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 33) {
            perms += Manifest.permission.READ_MEDIA_VIDEO
        } else {
            perms += Manifest.permission.READ_EXTERNAL_STORAGE
        }

        perms += Manifest.permission.RECORD_AUDIO

        if (Build.VERSION.SDK_INT < 29) {
            perms += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        val toAsk = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toAsk.isNotEmpty()) {
            permissionLauncher.launch(toAsk.toTypedArray())
        }
    }

    private fun requestScreenCapture() {
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    private fun openFullScreen(cam: Int) {
        val i = Intent(this, PlayerFullscreenActivity::class.java)
        val fileNameToSend = if (cam == 1) lastFileCam1 else lastFileCam2
        i.putExtra("cam", cam)
        i.putExtra("fileName", fileNameToSend)
        startActivity(i)
    }

    private fun registrarClipEnHistorial() {
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("America/El_Salvador")

        val sdfDia = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        sdfDia.timeZone = cal.timeZone
        val soloDia = sdfDia.format(cal.time)

        val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
        sdfFull.timeZone = cal.timeZone
        val marcaCompleta = sdfFull.format(cal.time)

        val listaArchivosEvento = listOf(
            "Animal.mp4",
            "Auto.mp4",
            "Perro.mp4",
            "...mp4"
        )

        for (nombre in listaArchivosEvento) {
            val clip = ClipItem(
                fechaDia = soloDia,
                fechaCompleta = marcaCompleta,
                cam = camSeleccionada,
                storageRef = nombre
            )
            ClipRepo.agregarClip(clip)
        }
    }
}
