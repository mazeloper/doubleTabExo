package com.stacon.todoexo.custom.views

import android.content.Context
import android.media.session.PlaybackState
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.stacon.todoexo.R
import com.stacon.todoexo.custom.PlayerDoubleTapListener
import com.stacon.todoexo.custom.SeekListener

class VideoOverlay(context: Context, private val attrs: AttributeSet?) :
    ConstraintLayout(context, attrs), PlayerDoubleTapListener {

    private var rootLayout: ConstraintLayout
    private var secondsView: SecondsView
    private var circleClipTapView: CircleClipTapView
    private var playerView: DoubleTapPlayerView? = null

    private var seekListener: SeekListener? = null
    private var performListener: PerformListener? = null

    private var player: Player? = null
    private var playerViewRef: Int = -1

    constructor(context: Context) : this(context, null) {
        this.visibility = View.INVISIBLE
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.vd_overlay, this, true)

        rootLayout = findViewById(R.id.root_constraint_layout)
        secondsView = findViewById(R.id.seconds_view)
        circleClipTapView = findViewById(R.id.circle_clip_tap_view)

        initAttributes()
        secondsView.isForward = true
        changeConstraints(true)

        // Animation End Callback
        circleClipTapView.performAtEnd = {
            performListener?.onAnimationEnd()

            secondsView.visibility = View.INVISIBLE
            secondsView.seconds = 0
            secondsView.stop()
        }
    }

    private fun initAttributes() {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.VideoOverlay, 0, 0)

            playerViewRef = ta.getResourceId(R.styleable.VideoOverlay_vd_playerView, -1)

            animationDuration =
                ta.getInt(R.styleable.VideoOverlay_vd_animationDuration, 650).toLong()

            seekSeconds = ta.getInt(R.styleable.VideoOverlay_vd_seekSeconds, 10)

            iconAnimationDuration =
                ta.getInt(R.styleable.VideoOverlay_vd_iconAnimationDuration, 750).toLong()

            arcSize = ta.getDimensionPixelSize(
                R.styleable.VideoOverlay_vd_arcSize,
                context.resources.getDimensionPixelSize(R.dimen.vd_arc_size)
            ).toFloat()

            tapCircleColor = ta.getColor(
                R.styleable.VideoOverlay_vd_tapCircleColor,
                ContextCompat.getColor(context, R.color.vd_tap_circle_color)
            )


            circleBgColor = ta.getColor(
                R.styleable.VideoOverlay_vd_backgroundCircleColor,
                ContextCompat.getColor(context, R.color.vd_background_circle_color)
            )

            // Seconds TextAppearance
            textAppearance = ta.getResourceId(
                R.styleable.VideoOverlay_vd_textAppearance,
                R.style.SecondsTextAppearance
            )

            // Seconds icon
            icon = ta.getResourceId(
                R.styleable.VideoOverlay_vd_icon,
                R.drawable.ic_play_triangle
            )
            ta.recycle()
        } else {
            // Set defaults
            arcSize = context.resources.getDimensionPixelSize(R.dimen.vd_arc_size).toFloat()
            tapCircleColor = ContextCompat.getColor(context, R.color.vd_tap_circle_color)
            circleBgColor = ContextCompat.getColor(context, R.color.vd_background_circle_color)
            animationDuration = 650
            iconAnimationDuration = 750
            seekSeconds = 10
            textAppearance = R.style.SecondsTextAppearance
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (playerViewRef != -1) {
            playerView((this.parent as View).findViewById(playerViewRef) as DoubleTapPlayerView)
        }
    }

    fun playerView(playerView: DoubleTapPlayerView) = apply {
        this.playerView = playerView
    }

    fun player(player: Player) = apply {
        this.player = player
    }

    fun seekListener(listener: SeekListener) = apply {
        seekListener = listener
    }

    fun performListener(listener: PerformListener) = apply {
        performListener = listener
    }

    fun seekSeconds(seconds: Int) = apply {
        seekSeconds = seconds
    }

    fun tapCircleColorRes(@ColorRes resId: Int) = apply {
        tapCircleColor = ContextCompat.getColor(context, resId)
    }

    fun tapCircleColorInt(@ColorInt color: Int) = apply {
        tapCircleColor = color
    }

    fun circleBackgroundColorRes(@ColorRes resId: Int) = apply {
        circleBgColor = ContextCompat.getColor(context, resId)
    }

    fun circleBackgroundColorInt(@ColorInt color: Int) = apply {
        circleBgColor = color
    }

    fun animationDuration(duration: Long) = apply {
        animationDuration = duration
    }

    fun arcSize(@DimenRes resId: Int) = apply {
        arcSize = context.resources.getDimension(resId)
    }

    fun arcSize(px: Float) = apply {
        arcSize = px
    }

    fun iconAnimationDuration(duration: Long) = apply {
        iconAnimationDuration = duration
    }

    fun icon(@DrawableRes resId: Int) = apply {
        icon = resId
    }

    fun textAppearance(@StyleRes resId: Int) = apply {
        textAppearance = resId
    }

    var animationDuration: Long
        get() = circleClipTapView.animationDuration
        private set(value) {
            circleClipTapView.animationDuration = value
        }

    /**
     * Forward / rewind duration on a tap in seconds.
     */
    var seekSeconds: Int = 0
        private set

    /**
     * Duration the icon animation (fade in + fade out) for a full cycle in milliseconds.
     */
    var iconAnimationDuration: Long = 750
        get() = secondsView.cycleDuration
        private set(value) {
            secondsView.cycleDuration = value
            field = value
        }

    /**
     * Size of the arc which will be clipped from the background circle.
     * The greater the value the more roundish the shape becomes
     */
    var arcSize: Float
        get() = circleClipTapView.arcSize
        internal set(value) {
            circleClipTapView.arcSize = value
        }

    /**
     * Color of the scaling circle on touch feedback.
     */
    var tapCircleColor: Int
        get() = circleClipTapView.circleColor
        private set(value) {
            circleClipTapView.circleColor = value
        }

    /**
     * Color of the clipped background circle
     */
    var circleBgColor: Int
        get() = circleClipTapView.circleBackgroundColor
        private set(value) {
            circleClipTapView.circleBackgroundColor = value
        }

    /**
     * Text appearance of the *xx seconds* text.
     */
    @StyleRes
    var textAppearance: Int = 0
        private set(value) {
            TextViewCompat.setTextAppearance(secondsView.textView, value)
            field = value
        }

    @DrawableRes
    var icon: Int = 0
        get() = secondsView.icon
        private set(value) {
            secondsView.stop()
            secondsView.icon = value
            field = value
        }

    override fun onDoubleTapStarted(posX: Float, posY: Float) {
        if (player == null || playerView == null)
            return

        if (performListener?.shouldForward(player!!, playerView!!, posX) == null)
            return
    }

    override fun onDoubleTapProgressUp(posX: Float, posY: Float) {
        if (player == null || playerView == null) return

        val shouldForward = performListener?.shouldForward(player!!, playerView!!, posX)
        if (this.visibility != View.VISIBLE) {
            if (shouldForward != null) {
                performListener?.onAnimationStart()
                secondsView.visibility = View.VISIBLE
                secondsView.start()
            } else
                return
        }
        when (shouldForward) {
            false -> {
                // First time tap or switched
                if (secondsView.isForward) {
                    changeConstraints(false)
                    secondsView.apply {
                        isForward = false
                        seconds = 0
                    }
                }

                // Cancel ripple and start new without triggering overlay disappearance
                // (resetting instead of ending)
                circleClipTapView.resetAnimation {
                    circleClipTapView.updatePosition(posX, posY)
                }
                rewinding()
            }
            true -> {
                // First time tap or switched
                if (!secondsView.isForward) {
                    changeConstraints(true)
                    secondsView.apply {
                        isForward = true
                        seconds = 0
                    }
                }

                // Cancel ripple and start new without triggering overlay disappearance
                // (resetting instead of ending)
                circleClipTapView.resetAnimation {
                    circleClipTapView.updatePosition(posX, posY)
                }
                forwarding()
            }
            else -> Unit
        }
    }

    /**
     * Seeks the video to desired position.
     * Calls interface functions when start reached ([SeekListener.onVideoStartReached])
     * or when end reached ([SeekListener.onVideoEndReached])
     *
     * @param newPosition desired position
     */
    private fun seekToPosition(newPosition: Long?) {
        if (newPosition == null) return

        // Start of the video reached
        if (newPosition <= 0) {
            player?.seekTo(0)

            seekListener?.onVideoStartReached()
            return
        }

        // End of the video reached
        player?.duration?.let { total ->
            if (newPosition >= total) {
                player?.seekTo(total)

                seekListener?.onVideoEndReached()
                return
            }
        }

        // Otherwise
        playerView?.keepInDoubleTapMode()
        player?.seekTo(newPosition)
    }

    private fun forwarding() {
        secondsView.seconds += seekSeconds
        seekToPosition(player?.currentPosition?.plus(seekSeconds * 1000))
    }

    private fun rewinding() {
        secondsView.seconds += seekSeconds
        seekToPosition(player?.currentPosition?.minus(seekSeconds * 1000))
    }

    private fun changeConstraints(forward: Boolean) {
        val constraintSet = ConstraintSet()
        with(constraintSet) {
            clone(rootLayout)
            if (forward) {
                clear(secondsView.id, ConstraintSet.START)
                connect(
                    secondsView.id, ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END
                )
            } else {
                clear(secondsView.id, ConstraintSet.END)
                connect(
                    secondsView.id, ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START
                )
            }
            secondsView.start()
            applyTo(rootLayout)
        }
    }

    interface PerformListener {
        /**
         * 오버레이가 보이지 않고 onDoubleTapProgressUp 이벤트가 발생했을 때 호출
         * 오버레이의 가시성은 이 인터페이스 메소드 내에서 VISIBLE 로 설정되어야 한다.
         */
        fun onAnimationStart()

        /**
         * 원 애니메이션이 완료되면 호출
         * 오버레이의 가시성은 이 인터페이스 메소드 내에서 GONE 으로 설정되어야 한다
         */
        fun onAnimationEnd()

        /**
         * Determines whether the player should forward, rewind or skip this tap by doing
         * nothing / ignoring. Is called for each tap.
         *
         * By overriding this method you can check for self-defined conditions whether showing the
         * overlay and rewinding/forwarding (e.g. if the media source valid) or skip it.
         *
         * In the following you see the default conditions for each action (if there is no media
         * to play ([PlaybackState.STATE_NONE]), an error occurred ([PlaybackState.STATE_ERROR])
         * or the media is stopped ([PlaybackState.STATE_STOPPED]) the tap will be ignored in any
         * case):
         *
         *
         *      | Action  | Current position          | Screen width portion |
         *      |---------|---------------------------|----------------------|
         *      | rewind  | greater than 500 ms       | 0% to 35%            |
         *      | forward | less than total duration  | 65% to 100%          |
         *      | ignore  |       ------------        | between 35% and 65%  |
         *
         * @param player Current [Player]
         * @param playerView [PlayerView] which accepts the taps
         * @param posX Position of the tap on the x-axis
         *
         * @return `true` to forward, `false` to rewind or `null` to ignore.
         */
        fun shouldForward(
            player: Player,
            playerView: DoubleTapPlayerView,
            posX: Float
        ): Boolean? {
            if (player.playbackState == PlaybackState.STATE_ERROR ||
                player.playbackState == PlaybackState.STATE_NONE ||
                player.playbackState == PlaybackState.STATE_STOPPED
            ) {

                playerView.cancelInDoubleTapMode()
                return null
            }

            if (player.currentPosition > 500 && posX < playerView.width * 0.35) {
                return false
            }

            if (player.currentPosition < player.duration && posX > playerView.width * 0.65) {
                return true
            }

            return null
        }
    }
}