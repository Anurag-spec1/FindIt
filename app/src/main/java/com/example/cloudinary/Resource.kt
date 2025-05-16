package com.example.cloudinary

import com.google.gson.annotations.SerializedName

data class Resource(

    @SerializedName("secure_url") val url: String,
)