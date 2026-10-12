package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName

data class ActivateRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("code")
    val code: Int
)
