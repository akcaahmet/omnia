package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName

data class CartUpdate(
    @SerializedName("quantity")
    var quantity: Int
)