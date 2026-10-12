package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName

data class FavoriteAdjust(
    @SerializedName("product_id")
    val productId: Int
)