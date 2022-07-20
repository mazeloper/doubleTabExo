package com.stacon.doubletabplayerview.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.stacon.doubletabplayerview.R
import com.stacon.doubletabplayerview.log.JsLog

class CircleClipTapView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var backgroundPaint = Paint()
    private var circlePaint = Paint()

    private var widthPx = 0
    private var heightPx = 0

    // Background
    private var shapePath = Path() // path: ex) 선을 그릴 때 똑바로 그을 수 있게 '자' 역할을 하는 클래스
    private var isLeft = true

    // Circle
    private var cX = 0f
    private var cY = 0f

    private var currentRadius = 0f
    private var minRadius = 0
    private var maxRadius = 0

    // Animation
    private var valueAnimator: ValueAnimator? = null
    private var forceReset = false

    var performAtEnd: () -> Unit = { }

    var arcSize: Float = 80f
        set(value) {
            field = value
            updatePathShape()
        }

    var circleBackgroundColor: Int
        get() = backgroundPaint.color
        set(value) {
            backgroundPaint.color = value
        }

    var circleColor: Int
        get() = circlePaint.color
        set(value) {
            circlePaint.color = value
        }

    var animationDuration: Long
        get() = valueAnimator?.duration ?: 650
        set(value) {
            getCircleAnimator().duration = value
        }

    init {
        requireNotNull(context) { "Context is null. " }

        backgroundPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true  // 가장자리 부드럽게, 내부 모양에는 영향 X
            color = ContextCompat.getColor(context, R.color.vd_background_circle_color)
        }

        circlePaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.vd_tap_circle_color)
        }

        // Pre-configurations depending on device display metrics
        val dm = context.resources.displayMetrics

        widthPx = dm.widthPixels
        heightPx = dm.heightPixels

        minRadius = (30f * dm.density).toInt()  // the logical density of the display, dp
        maxRadius = (400f * dm.density).toInt()

        updatePathShape()

        valueAnimator = getCircleAnimator()
    }

    private fun updatePathShape() {
        val halfWidth = widthPx * 0.5f

        shapePath.reset()

        val w = if (isLeft) 0f else widthPx.toFloat()
        val f = if (isLeft) 1 else -1

        shapePath.moveTo(w, 0f)                                     // 기준점을 해당 좌표로 이동한다.
        shapePath.lineTo(f * (halfWidth - arcSize) + w, 0f)     // Path 의 마지막에 경로를 추가한다.
        shapePath.quadTo(
            f * (halfWidth + arcSize) + w,
            heightPx.toFloat() / 2,
            f * (halfWidth - arcSize) + w,
            heightPx.toFloat()
        )                                                               // x1, y1 에서 x2, y2 까지 곡선을 그린다.
        shapePath.lineTo(w, heightPx.toFloat())                         // Path 의 마지막에 경로를 추가한다.

        shapePath.close()
        invalidate()    // 화면갱신
    }

    private fun getCircleAnimator(): ValueAnimator {
        valueAnimator ?: kotlin.run {
            valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = animationDuration
                addUpdateListener {
                    invalidateWithCurrentRadius(it.animatedValue as Float)
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                        visibility = VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        if (!forceReset) performAtEnd()
                    }

                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                })
            }
        }
        return valueAnimator!!
    }

    fun updatePosition(x: Float, y: Float) {
        cX = x
        cY = y

        val newIsLeft = x <= resources.displayMetrics.widthPixels / 2
        if (isLeft != newIsLeft) {
            isLeft = newIsLeft
            updatePathShape()
        }
    }

    private fun invalidateWithCurrentRadius(factor: Float) {
        currentRadius = minRadius + ((maxRadius - minRadius) * factor)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
//        JsLog.debug(">> x: $x , h: $h, oldW: $oldw, oldH: $oldh")
        widthPx = w
        heightPx = h
        updatePathShape()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Background
        canvas?.clipPath(shapePath)                     // 현재 클립을 지정된 경로와 교차시킨다.
        canvas?.drawPath(shapePath, backgroundPaint)    // 정한 경로대로 canvas 에 그리다.

        // Circle
        canvas?.drawCircle(cX, cY, currentRadius, circlePaint)
    }


    fun resetAnimation(body: () -> Unit) {
        forceReset = true
        getCircleAnimator().end()
        body()
        forceReset = false
        getCircleAnimator().start()
    }

    fun endAnimation() {
        getCircleAnimator().end()
    }
}