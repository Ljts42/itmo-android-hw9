package com.github.ljts42.hw9_arch.utils

import android.widget.ImageView
import com.github.ljts42.hw9_arch.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

fun convertTime(num: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(num))
}

fun downloadAndSetImage(link: String, view: ImageView) {
    Picasso.get().load("${Constants.BASE_URL}/$link").noFade()
        .placeholder(R.drawable.ic_broken_image).into(view)
}