package com.github.ljts42.hw9_arch.viewModel

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ljts42.hw9_arch.data.ChatRepository
import com.github.ljts42.hw9_arch.network.Message
import com.github.ljts42.hw9_arch.network.ServerClient
import com.github.ljts42.hw9_arch.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

class ChatViewModel(
    private val repository: ChatRepository, private val contentResolver: ContentResolver
) : ViewModel() {
    var messages = MutableLiveData<MutableList<Message>>()

    init {
        messages.value = mutableListOf()
    }

    fun size(): Int = messages.value?.size ?: 0

    private fun getLastId(): Int {
        return if (messages.value?.isEmpty() == true) Constants.START_ID else messages.value?.last()?.id!!
    }

    fun getMessages() {
        if (messages.value != null && messages.value!!.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val newMessages = repository.getMessages(Constants.START_ID, 20)
                withContext(Dispatchers.Main) {
                    if (newMessages.isNotEmpty()) {
                        messages.value?.addAll(newMessages)
                        messages.value = messages.value
                    }
                }
            }
        }
    }

    fun loadMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            val newMessages = repository.loadMessages(getLastId(), 20)
            withContext(Dispatchers.Main) {
                if (newMessages.isNotEmpty()) {
                    messages.value?.addAll(newMessages)
                    messages.value = messages.value
                }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            ServerClient.sendMessage(text)
        }
    }

    fun sendImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = uri.let { contentResolver.openInputStream(it) }
                val bitmap = BitmapFactory.decodeStream(inputStream)
                ServerClient.sendImage(bitmap, System.currentTimeMillis().toString())
            } catch (e: IOException) {
                Log.e("sendImage", "Failed to send image: ${e.message}")
            } catch (e: FileNotFoundException) {
                Log.e("sendImage", "Failed to send image: ${e.message}")
            }
        }
    }
}