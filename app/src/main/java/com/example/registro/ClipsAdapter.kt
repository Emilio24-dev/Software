package com.example.registro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClipsAdapter(
    private val clips: List<ClipItem>,
    private val onClipClick: (ClipItem) -> Unit
) : RecyclerView.Adapter<ClipsAdapter.ClipViewHolder>() {

    inner class ClipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCamName: TextView = itemView.findViewById(R.id.tvCamName)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clip, parent, false)
        return ClipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        val clip = clips[position]

        // Aquí usamos los datos del ClipItem
        holder.tvCamName.text = clip.camLabel      // ej: "SALA", "CAM1"
        holder.tvFechaHora.text = clip.fechaCompleta

        holder.itemView.setOnClickListener {
            onClipClick(clip)
        }
    }

    override fun getItemCount(): Int = clips.size
}
