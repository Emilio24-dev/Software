package com.example.registro

import android.content.Intent
import android.os.Bundle

import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {

                R.id.itemPerfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }

                R.id.itemHome -> {
                    startActivity(Intent(this, CasaActivity::class.java))
                    finish()
                    true
                }


                R.id.itemSetts-> {
                    startActivity(Intent(this, SettActivity::class.java))
                    finish()
                    true
                }


                else -> false


            }





        }
    }

}