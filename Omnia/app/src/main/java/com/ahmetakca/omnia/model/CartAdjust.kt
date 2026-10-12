package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName

data class CartAdjust(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int
)
