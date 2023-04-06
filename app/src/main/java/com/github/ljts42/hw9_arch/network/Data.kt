package com.github.ljts42.hw9_arch.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Data(
    val Text: TextData? = null, val Image: ImageData? = null
)