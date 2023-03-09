package com.ekzak.mycustomview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import viewScope
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
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

    private var watchTimeField: WatchTimeField = WatchTimeField(Calendar.getInstance().time)

    private var mainColor by Delegates.notNull<Int>()
    private var digitsColor by Delegates.notNull<Int>()
    private var arrowsColor by Delegates.notNull<Int>()
    private var borderColor by Delegates.notNull<Int>()

    private lateinit var mainPaint: Paint
    private lateinit var digitsPaint: Paint
    private lateinit var arrowsPaint: Paint
    private lateinit var borderPaint: Paint

    private val saveArea = RectF(0F, 0F, 0F, 0F)
    private val centerPoint = PointF(0F, 0F)
    private val pointF = PointF(0F, 0F)

    init {
        if (attributeSet != null) {
            initAttributes(attributeSet, defStyleAttr, defStyleRes)
        } else {
            initDefaults()
        }
        initPaints()
        if (isInEditMode) {
            watchTimeField = WatchTimeField(Calendar.getInstance().time)
        }

    }

    private fun initPaints() {
        mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mainColor
            style = Paint.Style.FILL
        }
        digitsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = digitsColor
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }
        arrowsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = arrowsColor
            strokeWidth = DEFAULT_DIGITS_SIZE.toPx(context)
            style = Paint.Style.STROKE
        }
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            strokeWidth = DEFAULT_BORDER_SIZE.toPx(context)
            style = Paint.Style.STROKE
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
        viewScope.launch {
            while (true) {
                watchTimeField.setTime(Calendar.getInstance().time)
                requestLayout()
                invalidate()
                delay(1000L)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel()
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
        val saveWidth = w - paddingStart - paddingTop
        val saveHeight = h - paddingStart - paddingTop

        centerPoint.x = saveWidth / 2F
        centerPoint.y = saveHeight / 2F

        saveArea.left = paddingLeft.toFloat()
        saveArea.right = saveArea.left + saveWidth
        saveArea.top = paddingTop.toFloat()
        saveArea.bottom = saveArea.top + saveHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawBorder(canvas)
        drawDigits(canvas)
        drawArrows(canvas)
    }

    private fun drawArrows(canvas: Canvas) {
        pointF.calculateXY(watchTimeField.hour, TypeDigit.HOUR, width / 6F)
        canvas.drawLine(centerPoint.x, centerPoint.y, pointF.x, pointF.y, arrowsPaint)
        pointF.calculateXY(watchTimeField.minute, TypeDigit.MINUTE_AND_SECOND, width / 4F)
        canvas.drawLine(centerPoint.x, centerPoint.y, pointF.x, pointF.y, arrowsPaint)
        pointF.calculateXY(watchTimeField.seconds, TypeDigit.MINUTE_AND_SECOND, width / 3F)
        canvas.drawLine(centerPoint.x, centerPoint.y, pointF.x, pointF.y, arrowsPaint)
    }

    private fun drawDigits(canvas: Canvas) {
        for (i in 1..12) {
            pointF.calculateXY(i, TypeDigit.HOUR, saveArea.width() / 2 - saveArea.width() / 10)
            canvas.drawText(i.toString(), pointF.x, pointF.y, digitsPaint.apply { textSize = width / 10F })
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawCircle(centerPoint.x, centerPoint.y, saveArea.width() / 2 - DEFAULT_BORDER_SIZE * 2, mainPaint)
    }

    private fun drawBorder(canvas: Canvas) {
        canvas.drawCircle(centerPoint.x, centerPoint.y, saveArea.width() / 2 - DEFAULT_BORDER_SIZE, borderPaint)
    }

    private fun Float.toPx(context: Context): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)

    private fun PointF.calculateXY(digit: Int, typeDigit: TypeDigit, radius: Float) {
        val startAngle = -Math.PI / 2
        val angle = when (typeDigit) {
            TypeDigit.HOUR -> HOUR_ANGLE
            TypeDigit.MINUTE_AND_SECOND -> MIN_SEC_ANGLE
        }
        val offset = startAngle + digit * angle
        x = (radius * kotlin.math.cos(offset)).toFloat() + width / 2
        y = (radius * kotlin.math.sin(offset)).toFloat() + height / 2
    }

    companion object {
        private const val DEFAULT_MAIN_COLOR = Color.BLUE
        private const val DEFAULT_CONTRAST_COLOR = Color.BLACK

        private const val DEFAULT_WATCH_SIZE = 100
        private const val DEFAULT_BORDER_SIZE = 4F
        private const val DEFAULT_DIGITS_SIZE = 2F
        private const val HOUR_ANGLE = Math.PI / 6
        private const val MIN_SEC_ANGLE = Math.PI / 30
    }

    private enum class TypeDigit {
        HOUR, MINUTE_AND_SECOND
    }
}


