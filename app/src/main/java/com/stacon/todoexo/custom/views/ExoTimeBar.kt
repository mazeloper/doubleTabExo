package com.stacon.todoexo.custom.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar

class ExoTimeBar(context: Context, attrs: AttributeSet?) : DefaultTimeBar(context, attrs),
    TimeBar.OnScrubListener {

    var callback: (Long) -> Unit = {}

    init {
        addListener(this)
    }

    fun addMoveListener(callback: (Long) -> Unit) {
        this.callback = callback
    }

    override fun onScrubStart(timeBar: TimeBar, position: Long) {}
    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {}
    override fun onScrubMove(timeBar: TimeBar, position: Long) {
        callback(position)
    }
}