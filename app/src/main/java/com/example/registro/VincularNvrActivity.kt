package com.example.registro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class VincularNvrActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }


    private lateinit var etPcIp: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnDesvincularNvr: Button
    private lateinit var btnGestionarCams: Button
    private lateinit var tvBackInicioNvr: TextView
    private lateinit var tvEstadoNvr: TextView
    private lateinit var tvEstadoVinculo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vincular_nvr)

        // Views
        etPcIp = findViewById(R.id.etPcIp)
        btnGuardar = findViewById(R.id.btnGuardarNvr)
        btnCancelar = findViewById(R.id.btnCancelarNvr)
        btnDesvincularNvr = findViewById(R.id.btnDesvincularNvr)
        btnGestionarCams = findViewById(R.id.btnGestionarCams)
        tvBackInicioNvr = findViewById(R.id.tvBackInicioNvr)
        tvEstadoNvr = findViewById(R.id.tvEstadoNvr)
        tvEstadoVinculo = findViewById(R.id.tvEstadoVinculo)

        tvEstadoNvr.text = "Buscando si hay un NVR vinculado..."

        // Volver a Inicio
        tvBackInicioNvr.setOnClickListener {
            finish()
        }

        btnCancelar.setOnClickListener {
            finish()
        }

        btnGuardar.setOnClickListener {
            guardarVinculacion()
        }

        btnDesvincularNvr.setOnClickListener {
            desvincularNvr()
        }

        // Ir a la pantalla donde se registran varias cámaras
        btnGestionarCams.setOnClickListener {
            startActivity(Intent(this, NvrCamsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarEstadoNvr()
    }

    private fun cargarEstadoNvr() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            tvEstadoNvr.text = "Inicia sesión para vincular un NVR."
            tvEstadoVinculo.text = ""
            btnDesvincularNvr.visibility = Button.GONE
            return
        }

        val uid = user.uid
        val ref = FirebaseDatabase.getInstance()
            .getReference("nvrLinks")
            .child(uid)

        tvEstadoNvr.text = "Buscando si hay un NVR vinculado..."
        tvEstadoVinculo.text = ""

        ref.get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    tvEstadoNvr.text = "No hay NVR vinculado aún."
                    tvEstadoVinculo.text = "Escribe la IP de tu PC NVR y toca Guardar."
                    btnDesvincularNvr.visibility = Button.GONE
                    etPcIp.setText("")
                    return@addOnSuccessListener
                }

                val ip = snap.child("pcIp").getValue(String::class.java) ?: ""

                if (ip.isNotEmpty()) {
                    tvEstadoNvr.text = "NVR vinculado: $ip"
                    tvEstadoVinculo.text =
                        "Si quieres dejar de usar este NVR, toca \"Desvincular NVR\"."
                    etPcIp.setText(ip)
                    btnDesvincularNvr.visibility = Button.VISIBLE
                } else {
                    tvEstadoNvr.text = "NVR vinculado (sin IP guardada)."
                    tvEstadoVinculo.text = ""
                    btnDesvincularNvr.visibility = Button.GONE
                }
            }
            .addOnFailureListener { e ->
                tvEstadoNvr.text = "Error al buscar NVR: ${e.message}"
                tvEstadoVinculo.text = ""
                btnDesvincularNvr.visibility = Button.GONE
            }
    }

    private fun guardarVinculacion() {
        val ip = etPcIp.text.toString().trim()

        if (ip.isEmpty()) {
            Toast.makeText(this, "Escribe la IP de la PC (NVR)", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid

        val ref = FirebaseDatabase.getInstance()
            .getReference("nvrLinks")
            .child(uid)

        // Ya no guardamos nombre de cámara aquí, sólo la IP
        val data = mapOf(
            "pcIp" to ip
        )

        ref.setValue(data)
            .addOnSuccessListener {
                tvEstadoNvr.text = "NVR vinculado: $ip"
                tvEstadoVinculo.text =
                    "Si quieres dejar de usar este NVR, toca \"Desvincular NVR\"."
                btnDesvincularNvr.visibility = Button.VISIBLE

                Toast.makeText(
                    this,
                    "NVR vinculado con esta cuenta.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al guardar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun desvincularNvr() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        val ref = FirebaseDatabase.getInstance()
            .getReference("nvrLinks")
            .child(uid)

        ref.removeValue()
            .addOnSuccessListener {
                tvEstadoNvr.text = "No hay NVR vinculado."
                tvEstadoVinculo.text = "Puedes vincular uno escribiendo la IP y tocando Guardar."
                btnDesvincularNvr.visibility = Button.GONE
                etPcIp.setText("")
                Toast.makeText(this, "NVR desvinculado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
