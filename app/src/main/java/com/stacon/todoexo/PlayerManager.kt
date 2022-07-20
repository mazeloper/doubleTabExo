package com.stacon.todoexo

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import com.stacon.doubletabplayerview.views.DoubleTapPlayerView
import com.stacon.doubletabplayerview.views.VideoOverlay

class PlayerManager(
    private val context: Context
) : DefaultLifecycleObserver {

    private lateinit var vdOverlay: VideoOverlay

    private lateinit var listener: Player.Listener

    private val mediaItems = mutableListOf<MediaItem>()
    private var videoReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var playerView: DoubleTapPlayerView? = null

    var player: ExoPlayer? = null
        private set

    fun isListenerInit() = ::listener.isInitialized

    fun injectView(playerView: DoubleTapPlayerView, overlay: VideoOverlay) {
        this.playerView = playerView
        this.vdOverlay = overlay
    }

    fun addMediaItem(item: MediaItem) {
        mediaItems.add(item)
    }

    fun addListener(listener: Player.Listener) {
        this.listener = listener
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }


    private fun initializePlayer() {
        player = ExoPlayer.Builder(context).build().also { player ->
            playerView?.player = player
            vdOverlay.player(player)

            if (isListenerInit()) player.addListener(listener)

            player.addMediaItems(mediaItems)
            player.playWhenReady = true
            player.seekTo(currentWindow, playbackPosition)
            player.prepare()
            player.play()
        }
    }

    private fun releasePlayers() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentMediaItemIndex
            videoReady = this.playWhenReady
            release()
        }
    }


    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (Util.SDK_INT < 24 && player == null) {
            initializePlayer()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        if (Util.SDK_INT <= 23) {
            releasePlayers()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        if (Util.SDK_INT > 23) {
            releasePlayers()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: PlayerManager? = null

        @JvmStatic
        fun getInstance(context: Context): PlayerManager =
            instance ?: synchronized(this) {
                instance ?: PlayerManager(context).also {
                    instance = it
                }
            }
    }
}