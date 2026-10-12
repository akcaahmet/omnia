package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName

data class ResendRequest(
    @SerializedName("email")
    val email: String
)