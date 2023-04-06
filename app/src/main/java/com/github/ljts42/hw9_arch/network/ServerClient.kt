package com.github.ljts42.hw9_arch.network

import android.graphics.Bitmap
import android.util.Log
import com.github.ljts42.hw9_arch.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException

object ServerClient {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi)).build()
    private val serverApi = retrofit.create(ServerApi::class.java)

    suspend fun getMessages(start: Int, count: Int): List<Message> {
        return withContext(Dispatchers.IO) {
            try {
                serverApi.getMessages(start, count)
            } catch (e: IOException) {
                Log.e("getMessages", "Error getting messages from server: ${e.message}")
                listOf()
            }
        }
    }

    suspend fun sendMessage(text: String) {
        withContext(Dispatchers.IO) {
            try {
                serverApi.sendMessage(
                    Message(
                        from = Constants.USERNAME, data = Data(TextData(text), null)
                    )
                )
            } catch (e: IOException) {
                Log.e("sendMessage", "Failed to send message: ${e.message}")
            }
        }
    }

    suspend fun sendImage(bitmap: Bitmap, filename: String) {
        withContext(Dispatchers.IO) {
            try {
                val messageBody = moshi.adapter(Message::class.java).toJson(
                    Message(
                        from = Constants.USERNAME
                    )
                ).toRequestBody("application/json".toMediaTypeOrNull())

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()
                val imageBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

                serverApi.sendImage(
                    messageBody, MultipartBody.Part.createFormData(
                        "picture", "$filename.jpg", imageBody
                    )
                )
            } catch (e: IOException) {
                Log.e("sendImage", "Failed to send image: ${e.message}")
            }
        }
    }
}