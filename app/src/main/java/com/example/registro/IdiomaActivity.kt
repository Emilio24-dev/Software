package com.example.registro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class IdiomaActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext = LocalManager.updateContextLocale(newBase)
        super.attachBaseContext(localeUpdatedContext)
    }

    private lateinit var rowEs: LinearLayout
    private lateinit var rowEn: LinearLayout
    private lateinit var checkEs: TextView
    private lateinit var checkEn: TextView
    private lateinit var tvBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_idioma)

        // Referencias a views
        rowEs = findViewById(R.id.rowEs)
        rowEn = findViewById(R.id.rowEn)
        checkEs = findViewById(R.id.checkEs)
        checkEn = findViewById(R.id.checkEn)
        tvBack = findViewById(R.id.tvBack)

        // Mostrar cuál idioma está seleccionado actualmente
        val currentLang = LocalManager.getLanguage(this)
        updateChecks(currentLang)

        // Volver atrás
        tvBack.setOnClickListener {
            finish()
        }

        // Español
        rowEs.setOnClickListener {
            changeLanguage("es")
        }

        // Inglés
        rowEn.setOnClickListener {
            changeLanguage("en")
        }
    }

    private fun changeLanguage(langCode: String) {
        val current = LocalManager.getLanguage(this)
        if (langCode == current) return

        // Guardar preferencia
        LocalManager.setLanguage(this, langCode)

        Toast.makeText(
            this,
            getString(R.string.language_changed_restart),
            Toast.LENGTH_SHORT
        ).show()

        // Volver a la pantalla principal ya con el nuevo idioma
        val intent = Intent(this, CasaActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        // Cerramos la pantalla de idioma
        finish()
    }

    private fun updateChecks(langCode: String) {
        checkEs.visibility = if (langCode == "es") View.VISIBLE else View.GONE
        checkEn.visibility = if (langCode == "en") View.VISIBLE else View.GONE
    }
}