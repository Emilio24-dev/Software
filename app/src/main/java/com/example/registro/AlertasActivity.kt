package com.example.registro

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AlertasActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma antes de que se cree la Activity
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertas)

        val rv = findViewById<RecyclerView>(R.id.rvAlertas)
        val btnVolver = findViewById<TextView>(R.id.btnVolver)

        rv.layoutManager = LinearLayoutManager(this)

        val alerts = AlertStore.getAlerts(this).reversed()
        rv.adapter = AlertasAdapter(alerts)

        btnVolver.setOnClickListener {
            finish()
        }
    }

    // ---------------- Adapter -----------------
    private class AlertasAdapter(
        private val items: List<AlertItem>
    ) : RecyclerView.Adapter<AlertasAdapter.VH>() {

        class VH(v: android.view.View) : RecyclerView.ViewHolder(v) {
            val tvMensaje: TextView = v.findViewById(R.id.tvMensaje)
            val tvDetalle: TextView = v.findViewById(R.id.tvDetalle)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alerta, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = items[pos]
            h.tvMensaje.text = item.message
            h.tvDetalle.text = "${item.cam} — ${item.fecha} ${item.horaInicio}-${item.horaFin}"
        }

        override fun getItemCount() = items.size
    }
}
