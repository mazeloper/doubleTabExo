package com.stacon.todoexo.custom.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.stacon.todoexo.R
import com.stacon.todoexo.custom.PlayerDoubleTapListener

class DoubleTapPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : StyledPlayerView(context, attrs, defStyleAttr) {

    private val gestureDetector: GestureDetectorCompat
    private val gestureListener: DoubleTapGestureListener = DoubleTapGestureListener(rootView)

    private var controllerRef: Int = -1

    private var controller: PlayerDoubleTapListener? = null
        get() = gestureListener.controls
        set(value) {
            gestureListener.controls = value
            field = value
        }

    // 이 필드가 true 로 설정되면 이 보기는 두 번 탭을 처리하고, 그렇지 않으면 원래 PlayerView 와 같은 방식으로 터치를 처리
    var isDoubleTapEnabled = true

    // 더블 탭이 활성화된 시간 창에서 후속 탭은 일반 탭 대신 제스처 감지기 메서드를 호출 (PlayerView.onTouchEvent 참조).
    var doubleTapDelay: Long = 700
        get() = gestureListener.doubleTapDelay
        set(value) {
            gestureListener.doubleTapDelay = value
            field = value
        }

    init {
        gestureDetector = GestureDetectorCompat(context, gestureListener)

        attrs?.let {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.DoubleTapPlayerView, 0, 0)
            controllerRef =
                ta.getResourceId(R.styleable.DoubleTapPlayerView_vd_controller, -1) ?: -1

            ta.recycle()
        }
    }

    /**
     * 제스처 콜백을 처리하는 [PlayerDoubleTapListener]를 설정
     */
    fun controller(controller: PlayerDoubleTapListener) = apply {
        this.controller = controller
    }

    /**
     * 더블 탭의 현재 상태를 반환
     */
    fun isInDoubleTapMode(): Boolean = gestureListener.isDoubleTapping

    /**
     * 더블 탭 모드를 유지하기 위해 시간 초과를 재설정
     *
     * PlayerDoubleTapListener.onDoubleTapStarted 에서 한 번 호출됩니다. 두 번 탭이 진행 중인 탭을 감지하도록 사용자 지정/재정의된 경우 외부에서 호출해야 함
     */
    fun keepInDoubleTapMode() = gestureListener.keepInDoubleTapMode()

    /**
     * PlayerDoubleTapListener.onDoubleTapFinished 를 호출하여 두 번 탭 모드를 즉시 취소합니다.
     */
    fun cancelInDoubleTapMode() = gestureListener.cancelInDoubleTapMode()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isDoubleTapEnabled) {
            gestureDetector.onTouchEvent(event)
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (controllerRef != -1) {
            try {
                val view = (this.parent as View).findViewById(controllerRef) as View
                if (view is PlayerDoubleTapListener) {
                    controller(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private class DoubleTapGestureListener(private val rootView: View) :
        GestureDetector.SimpleOnGestureListener() {

        private val mHandler = Handler(Looper.getMainLooper())
        private val mRunnable = Runnable {
            isDoubleTapping = false
            controls?.onDoubleTapFinished()
        }

        var controls: PlayerDoubleTapListener? = null
        var isDoubleTapping = false
        var doubleTapDelay: Long = 650

        /**
         * 더블 탭 모드를 유지하기 위해 시간 초과를 재설정
         *
         * PlayerDoubleTapListener.onDoubleTapStarted 에서 한 번 호출됩니다. 두 번 탭이 진행 중인 탭을 감지하도록 사용자 지정/재정의된 경우 외부에서 호출해야 함
         */
        fun keepInDoubleTapMode() {
            isDoubleTapping = true
            mHandler.removeCallbacks(mRunnable)
            mHandler.postDelayed(mRunnable, doubleTapDelay)
        }

        /**
         * PlayerDoubleTapListener.onDoubleTapFinished 를 호출하여 두 번 탭 모드를 즉시 취소합니다.
         */
        fun cancelInDoubleTapMode() {
            mHandler.removeCallbacks(mRunnable)
            isDoubleTapping = false
            controls?.onDoubleTapFinished()
        }

        override fun onDown(e: MotionEvent): Boolean {
            if (isDoubleTapping) {
                controls?.onDoubleTapProgressDown(e.x, e.y)
                return true
            }
            return super.onDown(e)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isDoubleTapping) {
                controls?.onDoubleTapProgressUp(e.x, e.y)
                return true
            }
            return super.onSingleTapUp(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (isDoubleTapping) return true
            return rootView.performClick()
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // First tap (ACTION_DOWN) of both taps
            if (!isDoubleTapping) {
                isDoubleTapping = true
                keepInDoubleTapMode()
                controls?.onDoubleTapStarted(e.x, e.y)
            }

            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            if (e.actionMasked == MotionEvent.ACTION_UP && isDoubleTapping) {
                controls?.onDoubleTapProgressUp(e.x, e.y)
                return true
            }
            return super.onDoubleTapEvent(e)
        }
    }
}



