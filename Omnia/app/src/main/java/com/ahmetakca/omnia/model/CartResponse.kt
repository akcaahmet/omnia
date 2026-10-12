package com.ahmetakca.omnia.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartResponse(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("price")
    val price: Float,
    @SerializedName("quantity")
    var quantity: Int,
    @SerializedName("total_price")
    var totalPrice: Float,
    @SerializedName("has_purchased")
    var hasPurchased: Boolean,
    @SerializedName("reviews")
    val reviews: List<ReviewResponse>
) : Parcelable