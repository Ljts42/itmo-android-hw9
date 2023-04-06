package com.github.ljts42.hw9_arch.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ServerApi {
    @POST("/1ch")
    suspend fun sendMessage(@Body message: Message): Response<Int>

    @Multipart
    @POST("/1ch")
    suspend fun sendImage(
        @Part("msg") message: RequestBody, @Part image: MultipartBody.Part
    ): Response<Int>

    @GET("/1ch")
    suspend fun getMessages(
        @Query("lastKnownId") start: Int, @Query("limit") count: Int
    ): List<Message>
}