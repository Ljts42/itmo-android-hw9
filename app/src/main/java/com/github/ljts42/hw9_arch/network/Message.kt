package com.github.ljts42.hw9_arch.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val id: Int? = null,
    val from: String,
    val to: String? = null,
    val data: Data? = null,
    val time: Long? = null
)