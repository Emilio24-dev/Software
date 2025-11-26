package com.example.registro

object ClipRepo {

    val clipsGuardados = mutableListOf<ClipItem>()

    fun agregarClip(clip: ClipItem) {
        clipsGuardados.add(clip)
    }

    fun clipsPorFecha(fechaDia: String): List<ClipItem> {
        return clipsGuardados
            .filter { it.fechaDia == fechaDia }
            .sortedBy { it.fechaCompleta }
    }

    fun fechasUnicas(): List<String> {
        return clipsGuardados
            .map { it.fechaDia }
            .distinct()
            .sortedDescending()
    }

    // 👇 NUEVO: cámaras únicas por fecha
    fun camsPorFecha(fechaDia: String): List<String> {
        return clipsGuardados
            .filter { it.fechaDia == fechaDia }
            .map { it.camLabel }
            .distinct()
            .sorted()
    }
}
