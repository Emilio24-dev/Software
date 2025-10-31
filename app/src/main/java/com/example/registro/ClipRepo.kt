package com.example.registro

data class ClipItem(
    val fechaDia: String,      // ej "27/10/2025"
    val fechaCompleta: String, // ej "27/10/2025 20:15:33"
    val cam: Int,              // 1 ó 2
    val storageRef: String,    // ej "C0318.MP4"
)

object ClipRepo {
    val clipsGuardados = mutableListOf<ClipItem>()

    fun agregarClip(item: ClipItem) {
        // lo metemos al inicio para que lo más nuevo salga primero
        clipsGuardados.add(0, item)
    }

    fun fechasUnicas(): List<String> {
        // devolver solo "27/10/2025", sin repetir
        return clipsGuardados.map { it.fechaDia }.distinct()
    }

    fun clipsPorFecha(fecha: String): List<ClipItem> {
        return clipsGuardados.filter { it.fechaDia == fecha }
    }
}
