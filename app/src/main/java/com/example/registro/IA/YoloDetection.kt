package com.example.registro.IA

/**
 * Representa una detección devuelta por el microservicio de IA.
 * Las coordenadas (x1, y1, x2, y2) vienen en píxeles del tamaño original de la imagen.
 */
data class YoloDetection(
    val className: String,
    val confidence: Float,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)
