package com.github.ljts42.hw9_arch.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.ljts42.hw9_arch.data.ChatRepository

class ChatViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(ChatRepository(context), context.contentResolver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}