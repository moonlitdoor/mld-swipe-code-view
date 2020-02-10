package com.moonlitdoor.swipe.code.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat

class SwipeCodeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    View(context, attrs, defStyleAttr, defStyleRes), View.OnTouchListener, GestureDetector.OnGestureListener {

    private var code: List<Direction> = listOf()

    private var state = 0
    private var swipeCodeListeners = mutableListOf<SwipeCodeListener>()
    private var detector: GestureDetectorCompat = GestureDetectorCompat(context, this)
    private var isCardinal: Boolean = true

    init {
        @Suppress("ClickableViewAccessibility")
        this.setOnTouchListener(this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.SwipeCodeView, defStyleAttr, defStyleRes)
        isCardinal = a.getInt(R.styleable.SwipeCodeView_directions, 0) == 0
        if (isCardinal) {
            a.getString(R.styleable.SwipeCodeView_code)?.let { codeString ->
                code = codeString.split("|").map { Cardinal.valueOf(it.toUpperCase()) }
            }
        } else {
            a.getString(R.styleable.SwipeCodeView_code)?.let { codeString ->
                code = codeString.split("|").map { Intercardinal.valueOf(it.toUpperCase()) }
            }
        }
        a.recycle()
    }

    fun addSwipeCodeListener(swipeCodeListener: SwipeCodeListener) = this.swipeCodeListeners.add(swipeCodeListener)

    fun addSwipeCodeListener(listener: () -> Unit) = addSwipeCodeListener(object : SwipeCodeListener {
        override fun onSuccess() = listener.invoke()
    })

    override fun onTouch(v: View?, event: MotionEvent?): Boolean = detector.onTouchEvent(event)

    override fun onShowPress(e: MotionEvent?) {}

    override fun onLongPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean = true

    override fun onDown(e: MotionEvent?): Boolean = true

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = true

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = onSwipe(getDirection(e1.x, e1.y, e2.x, e2.y))

    private fun getDirection(x1: Float, y1: Float, x2: Float, y2: Float): Direction = if (isCardinal) Cardinal.get(
        getAngle(x1, y1, x2, y2)) else Intercardinal.get(getAngle(x1, y1, x2, y2))

    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double = ((Math.atan2((y1 - y2).toDouble(),
        (x2 - x1).toDouble()) + Math.PI) * 180 / Math.PI + 180) % 360

    private fun onSwipe(direction: Direction): Boolean = true.also {
        if (code.isNotEmpty() && direction == code[state]) {
            state++
            if (code.size == state) {
                state = 0
                swipeCodeListeners.forEach { it.onSuccess() }
            }
        } else {
            state = 0
        }
    }

    interface SwipeCodeListener {
        fun onSuccess()
    }

    interface Direction {
        val range: Pair<Double, Double>
        val range2: Pair<Double, Double>?

        fun inRange(angle: Double): Boolean {
            return angle >= range.first && angle < range.second || range2?.let { angle >= it.first && angle < it.second } ?: false
        }

    }

    interface Directional {
        fun get(angle: Double): Direction
    }

    enum class Cardinal(override val range: Pair<Double, Double>, override val range2: Pair<Double, Double>? = null) : Direction {
        UP(45.0 to 135.0),
        LEFT(135.0 to 225.0),
        DOWN(225.0 to 315.0),
        RIGHT(0.0 to 45.0, 315.0 to 360.0);

        companion object : Directional {
            override fun get(angle: Double): Direction =
                Cardinal.values().toList().find { it.inRange(angle) } ?: RIGHT
        }
    }

    enum class Intercardinal(override val range: Pair<Double, Double>, override val range2: Pair<Double, Double>? = null) : Direction {
        UPRIGHT(22.5 to 67.5),
        UP(67.5 to 112.5),
        UPLEFT(112.5 to 157.5),
        LEFT(157.5 to 202.5),
        DOWNLEFT(202.5 to 247.5),
        DOWN(247.5 to 292.5),
        DOWNRIGHT(292.5 to 337.5),
        RIGHT(0.0 to 22.5, 337.5 to 360.0);

        companion object : Directional {
            override fun get(angle: Double): Direction =
                Intercardinal.values().toList().find { it.inRange(angle) } ?: RIGHT
        }
    }

}