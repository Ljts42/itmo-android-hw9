package com.github.ljts42.hw9_arch.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ljts42.hw9_arch.databinding.ActivityMainBinding
import com.github.ljts42.hw9_arch.ui.recycler.ChatAdapter
import com.github.ljts42.hw9_arch.viewModel.ChatViewModel
import com.github.ljts42.hw9_arch.viewModel.ChatViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatViewModel = ViewModelProvider(
            this, ChatViewModelFactory(this)
        )[ChatViewModel::class.java]

        initRecycleView()
        initObserver()

        chatViewModel.getMessages()
    }

    private fun initObserver() {
        chatViewModel.messages.observe(this) {
            (binding.chatRecycleView.adapter as ChatAdapter).update(it)
        }
    }

    private fun initRecycleView() {
        binding.chatRecycleView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ChatAdapter(chatViewModel.messages.value ?: mutableListOf(), onClickImage = {
                if (it.data?.Image != null) {
                    val intent = Intent(context, BigImageActivity::class.java)
                    intent.putExtra("imageUrl", it.data.Image.link)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            })

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = binding.chatRecycleView.layoutManager as LinearLayoutManager
                    if (layoutManager.findLastVisibleItemPosition() == chatViewModel.size() - 1) {
                        chatViewModel.loadMessages()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = binding.chatRecycleView.layoutManager as LinearLayoutManager
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && layoutManager.findLastVisibleItemPosition() == chatViewModel.size() - 1) {
                        chatViewModel.loadMessages()
                    }
                }
            })
        }
    }

    fun sendMessage(view: View) {
        chatViewModel.sendMessage(binding.inputField.text.toString())
        binding.inputField.setText("")
    }

    private val photoChooser =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                chatViewModel.sendImage(uri)
            }
        }

    fun choosePhoto(view: View) {
        photoChooser.launch("image/*")
    }
}