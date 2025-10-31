package com.example.registro.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla de clips grabados
@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val timestampMs: Long,   // cuándo se grabó (en ms)
    val cam: Int,            // 1 o 2
    val fileUri: String,     // ruta/uri del archivo .mp4 que guardamos
    val aiTag: String,       // ej "Movimiento", "Persona"
    val aiSummary: String    // ej "Persona detectada frente al portón"
)
