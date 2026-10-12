package com.ahmetakca.omnia.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ahmetakca.omnia.databinding.ItemRowBinding
import com.ahmetakca.omnia.model.ProductResponse
import com.bumptech.glide.Glide
import androidx.core.graphics.toColorInt

class ProductListAdapter(
    private val productList: List<ProductResponse>,
    private val onProductClick: (ProductResponse) -> Unit,
    private val addToFavoriteClick: (Int) -> Unit,
    private val removeToFavoriteClick: (Int) -> Unit
    // private val addToCartClick: (CartAdd) -> Unit
) :
    RecyclerView.Adapter<ProductListAdapter.ProductHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProductHolder,
        position: Int
    ) {
        Glide.with(holder.itemView.context)
            .load(productList.get(position).imageUrl)
            .into(holder.binding.productImage)
        holder.binding.productName.text = productList.get(position).name.toString()
        holder.binding.productPrice.text = productList.get(position).price.toString()
        holder.binding.favoriteButton.imageTintList = if (productList.get(position).isFavorite) {
            ColorStateList.valueOf("#EF5350".toColorInt())
        } else {
            ColorStateList.valueOf("#1F1F1F".toColorInt())
        }
        holder.binding.productImage.setOnClickListener {
            onProductClick(productList.get(position))
        }
        holder.binding.favoriteButton.setOnClickListener {
            if (productList.get(position).isFavorite) {
                removeToFavoriteClick(productList.get(position).productId)
                productList.get(position).isFavorite = false
            } else {
                addToFavoriteClick(productList.get(position).productId)
                productList.get(position).isFavorite = true
            }
            holder.binding.favoriteButton.imageTintList =
                if (productList.get(position).isFavorite) {
                    ColorStateList.valueOf("#EF5350".toColorInt())
                } else {
                    ColorStateList.valueOf("#1F1F1F".toColorInt())
                }
        }
        //holder.binding.addToCardButton.setOnClickListener {
        // buradaki 1 değeri alınan değer ile değiştirilecek
        //    val addItem = CartAdd(productList.get(position).productId, 1)
        //    addToCartClick(addItem)
        //}
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    class ProductHolder(val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root)
}