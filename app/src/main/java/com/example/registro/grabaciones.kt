package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class grabaciones : AppCompatActivity() {

    private lateinit var rvFechas: RecyclerView
    private lateinit var adapter: FechasAdapter
    private val fechasUnicas = mutableListOf<String>()

    // Resumen por fecha: "SALA · PORTON"
    private val resumenPorFecha = mutableMapOf<String, String>()

    // Formatos de fecha/hora
    private val sdfEntrada = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
    private val sdfDia = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)

    // Carpeta del NVR para este usuario (uid)
    private var nvrIdParaEsteUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grabaciones)

        // Botón "< Inicio"
        findViewById<TextView>(R.id.tvBackInicioGrab).setOnClickListener {
            startActivity(Intent(this, Inicio::class.java))
            finish()
        }

        rvFechas = findViewById(R.id.rvFechas)
        rvFechas.layoutManager = LinearLayoutManager(this)

        adapter = FechasAdapter(
            fechas = fechasUnicas,
            resumenPorFecha = resumenPorFecha
        ) { fecha ->
            val intent = Intent(this, GrabacionesDetalleActivity::class.java)
            intent.putExtra("fecha", fecha)
            intent.putExtra("nvrId", nvrIdParaEsteUsuario)
            startActivity(intent)
        }
        rvFechas.adapter = adapter

        // nvrId = uid del usuario logueado
        val user = FirebaseAuth.getInstance().currentUser
        nvrIdParaEsteUsuario = user?.uid
    }

    override fun onResume() {
        super.onResume()
        cargarClipsDesdeFirebaseYActualizarPantalla()
    }

    private fun cargarClipsDesdeFirebaseYActualizarPantalla() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver grabaciones", Toast.LENGTH_LONG)
                .show()
            return
        }

        // Si por alguna razón nvrId es null, usamos el uid
        val nvrId = nvrIdParaEsteUsuario ?: user.uid
        nvrIdParaEsteUsuario = nvrId

        val storageRef = Firebase.storage.reference
            .child("recordings")
            .child(nvrId)

        storageRef.listAll()
            .addOnSuccessListener { result: ListResult ->
                ClipRepo.clipsGuardados.clear()

                if (result.items.isEmpty()) {
                    refrescarListaFechas()
                    return@addOnSuccessListener
                }

                // Ya no pedimos metadata. Leemos TODO del nombre del archivo
                // Formato esperado: 2025-11-22_00-10-30_sala.mp4
                for (itemRef in result.items) {
                    val fileName = itemRef.name
                    val baseName = fileName.substringBeforeLast(".")      // 2025-11-22_00-10-30_sala
                    val fechaHoraPart = baseName.substringBeforeLast("_") // 2025-11-22_00-10-30
                    val camSlug = baseName.substringAfterLast("_")        // sala (o sala_porton, etc.)

                    val fechaGrabacion: Date? = try {
                        sdfEntrada.parse(fechaHoraPart)
                    } catch (e: Exception) {
                        null
                    }

                    if (fechaGrabacion == null) {
                        // Si el nombre no respeta el formato, saltamos este archivo
                        continue
                    }

                    val fechaDia = sdfDia.format(fechaGrabacion)          // 22/11/2025
                    val fechaFull = sdfFull.format(fechaGrabacion)        // 22/11/2025 00:10:30

                    val camLabel = camSlug.replace("_", " ").uppercase()  // SALA, PORTON, etc.

                    val clip = ClipItem(
                        storageRef = fileName,
                        fechaDia = fechaDia,
                        fechaCompleta = fechaFull,
                        camLabel = camLabel
                    )

                    ClipRepo.agregarClip(clip)
                }

                // Una vez procesados todos
                refrescarListaFechas()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al leer Storage: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                refrescarListaFechas()
            }
    }

    private fun refrescarListaFechas() {
        val listaFechas = ClipRepo.fechasUnicas()

        fechasUnicas.clear()
        resumenPorFecha.clear()

        for (fecha in listaFechas) {
            fechasUnicas.add(fecha)

            val cams = ClipRepo.camsPorFecha(fecha)
            val resumen = if (cams.isEmpty()) "" else cams.joinToString(" · ")
            resumenPorFecha[fecha] = resumen
        }

        adapter.notifyDataSetChanged()
    }

    class FechasAdapter(
        private val fechas: List<String>,
        private val resumenPorFecha: Map<String, String>,
        private val onClickFecha: (String) -> Unit
    ) : RecyclerView.Adapter<FechasAdapter.FechaViewHolder>() {

        class FechaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
            val tvCamsResumen: TextView = itemView.findViewById(R.id.tvCamsResumen)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FechaViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_fecha, parent, false)
            return FechaViewHolder(v)
        }

        override fun onBindViewHolder(holder: FechaViewHolder, position: Int) {
            val fecha = fechas[position]
            holder.tvFecha.text = fecha
            holder.tvCamsResumen.text = resumenPorFecha[fecha] ?: ""
            holder.itemView.setOnClickListener { onClickFecha(fecha) }
        }

        override fun getItemCount(): Int = fechas.size
    }
}
