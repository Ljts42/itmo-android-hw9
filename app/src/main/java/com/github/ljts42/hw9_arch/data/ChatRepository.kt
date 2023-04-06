package com.github.ljts42.hw9_arch.data

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Room
import com.github.ljts42.hw9_arch.network.*
import com.github.ljts42.hw9_arch.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ChatRepository(context: Context) {
    private val chatDatabase = Room.databaseBuilder(
        context, ChatDatabase::class.java, "${Constants.DB_NAME}.db"
    ).build()
    private val chatDao = chatDatabase.chatDao()

    private var isLoading = false

    suspend fun getMessages(start: Int, count: Int): List<Message> {
        var newMessages = try {
            chatDao.getMessages("1@channel").map {
                Message(
                    it.id,
                    it.from,
                    it.to,
                    if (it.type == DataType.TextData) Data(TextData(it.data), null)
                    else Data(null, ImageData(it.data)),
                    it.time
                )
            }
        } catch (e: SQLiteException) {
            Log.e("initRecycleView", "Error getting message from database: ${e.message}")
            listOf()
        }
        if (newMessages.isEmpty()) {
            newMessages = loadMessages(start, count)
        }
        return newMessages
    }

    suspend fun loadMessages(start: Int, count: Int): List<Message> {
        if (isLoading) return listOf()
        isLoading = true

        val newMessages = try {
            ServerClient.getMessages(start, count)
        } catch (e: IOException) {
            Log.e("getMessages", "Error getting messages from server: ${e.message}")
            listOf()
        }
        withContext(Dispatchers.IO) {
            saveMessages(newMessages)
        }

        isLoading = false
        return newMessages
    }

    private suspend fun saveMessages(messages: List<Message>) {
        try {
            messages.forEach {
                chatDao.addMessage(
                    MessageModel(
                        it.id!!,
                        it.from,
                        it.to!!,
                        if (it.data?.Text != null) DataType.TextData else DataType.ImageData,
                        it.data!!.Text?.text ?: it.data.Image!!.link,
                        it.time!!
                    )
                )
            }
        } catch (e: SQLiteException) {
            Log.e("saveMessages", "Error adding message to database: ${e.message}")
        }
    }
}