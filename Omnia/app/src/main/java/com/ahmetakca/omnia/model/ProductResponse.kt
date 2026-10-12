package com.ahmetakca.omnia.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductResponse(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("price")
    val price: Float,
    @SerializedName("stock")
    val stock: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("is_favorite")
    var isFavorite: Boolean,
    @SerializedName("has_purchased")
    var hasPurchased: Boolean,
    @SerializedName("reviews")
val reviews: List<ReviewResponse>
) : Parcelable