package com.example.registro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GrabacionesDetalleActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    private lateinit var fechaSeleccionada: String
    private lateinit var rv: RecyclerView
    private lateinit var adapter: ClipsAdapter
    private val clipsDeEseDia = mutableListOf<ClipItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grabaciones_detalle)

        fechaSeleccionada = intent.getStringExtra("fecha") ?: "N/A"

        findViewById<TextView>(R.id.tvFechaDetalle).text = fechaSeleccionada

        rv = findViewById(R.id.rvCams)
        rv.layoutManager = GridLayoutManager(this, 2)

        adapter = ClipsAdapter(clipsDeEseDia) { clip ->
            val i = Intent(this, PlayerActivity::class.java)
            i.putExtra("storageRef", clip.storageRef)
            i.putExtra("fecha", clip.fechaCompleta)
            i.putExtra("cam", clip.cam)
            startActivity(i)
        }

        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarClipsDeFecha(fechaSeleccionada)
    }

    private fun cargarClipsDeFecha(fechaDia: String) {
        clipsDeEseDia.clear()
        clipsDeEseDia.addAll(ClipRepo.clipsPorFecha(fechaDia))
        adapter.notifyDataSetChanged()
    }

    // --------- Adapter grid 2 columnas ----------
    private class ClipsAdapter(
        private val items: List<ClipItem>,
        private val onClick: (ClipItem) -> Unit
    ) : RecyclerView.Adapter<ClipsAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvCam: TextView = v.findViewById(R.id.tvCam)
            val tvInfo: TextView = v.findViewById(R.id.tvInfo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cam, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = items[pos]
            h.tvCam.text = "CAM ${item.cam}"
            h.tvInfo.text = "${item.fechaCompleta} · ${item.storageRef}"
            h.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
