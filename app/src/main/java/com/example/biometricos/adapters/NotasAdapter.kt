package com.example.biometricos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.biometricos.databinding.ItemNotaBinding
import com.example.biometricos.network.EntrenamientoResponse

class NotasAdapter(
    private var notas: List<EntrenamientoResponse>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<NotasAdapter.NotaViewHolder>() {

    inner class NotaViewHolder(val binding: ItemNotaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val binding = ItemNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = notas[position]
        holder.binding.apply {
            tvMetricas.text = "${nota.distanciaKm} km • ${nota.tiempoMinutos} min"
            tvDictado.text = nota.textoOriginal
            btnDelete.setOnClickListener { onDeleteClick(nota.id) }
        }
    }

    override fun getItemCount() = notas.size

    fun updateData(newNotas: List<EntrenamientoResponse>) {
        notas = newNotas
        notifyDataSetChanged()
    }
}
