package com.ahmetakca.omnia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmetakca.omnia.databinding.HistoryRowBinding
import com.ahmetakca.omnia.model.OrderResponse
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class HistoryListAdapter(
    private val historyList: List<OrderResponse>,
    private val onDetailClick: (OrderResponse) -> Unit,
    private val formatter: SimpleDateFormat
) :
    RecyclerView.Adapter<HistoryListAdapter.HistoryHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryHolder {
        val binding = HistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HistoryHolder,
        position: Int
    ) {
        holder.binding.dateTextView.text = formatter.format(historyList.get(position).createdAt)
        holder.binding.totalTextView.text = historyList.get(position).totalPrice.toString()
        holder.binding.statusTextView.text = historyList.get(position).status
        holder.binding.detailImageView.setOnClickListener {
            onDetailClick(historyList.get(position))
        }
        when (historyList.get(position).status) {
            "PENDING" -> {
                holder.binding.statusTextView.setTextColor(android.graphics.Color.YELLOW)
            }

            "COMPLETED" -> {
                holder.binding.statusTextView.setTextColor(android.graphics.Color.GREEN)
            }
        }
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(
            holder.binding.root.context,
            LinearLayoutManager.HORIZONTAL, false
        )
        holder.binding.recyclerView.adapter = ImageListAdapter(historyList.get(position).items)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    class HistoryHolder(val binding: HistoryRowBinding) : RecyclerView.ViewHolder(binding.root)
}