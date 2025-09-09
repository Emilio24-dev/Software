package com.daniela.envivo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // En vivo -> LiveActivity
        findViewById<View>(R.id.btnLive).setOnClickListener {
        startActivity(Intent(this, LiveActivity::class.java))
        }

        // Nada más por ahora (deja sin listeners los otros botones)
        // findViewById<MaterialButton>(R.id.btnRecordings)
        // findViewById<MaterialButton>(R.id.btnAlerts)
    }
}

