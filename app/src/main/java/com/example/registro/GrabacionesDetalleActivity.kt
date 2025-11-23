package com.example.registro

import android.content.Context
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

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    private lateinit var fechaSeleccionada: String
    private var nvrId: String? = null

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ClipsAdapter
    private val clipsDeEseDia = mutableListOf<ClipItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grabaciones_detalle)

        fechaSeleccionada = intent.getStringExtra("fecha") ?: "N/A"
        nvrId = intent.getStringExtra("nvrId")

        if (nvrId.isNullOrEmpty()) {
            val user = FirebaseAuth.getInstance().currentUser
            nvrId = user?.uid
        }

        findViewById<TextView>(R.id.tvBackDetalleGrab).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.tvFechaDetalle).text = fechaSeleccionada

        findViewById<Button>(R.id.btnPlayAllDay).setOnClickListener {
            reproducirTodoElDia()
        }

        rv = findViewById(R.id.rvCams)
        rv.layoutManager = GridLayoutManager(this, 2)

        adapter = ClipsAdapter(clipsDeEseDia) { clip ->
            abrirClipIndividual(clip)
        }

        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarClipsDeFecha(fechaSeleccionada)
    }

    private fun cargarClipsDeFecha(fecha: String) {
        clipsDeEseDia.clear()

        val lista = ClipRepo
            .clipsPorFecha(fecha)
            .sortedBy { it.fechaCompleta }

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
        i.putExtra("label", "$fechaSeleccionada (todo el día)")
        i.putExtra("nvrId", nvrId)
        startActivity(i)
    }
}
