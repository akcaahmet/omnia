package com.ahmetakca.omnia.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.util.Date

@Parcelize
data class ReviewResponse(
    @SerializedName("review_id")
    val reviewId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("comment")
    var comment: String,
    @SerializedName("review_at")
    var reviewAt: Date,
    @SerializedName("is_owner")
    val isOwner: Boolean
) : Parcelable
