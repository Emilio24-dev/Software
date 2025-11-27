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
        // Bloque que traduce las palabras de español a ingles
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    private lateinit var etNvrCode: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnDesvincular: Button
    private lateinit var btnGestionarCams: Button
    private lateinit var tvBackInicioNvr: TextView
    private lateinit var tvEstadoNvr: TextView
    private lateinit var tvEstadoVinculo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vincular_nvr)

        etNvrCode = findViewById(R.id.etNvrCode)
        btnGuardar = findViewById(R.id.btnGuardarNvr)
        btnCancelar = findViewById(R.id.btnCancelarNvr)
        btnDesvincular = findViewById(R.id.btnDesvincularNvr)
        btnGestionarCams = findViewById(R.id.btnGestionarCams)
        tvBackInicioNvr = findViewById(R.id.tvBackInicioNvr)
        tvEstadoNvr = findViewById(R.id.tvEstadoNvr)
        tvEstadoVinculo = findViewById(R.id.tvEstadoVinculo)

        tvBackInicioNvr.setOnClickListener { finish() }
        btnCancelar.setOnClickListener { finish() }

        btnGuardar.setOnClickListener { guardarVinculacion() }
        btnDesvincular.setOnClickListener { desvincularNvr() }

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
            btnDesvincular.visibility = Button.GONE
            return
        }

        val uid = user.uid
        val refUserLink = FirebaseDatabase.getInstance()
            .getReference("userNvrLinks")
            .child(uid)

        tvEstadoNvr.text = "Revisando si tienes un NVR vinculado..."

        refUserLink.get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    tvEstadoNvr.text = "No tienes NVR vinculado aún."
                    tvEstadoVinculo.text = ""
                    btnDesvincular.visibility = Button.GONE
                    return@addOnSuccessListener
                }

                val nvrId = snap.child("nvrId").getValue(String::class.java) ?: ""
                if (nvrId.isNotEmpty()) {
                    tvEstadoNvr.text = "NVR vinculado: $nvrId"
                    tvEstadoVinculo.text = "Este NVR subirá grabaciones a tu cuenta."
                    etNvrCode.setText(nvrId)
                    btnDesvincular.visibility = Button.VISIBLE
                } else {
                    tvEstadoNvr.text = "No tienes NVR vinculado aún."
                    tvEstadoVinculo.text = ""
                    btnDesvincular.visibility = Button.GONE
                }
            }
            .addOnFailureListener { e ->
                tvEstadoNvr.text = "Error al leer vínculo: ${e.message}"
                tvEstadoVinculo.text = ""
                btnDesvincular.visibility = Button.GONE
            }
    }

    private fun guardarVinculacion() {
        val nvrCode = etNvrCode.text.toString().trim()
        if (nvrCode.isEmpty()) {
            Toast.makeText(this, "Escribe el código del NVR", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid
        val email = user.email ?: ""

        val refByNvr = FirebaseDatabase.getInstance()
            .getReference("nvrLinksByNvrId")
            .child(nvrCode)

        val refByUser = FirebaseDatabase.getInstance()
            .getReference("userNvrLinks")
            .child(uid)

        // Primero comprobamos si ese NVR ya tiene dueño
        refByNvr.get()
            .addOnSuccessListener { snap ->
                val existingOwner = snap.child("ownerUid").getValue(String::class.java)

                if (!existingOwner.isNullOrEmpty() && existingOwner != uid) {
                    Toast.makeText(
                        this,
                        "Este NVR ya está vinculado a otra cuenta.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                // Guardamos vínculo NVR -> usuario
                val dataNvr = mapOf(
                    "ownerUid" to uid,
                    "ownerEmail" to email,
                    "lastUpdate" to System.currentTimeMillis()
                )

                // Y vínculo usuario -> NVR
                val dataUser = mapOf(
                    "nvrId" to nvrCode
                )

                refByNvr.setValue(dataNvr)
                    .continueWithTask {
                        refByUser.setValue(dataUser)
                    }
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "NVR vinculado correctamente.",
                            Toast.LENGTH_LONG
                        ).show()
                        cargarEstadoNvr()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al vincular: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al comprobar NVR: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun desvincularNvr() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        val refUser = FirebaseDatabase.getInstance()
            .getReference("userNvrLinks")
            .child(uid)

        refUser.get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    Toast.makeText(this, "No hay NVR vinculado.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val nvrId = snap.child("nvrId").getValue(String::class.java) ?: ""
                if (nvrId.isEmpty()) {
                    refUser.removeValue()
                    cargarEstadoNvr()
                    return@addOnSuccessListener
                }

                val refNvr = FirebaseDatabase.getInstance()
                    .getReference("nvrLinksByNvrId")
                    .child(nvrId)

                // Borramos las dos referencias
                refNvr.removeValue()
                    .continueWithTask {
                        refUser.removeValue()
                    }
                    .addOnSuccessListener {
                        Toast.makeText(this, "NVR desvinculado.", Toast.LENGTH_SHORT).show()
                        cargarEstadoNvr()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al desvincular: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
    }
}
