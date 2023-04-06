package com.github.ljts42.hw9_arch.ui

import android.content.Intent
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.github.ljts42.hw9_arch.data.ChatDao
import com.github.ljts42.hw9_arch.data.ChatDatabase
import com.github.ljts42.hw9_arch.data.DataType
import com.github.ljts42.hw9_arch.data.MessageModel
import com.github.ljts42.hw9_arch.databinding.ActivityMainBinding
import com.github.ljts42.hw9_arch.network.*
import com.github.ljts42.hw9_arch.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatViewModel: ChatViewModel

    private lateinit var moshi: Moshi
    private lateinit var serverApi: ServerApi

    private lateinit var messageDatabase: ChatDatabase
    private lateinit var messageDao: ChatDao

    private var isLoading = false
    private val pickImageRequest = 9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        initDatabase()
        initRetrofit()
        initRecycleView()
    }

    private fun initRetrofit() {
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        val retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi)).build()

        serverApi = retrofit.create(ServerApi::class.java)
    }

    private fun initDatabase() {
        messageDatabase = Room.databaseBuilder(
            this, ChatDatabase::class.java, "${Constants.DB_NAME}.db"
        ).build()
        messageDao = messageDatabase.chatDao()
    }

    private fun initRecycleView() {
        val viewManager = LinearLayoutManager(this)
        binding.chatRecycleView.apply {
            layoutManager = viewManager
            adapter = ChatRecyclerAdapter(chatViewModel.messages, onClickImage = {
                if (it.data?.Image != null) {
                    val intent = Intent(context, BigImageActivity::class.java)
                    intent.putExtra("imageUrl", it.data.Image.link)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            })
        }

        if (chatViewModel.messages.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val newMessages = messageDao.getMessages("1@channel")
                    Log.d("initApp", "db size: ${newMessages.size}")
                    withContext(Dispatchers.Main) {
                        val start = chatViewModel.messages.size
                        chatViewModel.messages.addAll(newMessages.map {
                            Message(
                                it.id,
                                it.from,
                                it.to,
                                if (it.type == DataType.TextData) Data(TextData(it.data), null)
                                else Data(null, ImageData(it.data)),
                                it.time
                            )
                        })
                        binding.chatRecycleView.adapter?.notifyItemRangeInserted(
                            start, newMessages.size
                        )
                        if (chatViewModel.messages.isEmpty()) {
                            getMessages()
                        }
                    }
                } catch (e: SQLiteException) {
                    Log.e("initRecycleView", "Error getting message from database: ${e.message}")
                }
            }
        }

        binding.chatRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.chatRecycleView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == chatViewModel.messages.size - 1) {
                    getMessages()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = binding.chatRecycleView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE && layoutManager.findLastVisibleItemPosition() == chatViewModel.messages.size - 1) {
                    getMessages()
                }
            }
        })
    }

    private fun getMessages(count: Int = 20) {
        if (isLoading) return
        isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            val newMessages = try {
                serverApi.getMessages(chatViewModel.lastId(), count)
            } catch (e: IOException) {
                Log.e("getMessages", "Error getting messages from server: ${e.message}")
                listOf()
            }
            try {
                newMessages.forEach {
                    messageDao.addMessage(
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
                Log.e("getMessages", "Error adding message to database: ${e.message}")
            }
            withContext(Dispatchers.Main) {
                if (newMessages.isNotEmpty()) {
                    val start = chatViewModel.messages.size
                    chatViewModel.messages.addAll(newMessages)
                    binding.chatRecycleView.adapter?.notifyItemRangeInserted(
                        start, newMessages.size
                    )
                }
                isLoading = false
            }
        }
    }

    fun sendMessage(view: View) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                serverApi.sendMessage(
                    Message(
                        from = Constants.USERNAME,
                        data = Data(TextData(binding.inputField.text.toString()), null)
                    )
                )
            }
        } catch (e: IOException) {
            Log.e("SendMessage", "Failed to send message: ${e.message}")
        } finally {
            binding.inputField.setText("")
        }
    }

    private val photoChooser =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val inputStream = uri?.let { contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap != null) {
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
                                "picture", "${System.currentTimeMillis()}.jpg", imageBody
                            )
                        )
                    }
                } catch (e: IOException) {
                    Log.e("SendImage", "Failed to send image: ${e.message}")
                } catch (e: FileNotFoundException) {
                    Log.e("SendImage", "Failed to send image: ${e.message}")
                }
            }
        }

    fun choosePhoto(view: View) {
        photoChooser.launch("image/*")
    }
}