package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class GrabacionesDetalleActivity : AppCompatActivity() {

    private lateinit var fechaSeleccionada: String
    private lateinit var camLabelSeleccionada: String
    private var nvrId: String? = null

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ClipsAdapter
    private val clipsDeEseDia = mutableListOf<ClipItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grabaciones_detalle)

        // Datos recibidos
        fechaSeleccionada = intent.getStringExtra("fecha") ?: "N/A"
        camLabelSeleccionada = intent.getStringExtra("camLabel") ?: ""
        nvrId = intent.getStringExtra("nvrId")

        // Si nvrId viene nulo, tomamos el uid del usuario
        if (nvrId.isNullOrEmpty()) {
            val user = FirebaseAuth.getInstance().currentUser
            nvrId = user?.uid
        }

        // Botón "<"
        findViewById<TextView>(R.id.tvBackDetalleGrab).setOnClickListener {
            finish()
        }

        // Títulos
        findViewById<TextView>(R.id.tvFechaDetalle).text = fechaSeleccionada
        val tvCamDetalle = findViewById<TextView?>(R.id.tvCamDetalle) // opcional
        tvCamDetalle?.text = camLabelSeleccionada

        // Botón reproducir TODO el día (solo esa cámara)
        findViewById<Button>(R.id.btnPlayAllDay).setOnClickListener {
            reproducirTodoElDia()
        }

        // RecyclerView
        rv = findViewById(R.id.rvCams)
        rv.layoutManager = GridLayoutManager(this, 2)

        adapter = ClipsAdapter(
            clips = clipsDeEseDia,
            onClick = { clip ->
                // Tap normal → solo ese clip
                abrirClipIndividual(clip)
            },
            onPlayFromHere = { clipInicio ->
                // Long-press → reproducir desde aquí hasta el final del día
                reproducirDesdeClip(clipInicio)
            }
        )

        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarClipsDeFechaYCam(fechaSeleccionada, camLabelSeleccionada)
    }

    // Solo clips del día + cámara seleccionada
    private fun cargarClipsDeFechaYCam(fecha: String, camLabel: String) {
        clipsDeEseDia.clear()

        val lista = ClipRepo
            .clipsPorFecha(fecha)               // filtra por fecha
            .filter { it.camLabel == camLabel } // y por cámara
            .sortedBy { it.fechaCompleta }      // aseguramos orden cronológico

        clipsDeEseDia.addAll(lista)
        adapter.notifyDataSetChanged()
    }

    private fun abrirClipIndividual(clip: ClipItem) {
        val i = Intent(this, PlayerActivity::class.java)
        i.putStringArrayListExtra("playlist", arrayListOf(clip.storageRef))
        i.putExtra("label", clip.fechaCompleta)
        i.putExtra("nvrId", nvrId)
        startActivity(i)
    }

    /** Reproduce todos los clips de ese día (solo esa cámara) desde el PRINCIPIO */
    private fun reproducirTodoElDia() {
        if (clipsDeEseDia.isEmpty()) {
            Toast.makeText(this, "No hay clips para este día", Toast.LENGTH_LONG).show()
            return
        }

        val playlist = clipsDeEseDia
            .sortedBy { it.fechaCompleta }
            .map { it.storageRef }

        val i = Intent(this, PlayerActivity::class.java)
        i.putStringArrayListExtra("playlist", ArrayList(playlist))
        i.putExtra("label", "$fechaSeleccionada - $camLabelSeleccionada (todo el día)")
        i.putExtra("nvrId", nvrId)
        startActivity(i)
    }

    /** Reproduce desde el clip pulsado (incluido) hasta el final del día */
    private fun reproducirDesdeClip(clipInicio: ClipItem) {
        if (clipsDeEseDia.isEmpty()) return

        val indice = clipsDeEseDia.indexOf(clipInicio)
        if (indice == -1) return

        val playlist = clipsDeEseDia
            .subList(indice, clipsDeEseDia.size)   // desde ese índice hasta el final
            .map { it.storageRef }

        val i = Intent(this, PlayerActivity::class.java)
        i.putStringArrayListExtra("playlist", ArrayList(playlist))
        i.putExtra(
            "label",
            "$fechaSeleccionada - $camLabelSeleccionada (desde ${clipInicio.fechaCompleta})"
        )
        i.putExtra("nvrId", nvrId)

        Toast.makeText(
            this,
            "Reproduciendo desde ${clipInicio.fechaCompleta}",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(i)
    }
}
