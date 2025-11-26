package com.example.registro

data class ClipItem(
    val fechaDia: String,          // 20/11/2025
    val fechaCompleta: String,     // 20/11/2025 21:34:22
    val camLabel: String,          // SALA, CAM1, PORTÓN
    val storageRef: String         // nombre archivo en Firebase
)
