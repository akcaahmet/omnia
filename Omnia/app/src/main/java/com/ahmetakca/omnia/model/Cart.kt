package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.util.Date

data class Cart(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("product_id")
    val prodcutId: Int,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("created_at")
    val createdAt: Date
)
