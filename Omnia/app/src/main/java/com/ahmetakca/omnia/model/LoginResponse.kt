package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    var accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)