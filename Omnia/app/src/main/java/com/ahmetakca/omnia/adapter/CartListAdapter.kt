package com.ahmetakca.omnia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ahmetakca.omnia.databinding.ItemRowBinding
import com.ahmetakca.omnia.model.CartAdjust
import com.ahmetakca.omnia.model.CartResponse
import com.bumptech.glide.Glide

class CartListAdapter(
    private val cartList: List<CartResponse>,
    private val onProductClick: (CartResponse) -> Unit,
    private val addToCartClick: (CartAdjust) -> Unit,
    private val removeToCartClick: (CartAdjust) -> Unit,
    private val deleteCartClick: (Int) -> Unit
) :
    RecyclerView.Adapter<CartListAdapter.CartHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // binding.addToCardButton.visibility = View.GONE
        binding.favoriteButton.visibility = View.GONE
        binding.deleteCartButton.visibility = View.VISIBLE
        binding.productInfo.visibility = View.VISIBLE
        return CartHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CartHolder,
        position: Int
    ) {
        Glide.with(holder.itemView.context).load(cartList.get(position).imageUrl)
            .into(holder.binding.productImage)
        holder.binding.productImage.setOnClickListener {
            onProductClick(cartList.get(position))
        }
        var productCount = cartList.get(position).quantity
        var totalPrice = cartList.get(position).totalPrice
        holder.binding.productName.text = cartList.get(position).name
        holder.binding.quantityProduct.text = productCount.toString()
        holder.binding.productPrice.text = totalPrice.toString()
        holder.binding.quantityRemove.visibility = if (productCount > 1) View.VISIBLE else View.GONE
        holder.binding.quantityAdd.setOnClickListener {
            productCount++
            cartList.get(position).quantity = productCount
            totalPrice = cartList.get(position).price * productCount
            val addItem = CartAdjust(cartList.get(position).productId, productCount)
            addToCartClick(addItem)
            holder.binding.quantityProduct.text = productCount.toString()
            holder.binding.productPrice.text = totalPrice.toString()
            holder.binding.quantityRemove.visibility = View.VISIBLE
        }
        // Add ve remove işlemleri aynı fonksiyon içinde olabilir yani bir id ile yapılabilir
        holder.binding.quantityRemove.setOnClickListener {
            if (productCount > 1) {
                productCount--
                cartList.get(position).quantity = productCount
                totalPrice = cartList.get(position).price * productCount
                val removeItem = CartAdjust(cartList.get(position).productId, productCount)
                removeToCartClick(removeItem)
                holder.binding.quantityProduct.text = productCount.toString()
                holder.binding.productPrice.text = totalPrice.toString()
                holder.binding.quantityRemove.visibility =
                    if (productCount > 1) View.VISIBLE else View.GONE
            }
            // Değer birse visibility gone olacak değilse visible
            // onProductQuantityRemove
        }
        holder.binding.deleteCartButton.setOnClickListener { deleteCartClick(cartList.get(position).productId) }
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    class CartHolder(val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root)
}