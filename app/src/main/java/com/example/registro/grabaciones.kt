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

    // formatos de fecha que usamos siempre (día y día+hora)
    private val sdfDia = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)

    // string de la fecha de AHORITA, ej "28/10/2025"
    private val hoyString = sdfDia.format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grabaciones)

        rvFechas = findViewById(R.id.rvFechas)
        rvFechas.layoutManager = LinearLayoutManager(this)

        adapter = FechasAdapter(fechasUnicas) { fecha ->
            val intent = Intent(this, GrabacionesDetalleActivity::class.java)
            intent.putExtra("fecha", fecha)
            startActivity(intent)
        }
        rvFechas.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarClipsDesdeFirebaseYActualizarPantalla()
    }

    /**
     * 1. Descarga lista de archivos en /live de Firebase Storage.
     * 2. Mete cada archivo a ClipRepo con su fecha.
     * 3. Si para la fecha de hoy no vino nada, inyectamos 4 clips falsos predefinidos.
     * 4. Refrescamos las viñetas.
     */
    private fun cargarClipsDesdeFirebaseYActualizarPantalla() {
        val storageRef = Firebase.storage.reference.child("live")

        storageRef.listAll()
            .addOnSuccessListener { result: ListResult ->
                // limpiamos todo para no duplicar
                ClipRepo.clipsGuardados.clear()

                if (result.items.isEmpty()) {
                    // si literalmente no hay nada en Firebase todavía,
                    // creamos la fecha de hoy con clips fake
                    agregarClipsDeHoyPredeterminados()
                    refrescarListaFechas()
                    return@addOnSuccessListener
                }

                var pendientes = result.items.size
                var hoyTuvoClips = false

                result.items.forEach { itemRef ->
                    itemRef.metadata
                        .addOnSuccessListener { meta ->
                            val millis = meta.updatedTimeMillis
                            val fechaDia = sdfDia.format(Date(millis))      // ej "28/10/2025"
                            val fechaFull = sdfFull.format(Date(millis))    // ej "28/10/2025 02:47:11"

                            // por ahora ponemos cam fija = 1 (igual que venías)
                            val camForThis = 1

                            val clip = ClipItem(
                                fechaDia = fechaDia,
                                fechaCompleta = fechaFull,
                                cam = camForThis,
                                storageRef = itemRef.name // ej "C0309.mp4"
                            )

                            ClipRepo.agregarClip(clip)

                            if (fechaDia == hoyString) {
                                hoyTuvoClips = true
                            }
                        }
                        .addOnCompleteListener {
                            pendientes -= 1
                            if (pendientes == 0) {
                                // ya procesamos TODO Firebase
                                if (!hoyTuvoClips) {
                                    // si Firebase NO tenía nada etiquetado con la fecha de hoy,
                                    // metemos los 4 clips fake para hoy
                                    agregarClipsDeHoyPredeterminados()
                                }
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

                // si falló Firebase igual queremos poder ver algo hoy
                agregarClipsDeHoyPredeterminados()
                refrescarListaFechas()
            }
    }

    /**
     * Esta función crea 4 clips "dummy" (fijos) y los mete en ClipRepo
     * bajo la fecha actual (hoyString). Eso hace que:
     *
     * - Siempre exista la tarjeta "28/10/2025"
     * - Al abrirla, GrabacionesDetalleActivity muestre esos clips
     */
    private fun agregarClipsDeHoyPredeterminados() {
        val marcaCompleta = sdfFull.format(Date())

        // Nombres EXACTOS según tu Firebase Storage
        val nombresClips = listOf(
            "C0309.MP4",
            "C0311.MP4",
            "C0325.MP4",
            "C0333.MP4"
        )

        for (nombre in nombresClips) {
            val clip = ClipItem(
                fechaDia = hoyString,
                fechaCompleta = marcaCompleta,
                cam = 1,
                storageRef = nombre
            )
            ClipRepo.agregarClip(clip)
        }
    }

    /**
     * Rellena la lista de viñetas (fechas únicas) en pantalla
     */
    private fun refrescarListaFechas() {
        val listaFechas = ClipRepo.fechasUnicas() // ya viene ordenada por cómo la guardás

        fechasUnicas.clear()
        fechasUnicas.addAll(listaFechas)

        // si por alguna razón hoy no está aún en la listaFechas,
        // lo forzamos al final para que se vea la viñeta igual
        if (!fechasUnicas.contains(hoyString)) {
            fechasUnicas.add(hoyString)
        }

        adapter.notifyDataSetChanged()
    }

    // =====================================================
    // ADAPTER: pinta las viñetas de fechas en el RecyclerView
    // =====================================================
    class FechasAdapter(
        private val fechas: List<String>,
        private val onClickFecha: (String) -> Unit
    ) : RecyclerView.Adapter<FechasAdapter.FechaViewHolder>() {

        class FechaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // tu layout item_fecha.xml solo tiene este TextView
            val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FechaViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_fecha, parent, false)
            return FechaViewHolder(v)
        }

        override fun onBindViewHolder(holder: FechaViewHolder, position: Int) {
            val fecha = fechas[position]
            holder.tvFecha.text = fecha

            holder.itemView.setOnClickListener {
                onClickFecha(fecha)
            }
        }

        override fun getItemCount(): Int = fechas.size
    }
}

