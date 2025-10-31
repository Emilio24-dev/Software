package com.example.registro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.registro.db.ClipDao
import com.example.registro.db.ClipEntity
import com.example.registro.db.DbProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ScreenRecordService : Service() {

    companion object {
        // LiveActivity lee esto para pintar el botón REC verde/rojo
        var isRecording: Boolean = false

        private const val CHANNEL_ID = "sentri_record_channel"
        private const val NOTIF_ID = 777
    }

    // ---------- MediaProjection / Recorder stuff ----------
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null

    // Ruta completa del archivo que estamos grabando ahora
    private var outputFilePath: String? = null

    // Qué cam estaba seleccionada cuando se presionó REC al iniciar
    private var camSeleccionadaAtStart: Int = 1

    // ---------- Room / DAO ----------
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // 🔥 Usa la misma base de datos global de la app
    private val clipDao: ClipDao by lazy {
        DbProvider.dao(applicationContext)
    }
    

    // Este servicio no es bound
    override fun onBind(intent: Intent?): IBinder? = null

    // Se llama cada vez que le hacemos startService(...) o startForegroundService(...)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val action = intent?.getStringExtra("action")

        when (action) {
            "START" -> {
                val resultCode = intent.getIntExtra("resultCode", 0)
                val dataIntent = intent.getParcelableExtra<Intent>("dataIntent")

                // cuál cam estaba activa al inicio
                camSeleccionadaAtStart = intent.getIntExtra("cam", 1)

                if (!isRecording && dataIntent != null) {
                    startForegroundNotification()
                    startRecordingSafe(resultCode, dataIntent)
                }
            }

            "STOP" -> {
                stopRecordingAndSaveClip()
                stopForeground(true)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    // ---------- Notificación foreground obligatoria ----------
    private fun startForegroundNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Grabación Sentri",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(ch)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, LiveActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Grabando pantalla")
            .setContentText("Sentri está guardando la transmisión")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notif)
    }

    // ---------- Arrancar grabación con try/catch ----------
    private fun startRecordingSafe(resultCode: Int, dataIntent: Intent) {
        try {
            startRecording(resultCode, dataIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Si algo falla al arrancar, limpiamos para que no quede zombie
            forceStopInternal()
        }
    }

    private fun startRecording(resultCode: Int, dataIntent: Intent) {
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, dataIntent)

        // Creamos carpeta Movies/Sentri en almacenamiento público
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val sentriDir = File(moviesDir, "Sentri")
        if (!sentriDir.exists()) {
            sentriDir.mkdirs()
        }

        // archivo final .mp4
        val outFile = File(sentriDir, "VID_$timeStamp.mp4")
        outputFilePath = outFile.absolutePath

        // Resolución fija estable (puedes ajustar)
        val targetW = 1280
        val targetH = 720
        val dpi = resources.displayMetrics.densityDpi

        // Configurar MediaRecorder
        mediaRecorder = MediaRecorder().apply {
            // audio del micrófono
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // video: surface que nos va a dar MediaProjection
            setVideoSource(MediaRecorder.VideoSource.SURFACE)

            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFilePath)

            // audio AAC
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44100)

            // video H264
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(5_000_000)
            setVideoFrameRate(30)
            setVideoSize(targetW, targetH)

            prepare()
        }

        val recorderSurface = mediaRecorder!!.surface

        // VirtualDisplay = espejo de pantalla hacia recorderSurface
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "SentriCapture",
            targetW,
            targetH,
            dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            recorderSurface,
            null,
            null
        )

        // ¡a grabar!
        mediaRecorder?.start()
        isRecording = true
    }

    // ---------- Parar grabación + guardar clip en Room ----------
    private fun stopRecordingAndSaveClip() {
        if (!isRecording) return

        // 1. detener grabación física y limpiar recursos
        val savedPath = outputFilePath // respaldo antes de que lo nullemos
        forceStopInternal()

        // 2. si realmente se generó archivo
        if (!savedPath.isNullOrBlank()) {
            val nowMs = System.currentTimeMillis()

            // IA fake / fachada
            val aiTagFake = "Movimiento"
            val aiSummaryFake = if (camSeleccionadaAtStart == 1) {
                "Persona detectada frente a CAM 1"
            } else {
                "Actividad detectada frente a CAM 2"
            }

            // importante: ExoPlayer puede reproducir "file://"
            val fileUriStr = "file://$savedPath"

            val clipEntity = ClipEntity(
                timestampMs = nowMs,
                cam = camSeleccionadaAtStart,
                fileUri = fileUriStr,
                aiTag = aiTagFake,
                aiSummary = aiSummaryFake
            )

            // Insert en Room en background (IO)
            serviceScope.launch {
                try {
                    clipDao.insertClip(clipEntity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Libera MediaRecorder, VirtualDisplay, MediaProjection.
     * También marca isRecording = false.
     */
    private fun forceStopInternal() {
        try {
            mediaRecorder?.apply {
                try { stop() } catch (e: Exception) { e.printStackTrace() }
                try { reset() } catch (e: Exception) { e.printStackTrace() }
                try { release() } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try { virtualDisplay?.release() } catch (e: Exception) { e.printStackTrace() }
        virtualDisplay = null

        try { mediaProjection?.stop() } catch (e: Exception) { e.printStackTrace() }
        mediaProjection = null

        mediaRecorder = null
        isRecording = false
    }

    override fun onDestroy() {
        super.onDestroy()
        // Si matan el service sin STOP explícito,
        // igual liberamos recursos
        forceStopInternal()
    }
}



