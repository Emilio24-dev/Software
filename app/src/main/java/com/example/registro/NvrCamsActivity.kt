package com.example.registro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NvrCamsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }


    private lateinit var tvBack: TextView
    private lateinit var etCamName: EditText
    private lateinit var etCamRtsp: EditText
    private lateinit var btnGuardar: Button
    private lateinit var rvCams: RecyclerView
    private lateinit var adapter: CamsAdapter

    private var listaCams = mutableListOf<NvrCamItem>()
    private var editKey: String? = null  // null = estamos creando, no editando

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nvr_cams)

        tvBack = findViewById(R.id.tvBackNvrCams)
        etCamName = findViewById(R.id.etCamName)
        etCamRtsp = findViewById(R.id.etCamRtsp)
        btnGuardar = findViewById(R.id.btnGuardarCam)
        rvCams = findViewById(R.id.rvListaCams)

        rvCams.layoutManager = LinearLayoutManager(this)
        adapter = CamsAdapter(listaCams,
            onEdit = { cam -> cargarParaEditar(cam) },
            onDelete = { cam -> eliminarCam(cam) }
        )
        rvCams.adapter = adapter

        tvBack.setOnClickListener { finish() }
        btnGuardar.setOnClickListener { guardarCamara() }
    }

    override fun onResume() {
        super.onResume()
        cargarCamaras()
    }

    // -----------------------------------------------------------------------------------------
    // CARGAR CÁMARAS DESDE FIREBASE
    // -----------------------------------------------------------------------------------------
    private fun cargarCamaras() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        val ref = FirebaseDatabase.getInstance()
            .getReference("nvrCams")
            .child(uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCams.clear()

                for (camSnap in snapshot.children) {
                    val cam = camSnap.getValue(NvrCamItem::class.java)
                    if (cam != null) {
                        cam.key = camSnap.key
                        listaCams.add(cam)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // -----------------------------------------------------------------------------------------
    // GUARDAR O ACTUALIZAR
    // -----------------------------------------------------------------------------------------
    private fun guardarCamara() {
        val name = etCamName.text.toString().trim()
        val rtsp = etCamRtsp.text.toString().trim()

        if (name.isEmpty() || rtsp.isEmpty()) {
            Toast.makeText(this, "Escribe nombre y RTSP", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        val ref = FirebaseDatabase.getInstance()
            .getReference("nvrCams")
            .child(uid)

        // Validación: evitar duplicados (por nombre o RTSP)
        val repetida = listaCams.any {
            it.name.equals(name, ignoreCase = true) ||
                    it.rtsp.equals(rtsp, ignoreCase = true)
        }

        if (editKey == null && repetida) {
            Toast.makeText(this, "Esa cámara ya existe", Toast.LENGTH_SHORT).show()
            return
        }

        // Si editamos → usamos la key existente
        val key = editKey ?: ref.push().key!!

        val data = mapOf(
            "name" to name,
            "rtsp" to rtsp,
            "enabled" to true
        )

        ref.child(key).setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this,
                    if (editKey == null) "Cámara agregada" else "Cámara actualizada",
                    Toast.LENGTH_SHORT
                ).show()

                limpiarCampos()
                cargarCamaras()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // -----------------------------------------------------------------------------------------
    // CARGAR EN CAMPOS PARA EDITAR
    // -----------------------------------------------------------------------------------------
    private fun cargarParaEditar(cam: NvrCamItem) {
        editKey = cam.key
        etCamName.setText(cam.name)
        etCamRtsp.setText(cam.rtsp)
        btnGuardar.text = "Actualizar cámara"
    }

    // -----------------------------------------------------------------------------------------
    // ELIMINAR CÁMARA
    // -----------------------------------------------------------------------------------------
    private fun eliminarCam(cam: NvrCamItem) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        if (cam.key == null) return

        FirebaseDatabase.getInstance()
            .getReference("nvrCams")
            .child(uid)
            .child(cam.key!!)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Cámara eliminada", Toast.LENGTH_SHORT).show()
                cargarCamaras()
            }
    }

    // -----------------------------------------------------------------------------------------
    private fun limpiarCampos() {
        etCamName.text.clear()
        etCamRtsp.text.clear()
        btnGuardar.text = "Guardar cámara"
        editKey = null
    }

    // -----------------------------------------------------------------------------------------

    data class NvrCamItem(
        var name: String = "",
        var rtsp: String = "",
        var enabled: Boolean = true,
        @JvmField var key: String? = null
    )

    class CamsAdapter(
        private val cams: List<NvrCamItem>,
        private val onEdit: (NvrCamItem) -> Unit,
        private val onDelete: (NvrCamItem) -> Unit
    ) : RecyclerView.Adapter<CamsAdapter.CamViewHolder>() {

        inner class CamViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.itemCamName)
            val tvRtsp: TextView = v.findViewById(R.id.itemCamRtsp)
            val btnEdit: ImageButton = v.findViewById(R.id.btnEditCam)
            val btnDelete: ImageButton = v.findViewById(R.id.btnDeleteCam)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CamViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_nvr_cam, parent, false)
            return CamViewHolder(v)
        }

        override fun onBindViewHolder(holder: CamViewHolder, position: Int) {
            val cam = cams[position]
            holder.tvName.text = cam.name
            holder.tvRtsp.text = cam.rtsp

            holder.btnEdit.setOnClickListener { onEdit(cam) }
            holder.btnDelete.setOnClickListener { onDelete(cam) }
        }

        override fun getItemCount(): Int = cams.size
    }
}
