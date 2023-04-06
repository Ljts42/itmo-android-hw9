package com.github.ljts42.hw9_arch.ui

import androidx.lifecycle.ViewModel
import com.github.ljts42.hw9_arch.network.Message

class ChatViewModel : ViewModel() {
    var messages: MutableList<Message> = mutableListOf()

    fun lastId(): Int {
        return if (messages.isEmpty()) 5555 else messages.last().id!!
    }
}