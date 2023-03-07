package com.ekzak.mycustomview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.properties.Delegates

class WatchView constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context,
        attributeSet,
        defStyleAttr,
        R.style.DefaultWatchStyle)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.watchStyle)
    constructor(context: Context) : this(context, null)

    var watchTimeField: WatchTimeField? = null
        set(value) {
            field?.listeners?.remove(listener)
            field = value
            field?.listeners?.add(listener)
            requestLayout()
            invalidate()
        }

    private val listener: OnFieldChangedListener = {

    }

    private var mainColor by Delegates.notNull<Int>()
    private var digitsColor by Delegates.notNull<Int>()
    private var arrowsColor by Delegates.notNull<Int>()
    private var borderColor by Delegates.notNull<Int>()

    private val paint = Paint().apply { color = Color.BLACK }
    private val saveArea = RectF(0f, 0f, 0f, 0f)
    private val centerPoint = Point(0, 0)

    init {
        if (attributeSet != null) {
            initAttributes(attributeSet, defStyleAttr, defStyleRes)
        } else {
            initDefaults()
        }
    }

    private fun initAttributes(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.WatchView, defStyleAttr, defStyleRes)
        mainColor = typedArray.getColor(R.styleable.WatchView_mainColor, DEFAULT_MAIN_COLOR)
        digitsColor = typedArray.getColor(R.styleable.WatchView_digitsColor, DEFAULT_CONTRAST_COLOR)
        arrowsColor = typedArray.getColor(R.styleable.WatchView_arrowsColor, DEFAULT_CONTRAST_COLOR)
        borderColor = typedArray.getColor(R.styleable.WatchView_borderColor, DEFAULT_CONTRAST_COLOR)
        typedArray.recycle()
    }

    private fun initDefaults() {
        mainColor = DEFAULT_MAIN_COLOR
        digitsColor = DEFAULT_CONTRAST_COLOR
        arrowsColor = DEFAULT_CONTRAST_COLOR
        borderColor = DEFAULT_CONTRAST_COLOR
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        watchTimeField?.listeners?.add(listener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        watchTimeField?.listeners?.remove(listener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minSize = min(suggestedMinimumWidth, suggestedMinimumHeight)
        val minWidth = minSize + paddingStart + paddingEnd
        val minHeight = minSize + paddingTop + paddingBottom

        val desiredWidth = max(minWidth, DEFAULT_WATCH_SIZE + paddingStart + paddingEnd)
        val desiredHeight = max(minHeight, DEFAULT_WATCH_SIZE + paddingTop + paddingBottom)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec),
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val field = watchTimeField ?: return
        val saveWidth = w - paddingStart - paddingTop
        val saveHeight = h - paddingStart - paddingTop

        centerPoint.x = saveWidth / 2
        centerPoint.y = saveHeight / 2

        saveArea.left = paddingLeft.toFloat()
        saveArea.right = saveArea.left + saveWidth
        saveArea.top = paddingTop.toFloat()
        saveArea.bottom = saveArea.top + saveHeight
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    companion object {
        private const val DEFAULT_MAIN_COLOR = Color.WHITE
        private const val DEFAULT_CONTRAST_COLOR = Color.BLACK

        private const val DEFAULT_WATCH_SIZE = 100
    }
}
