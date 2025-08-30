package com.example.registro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlin.jvm.java

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
        val checkAlerts = findViewById<CheckBox>(R.id.checkAlerts)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = findViewById<Button>(R.id.btnLogout)


        btnChangePassword.setOnClickListener {
            Toast.makeText(this, "Función de cambiar contraseña...", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Modo oscuro activado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Modo claro activado", Toast.LENGTH_SHORT).show()
            }
        }

        checkAlerts.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Alertas activadas", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Alertas desactivadas", Toast.LENGTH_SHORT).show()
            }
        }


    }
}