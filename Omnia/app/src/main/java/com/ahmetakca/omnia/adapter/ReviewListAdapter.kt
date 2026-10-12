package com.ahmetakca.omnia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.ahmetakca.omnia.databinding.ReviewRowBinding
import com.ahmetakca.omnia.model.ReviewResponse
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class ReviewListAdapter(
    private val reviewList: List<ReviewResponse>,
    private val removeToReviewClick: (Int) -> Unit,
    private val editToReviewClick: (Int, String) -> Unit,
    private val formatter: SimpleDateFormat
) :
    RecyclerView.Adapter<ReviewListAdapter.ReviewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewHolder {
        val binding = ReviewRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.settingsImageButton.visibility = View.GONE
        return ReviewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ReviewHolder,
        position: Int
    ) {
        holder.binding.usernameTextView.text = reviewList.get(position).username
        holder.binding.reviewTextView.text = reviewList.get(position).comment
        holder.binding.reviewDateTextView.text = formatter.format(reviewList.get(position).reviewAt)
        if (reviewList.get(position).isOwner) {
            holder.binding.settingsImageButton.visibility = View.VISIBLE
        } else {
            holder.binding.settingsImageButton.visibility = View.GONE
        }
        holder.binding.settingsImageButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menu.add("Sil")
            popupMenu.menu.add("Düzenle")
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Sil" -> {
                        removeToReviewClick(reviewList.get(position).reviewId)
                        true
                    }

                    "Düzenle" -> {
                        editToReviewClick(
                            reviewList.get(position).reviewId,
                            reviewList.get(position).comment
                        )
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    class ReviewHolder(val binding: ReviewRowBinding) : RecyclerView.ViewHolder(binding.root)
}