package com.ahmetakca.omnia.model

import com.google.gson.annotations.SerializedName

data class ReviewUpdate(
    @SerializedName("comment")
    var comment: String
)
