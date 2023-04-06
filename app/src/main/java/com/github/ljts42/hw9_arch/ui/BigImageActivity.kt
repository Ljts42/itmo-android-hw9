package com.github.ljts42.hw9_arch.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.ljts42.hw9_arch.R
import com.github.ljts42.hw9_arch.utils.downloadAndSetImage

class BigImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_image)

        val imageView: ImageView = findViewById(R.id.image_big)
        val link = intent.getStringExtra("imageUrl")
        downloadAndSetImage("img/$link", imageView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}