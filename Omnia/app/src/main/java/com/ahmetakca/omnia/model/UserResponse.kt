package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("user_id")
    val id: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("is_active")
    val isActive: Boolean
)
