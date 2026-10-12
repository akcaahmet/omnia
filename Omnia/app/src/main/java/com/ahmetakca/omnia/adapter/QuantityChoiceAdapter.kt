package com.ahmetakca.omnia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ahmetakca.omnia.databinding.QuantityRowBinding

class QuantityChoiceAdapter(
    private val quantityList: List<Int>,
    private val onQuantityClick: (Int) -> Unit
) :
    RecyclerView.Adapter<QuantityChoiceAdapter.QuantityHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): QuantityHolder {
        val binding = QuantityRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuantityHolder(binding)
    }

    override fun onBindViewHolder(
        holder: QuantityHolder,
        position: Int
    ) {
        holder.binding.quantityChoice.text = quantityList.get(position).toString()
        holder.binding.root.setOnClickListener {
            onQuantityClick(quantityList.get(position))
        }
    }

    override fun getItemCount(): Int {
        return quantityList.size
    }

    class QuantityHolder(val binding: QuantityRowBinding) : RecyclerView.ViewHolder(binding.root)
}