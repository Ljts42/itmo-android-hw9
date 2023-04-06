package com.github.ljts42.hw9_arch.ui.recycler

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.ljts42.hw9_arch.R
import com.github.ljts42.hw9_arch.network.Message
import com.github.ljts42.hw9_arch.utils.convertTime
import com.github.ljts42.hw9_arch.utils.downloadAndSetImage

class MessageViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    private val usernameView = root.findViewById<TextView>(R.id.username_view)
    private val msgIdView = root.findViewById<TextView>(R.id.msg_id_view)
    private val timeView = root.findViewById<TextView>(R.id.time_view)
    private val textView = root.findViewById<TextView>(R.id.text_view)
    val imageView: ImageView = root.findViewById(R.id.image_view)

    fun bind(message: Message) {
        if (message.data?.Text != null) {
            textView.visibility = View.VISIBLE
            imageView.visibility = View.GONE

            textView.text = message.data.Text.text
        } else if (message.data?.Image != null) {
            textView.visibility = View.GONE
            imageView.visibility = View.VISIBLE

            downloadAndSetImage("thumb/${message.data.Image.link}", imageView)
        }
        usernameView.text = message.from
        msgIdView.text = message.id.toString()
        message.time?.let {
            timeView.text = convertTime(it)
        }
    }
}