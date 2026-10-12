package com.ahmetakca.omnia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ahmetakca.omnia.databinding.ImageRowBinding
import com.ahmetakca.omnia.model.OrderItemResponse
import com.bumptech.glide.Glide

class ImageListAdapter(private val imageList: List<OrderItemResponse>) :
    RecyclerView.Adapter<ImageListAdapter.ImageHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageHolder {
        val binding = ImageRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ImageHolder,
        position: Int
    ) {
        Glide.with(holder.itemView.context).load(imageList.get(position).imageUrl)
            .into(holder.binding.productImageView)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ImageHolder(val binding: ImageRowBinding) : RecyclerView.ViewHolder(binding.root)
}