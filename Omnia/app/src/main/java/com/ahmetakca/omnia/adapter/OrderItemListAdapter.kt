package com.ahmetakca.omnia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ahmetakca.omnia.databinding.OrderItemRowBinding
import com.ahmetakca.omnia.model.OrderResponse
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class OrderItemListAdapter(
    private val orderItemList: OrderResponse,
    private val formatter: SimpleDateFormat
) :
    RecyclerView.Adapter<OrderItemListAdapter.OrderItemHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderItemHolder {
        val binding =
            OrderItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderItemHolder(binding)
    }

    override fun onBindViewHolder(
        holder: OrderItemHolder,
        position: Int
    ) {
        holder.binding.predictionDateTextView.text = formatter.format(orderItemList.createdAt)
        holder.binding.deliveryNumberTextView.text = orderItemList.id.toString()
        Glide.with(holder.itemView.context).load(orderItemList.items.get(position).imageUrl)
            .into(holder.binding.productImageView)
        holder.binding.productNameTextView.text = orderItemList.items.get(position).productName
        holder.binding.productInfoTextView.text = orderItemList.status
        holder.binding.quantityTextView.text = orderItemList.items.get(position).quantity.toString()
    }

    override fun getItemCount(): Int {
        return orderItemList.items.size
    }

    class OrderItemHolder(val binding: OrderItemRowBinding) : RecyclerView.ViewHolder(binding.root)
}