package com.ahmetakca.omnia.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class OrderResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("total_price")
    val totalPrice: Float,
    @SerializedName("status")
    val status: String,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("items")
    val items: List<OrderItemResponse>
) : Parcelable
