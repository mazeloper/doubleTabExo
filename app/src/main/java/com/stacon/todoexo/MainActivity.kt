package com.stacon.todoexo

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.stacon.todoexo.custom.views.VideoOverlay
import com.stacon.todoexo.databinding.ActivityMainBinding
import com.stacon.todoexo.databinding.ExoPlaybackControlViewBinding

class MainActivity : AppCompatActivity() {

    private val playerManager by lazy { PlayerManager.getInstance(this) }

    private lateinit var binding: ActivityMainBinding

    private lateinit var controlsBinding: ExoPlaybackControlViewBinding

    private var isVideoFullscreen = false

    private val controller by lazy { WindowInsetsControllerCompat(window, binding.root) }

    private val rootLayout by lazy { binding.rootConstraintLayout }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val view = binding.root
        val controls = view.findViewById<ConstraintLayout>(R.id.exo_controls_root)
        controlsBinding = ExoPlaybackControlViewBinding.bind(controls)
        controlsBinding.playerManager = playerManager
        controlsBinding.activity = this

        setSystemUI(false)
        // 플레이어 라이플사이클옵저버 연결
        lifecycle.addObserver(playerManager)
        // 더블탭플레이어 초기화
        initDoubleTapPlayerView()

        // 플레이어, 오버레이 연결
        playerManager.injectView(binding.playerView, binding.vdOverlay)
        // 미디어 설정
        playerManager.addMediaItem(MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"))
        playerManager.addMediaItem(MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"))

        initViews()
    }

    private fun initDoubleTapPlayerView() {
        binding.vdOverlay.performListener(object : VideoOverlay.PerformListener {
            override fun onAnimationStart() {
                binding.playerView.useController = false
                binding.vdOverlay.visibility = View.VISIBLE
            }

            override fun onAnimationEnd() {
                binding.vdOverlay.visibility = View.GONE
                binding.playerView.useController = true
            }
        })
        binding.playerView.doubleTapDelay = 800
    }

    private fun initViews() {
        playerManager.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying)
                    controlsBinding.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_50)
                else
                    controlsBinding.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_50)
            }
        })
        controlsBinding.exoProgress.addMoveListener {
            playerManager.seekTo(it)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun toggleFullscreen() {
        if (isVideoFullscreen) {
            changeConstraints(false)
            setSystemUI(false)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            isVideoFullscreen = false
        } else {
            changeConstraints(true)
            setSystemUI(true)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            isVideoFullscreen = true
        }
    }

    fun closeScreen() {
        // TODO : 2022/07/21
    }

    /**
     * 가로 / 세로에 따른 ExoPlayer 비율 초기화
     */
    private fun changeConstraints(fullscreen: Boolean) {
        val ratio = if (fullscreen) null else "16:9"
        val constraintSet = ConstraintSet()
        with(constraintSet) {
            clone(rootLayout)
            setDimensionRatio(binding.frameLayout.id, ratio)
            applyTo(rootLayout)
        }
    }

    /**
     * 가로 / 세로에 따른 시스템 UI 초기화
     */
    private fun setSystemUI(fullscreen: Boolean) {
        binding.rootConstraintLayout.post {
            if (fullscreen) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                controller.show(WindowInsetsCompat.Type.systemBars())
                controller.show(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
            }
        }
    }

    override fun onBackPressed() {
        if (isVideoFullscreen)
            toggleFullscreen()
        else
            super.onBackPressed()
    }
}