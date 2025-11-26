package com.example.registro

import android.content.Context
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

    override fun attachBaseContext(newBase: Context) {
        // Bloque que traduce las palabras de español a ingles
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    // ─────────────────────────────
    // MODELO PARA LA LISTA: día + cámara
    // ─────────────────────────────
    data class FechaCam(
        val fechaDia: String,
        val camLabel: String
    )

    private lateinit var rvFechas: RecyclerView
    private lateinit var adapter: FechasAdapter
    private val gruposFechaCam = mutableListOf<FechaCam>()

    // Formatos usando la zona horaria del TELÉFONO (por defecto del sistema)
    private val sdfDia = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)

    // 👉 Formato del NOMBRE DE ARCHIVO del NVR: 2025-11-25_21-14-48_cam1.mp4
    private val sdfFileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

    // Carpeta del NVR para este usuario (usamos su uid)
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

        adapter = FechasAdapter(gruposFechaCam) { grupo ->
            val intent = Intent(this, GrabacionesDetalleActivity::class.java)
            intent.putExtra("fecha", grupo.fechaDia)
            intent.putExtra("camLabel", grupo.camLabel)          // 👉 pasamos la cámara
            intent.putExtra("nvrId", nvrIdParaEsteUsuario)
            startActivity(intent)
        }
        rvFechas.adapter = adapter

        // nvrId = uid del usuario actual
        val user = FirebaseAuth.getInstance().currentUser
        nvrIdParaEsteUsuario = user?.uid
    }

    override fun onResume() {
        super.onResume()
        cargarClipsDesdeFirebaseYActualizarPantalla()
    }

    // ─────────────────────────────
    // Cargar clips de Storage → ClipRepo
    // ─────────────────────────────
    private fun cargarClipsDesdeFirebaseYActualizarPantalla() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver grabaciones", Toast.LENGTH_LONG)
                .show()
            return
        }

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

                var pendientes = result.items.size

                result.items.forEach { itemRef ->
                    itemRef.metadata
                        .addOnSuccessListener { meta ->
                            // Nombre de archivo, ej:
                            // 2025-11-25_21-14-48_cam1.mp4
                            val baseName = itemRef.name.substringBeforeLast(".")
                            val datePart = baseName.substringBeforeLast("_") // 2025-11-25_21-14-48

                            // Intentamos parsear la fecha desde el NOMBRE del archivo
                            val fechaDesdeNombre: Date? = try {
                                sdfFileName.parse(datePart)
                            } catch (e: Exception) {
                                null
                            }

                            val fecha: Date = fechaDesdeNombre ?: run {
                                // Si falla, usamos la fecha de Firebase como respaldo
                                val millis = meta.updatedTimeMillis
                                Date(millis)
                            }

                            val fechaDia = sdfDia.format(fecha)
                            val fechaFull = sdfFull.format(fecha)

                            // Deducir cámara desde el nombre del archivo
                            // 2025-11-25_21-14-48_sala.mp4 -> "SALA"
                            val camSlug = baseName.substringAfterLast("_")
                            val camLabel = camSlug.replace("_", " ").uppercase()

                            val clip = ClipItem(
                                storageRef = itemRef.name,
                                fechaDia = fechaDia,
                                fechaCompleta = fechaFull,
                                camLabel = camLabel
                            )

                            ClipRepo.agregarClip(clip)
                        }
                        .addOnCompleteListener {
                            pendientes -= 1
                            if (pendientes == 0) {
                                refrescarListaFechas()
                            }
                        }
                }
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

    // ─────────────────────────────
    // Construye la lista: un item por (fecha, cámara)
    // ─────────────────────────────
    private fun refrescarListaFechas() {
        val grupos = ClipRepo.clipsGuardados
            .groupBy { it.fechaDia }          // por día
            .flatMap { (fecha, clips) ->
                clips
                    .map { it.camLabel }
                    .distinct()
                    .map { camLabel -> FechaCam(fechaDia = fecha, camLabel = camLabel) }
            }
            // Orden: fecha descendente (por texto dd/MM/yyyy) y luego nombre de cámara
            .sortedWith(
                compareByDescending<FechaCam> { it.fechaDia }
                    .thenBy { it.camLabel }
            )

        gruposFechaCam.clear()
        gruposFechaCam.addAll(grupos)
        adapter.notifyDataSetChanged()
    }

    // ─────────────────────────────
    // ADAPTER
    // ─────────────────────────────
    class FechasAdapter(
        private val grupos: List<FechaCam>,
        private val onClickGrupo: (FechaCam) -> Unit
    ) : RecyclerView.Adapter<FechasAdapter.FechaViewHolder>() {

        class FechaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
            val tvCam: TextView = itemView.findViewById(R.id.tvCams)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FechaViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_fecha, parent, false)
            return FechaViewHolder(v)
        }

        override fun onBindViewHolder(holder: FechaViewHolder, position: Int) {
            val grupo = grupos[position]
            holder.tvFecha.text = grupo.fechaDia
            holder.tvCam.text = grupo.camLabel
            holder.itemView.setOnClickListener { onClickGrupo(grupo) }
        }

        override fun getItemCount(): Int = grupos.size
    }
}
