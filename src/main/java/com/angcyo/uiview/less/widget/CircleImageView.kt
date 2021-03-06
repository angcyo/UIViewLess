package com.angcyo.uiview.less.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.angcyo.uiview.less.R
import com.angcyo.uiview.less.kotlin.calcWidthHeightRatio
import com.angcyo.uiview.less.kotlin.density

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：圆形图片
 * 创建人员：Robi
 * 创建时间：2017/06/08 17:40
 * 修改人员：Robi
 * 修改时间：2017/06/08 17:40
 * 修改备注：
 * Version: 1.0.0
 */
open class CircleImageView(context: Context, attributeSet: AttributeSet? = null) : AppCompatImageView(context, attributeSet) {

    var showType = NORMAL
        set(value) {
            field = value
            resetMaskDrawable()
            postInvalidate()
        }

    /**实现圆角的方式*/
    var schemeVersion = SCHEME_CLIP_PATH

    var roundRadius = 0f

    private val roundRectF: RectF by lazy {
        RectF()
    }

    /**默认1dp*/
    var lineWidth = 0f
    var lineColor: Int = Color.WHITE

    /**是否绘制边框*/
    var drawBorder = true
        set(value) {
            field = value
            postInvalidate()
        }

    /**在showType为NORMAL时, 是否显示边框*/
    var drawInnerBorder = false
        set(value) {
            field = value
            postInvalidate()
        }

    /**4个角的圆角信息*/
    val radius: FloatArray
        get() {
            return floatArrayOf(roundRadius, roundRadius, roundRadius, roundRadius,
                    roundRadius, roundRadius, roundRadius, roundRadius)
        }

    /**高度等于宽度*/
    protected var equWidth: Boolean = false

    /**宽度等于高度*/
    protected var equHeight: Boolean = false

    var widthHeightRatio: String? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleImageView)
        showType = typedArray.getInt(R.styleable.CircleImageView_r_show_type, showType)
        schemeVersion = typedArray.getInt(R.styleable.CircleImageView_r_scheme_version, SCHEME_CLIP_PATH)
        lineColor = typedArray.getColor(R.styleable.CircleImageView_r_line_color, lineColor)
        roundRadius = typedArray.getDimensionPixelOffset(R.styleable.CircleImageView_r_round_radius, (10 * density).toInt()).toFloat()
        lineWidth = typedArray.getDimensionPixelOffset(R.styleable.CircleImageView_r_line_width, (1 * density).toInt()).toFloat()
        equWidth = typedArray.getBoolean(R.styleable.CircleImageView_r_equ_width, equWidth)
        equHeight = typedArray.getBoolean(R.styleable.CircleImageView_r_equ_height, equHeight)
        drawBorder = typedArray.getBoolean(R.styleable.CircleImageView_r_draw_border, drawBorder)
        drawInnerBorder = typedArray.getBoolean(R.styleable.CircleImageView_r_draw_inner_border, drawInnerBorder)
        widthHeightRatio = typedArray.getString(R.styleable.CircleImageView_r_width_height_ratio)
        typedArray.recycle()
    }

    private val clipPath: Path by lazy { Path() }
    private val borderPath: Path by lazy { Path() }

    val paint: Paint by lazy {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.strokeWidth = lineWidth
        p.strokeJoin = Paint.Join.ROUND
        p.strokeCap = Paint.Cap.ROUND
        p.style = Paint.Style.STROKE
        p.isFilterBitmap = true
        p
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (equWidth) {
            setMeasuredDimension(measuredWidth, measuredWidth)
        } else if (equHeight) {
            setMeasuredDimension(measuredHeight, measuredHeight)
        } else {
            calcWidthHeightRatio(widthHeightRatio)?.let {
                setMeasuredDimension(it[0], it[1])
            }
        }
    }

    private var bitmapCanvas: Canvas? = null
    private var bitmapSource: Bitmap? = null
    private val bitmapPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = false
        }
    }
    private val bitmapClearPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = false
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    var schemeMaskDrawable: Drawable? = null
    val schemeMaskPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
    }

    private fun resetMaskDrawable() {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE//设置形状
            cornerRadii = radius
            setColor(Color.WHITE)//请不要用透明颜色试图隐藏solid
        }

        val size = Math.min(measuredHeight - paddingTop - paddingBottom,
                measuredWidth - paddingLeft - paddingRight)
        val cx = paddingLeft + size / 2
        val cy = paddingTop + size / 2
        val cr = size / 2

        when (showType) {
            ROUND -> {
                drawable.setBounds(cx - cr, cy - cr, cx + cr, cy + cr)
                schemeMaskDrawable = drawable
            }
            ROUND_RECT -> {
                drawable.setBounds(paddingLeft, paddingTop,
                        measuredWidth - paddingRight, measuredHeight - paddingBottom)
                schemeMaskDrawable = drawable
            }
            CIRCLE -> {
                drawable.setBounds(cx - cr, cy - cr, cx + cr, cy + cr)
                drawable.cornerRadius = 45 * density
                schemeMaskDrawable = drawable
            }
            else -> schemeMaskDrawable = null
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (schemeVersion == SCHEME_CANVAS_BITMAP) {
            createBitmapCanvas(w, h)
        } else if (schemeVersion == SCHEME_MASK_DRAWABLE) {
            resetMaskDrawable()
        }
    }

    private fun createBitmapCanvas(w: Int, h: Int) {
        bitmapSource?.recycle()
        bitmapCanvas = null
        if (w > 0 && h > 0) {
            bitmapSource = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmapSource)
        }
    }

    override fun onDraw(canvas: Canvas) {
        when (showType) {
            NORMAL -> {
                try {
                    super.onDraw(canvas)
                } catch (e: Exception) {
                    onDrawError()
                    e.printStackTrace()
                }
            }
            ROUND, CIRCLE, ROUND_RECT -> {
                val size = Math.min(measuredHeight - paddingTop - paddingBottom,
                        measuredWidth - paddingLeft - paddingRight)
                val cx = (paddingLeft + size / 2).toFloat()
                val cy = (paddingTop + size / 2).toFloat()
                val cr = (size / 2).toFloat()
                clipPath.reset()
                borderPath.reset()
                paint.color = lineColor

                if (showType == CIRCLE) {
                    clipPath.addCircle(cx, cy, cr, Path.Direction.CW)
                    borderPath.addCircle(cx, cy, cr - lineWidth / 2 + 0.5f, Path.Direction.CW)
                } else {
                    if (showType == ROUND_RECT) {
                        roundRectF.set(paddingLeft.toFloat(), paddingTop.toFloat(),
                                (measuredWidth - paddingRight).toFloat(), (measuredHeight - paddingBottom).toFloat())
                    } else {
                        roundRectF.set(cx - cr, cy - cr, cx + cr, cy + cr)
                    }
                    clipPath.addRoundRect(roundRectF, radius, Path.Direction.CW)
                    roundRectF.inset(lineWidth / 2 - 0.5f, lineWidth / 2 - 0.5f)
                    borderPath.addRoundRect(roundRectF, radius, Path.Direction.CW)
                }

                when (schemeVersion) {
                    SCHEME_CLIP_PATH -> {
//                        fun save(canvas: Canvas) = if (TextUtils.equals(Build.MANUFACTURER, "HUAWEI") &&
//                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /*华为手机处理圆角, 偶尔会失败.*/) {
//                            canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null)
//                        } else {
//                            canvas.save()
//                        }

                        /*DisplayListCanvas */
                        fun save(canvas: Canvas) = if (canvas.javaClass.name.contains("GLES20RecordingCanvas")) {
                            canvas.save()
                        } else {
                            canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null, Canvas.ALL_SAVE_FLAG)
                        }
//
                        var save1 = save(canvas)
                        canvas.clipPath(clipPath, Region.Op.INTERSECT)//交集显示

                        //val savePath = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
                        //var save2 = save(canvas)
                        //                if (TextUtils.equals(Build.MODEL, "VTR-AL00") /*华为P10处理圆角, 偶尔会失败.*/) {
                        //                if (TextUtils.equals(Build.MANUFACTURER, "HUAWEI") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /*华为手机处理圆角, 偶尔会失败.*/) {
                        //                    save2 = canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint, Canvas.ALL_SAVE_FLAG)
                        //                } else {
                        //                    save2 = canvas.save()
                        //                }
//                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                        try {
                            super.onDraw(canvas)
                        } catch (e: Exception) {
                            onDrawError()
                            e.printStackTrace()
                        }
                        //canvas.restoreToCount(save2)
                        if (!isInEditMode) {
                            canvas.restoreToCount(save1)
                        }
                    }
                    SCHEME_CANVAS_BITMAP -> {
                        bitmapCanvas?.let {
                            it.save()
                            it.drawPaint(bitmapClearPaint)
                            it.clipPath(clipPath)//交集显示
                            super.onDraw(it)
                            it.restore()
                            canvas.drawBitmap(bitmapSource, 0f, 0f, null)
                        }
                    }
                    SCHEME_MASK_DRAWABLE -> {
                        if (schemeMaskDrawable == null) {
                            super.onDraw(canvas)
                        } else {
                            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
                            //schemeMaskDrawable!!.setBounds(0, 0, width, height)
                            schemeMaskDrawable!!.draw(canvas)
                            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), schemeMaskPaint, Canvas.ALL_SAVE_FLAG)
                            super.onDraw(canvas)
                            canvas.restore()
                            canvas.restore()
                        }
                    }
                }

                if (drawBorder) {
                    canvas.drawPath(borderPath, paint)
                }

                //canvas.restoreToCount(savePath)

                //canvas.drawRoundRect(roundRectF, roundRadius, roundRadius, paint)
                //canvas.drawCircle(cx, cy, cr, paint)
                //canvas.restoreToCount(saveLayer)
            }
        }

        if (drawInnerBorder) {
            borderPath.reset()
            val l2 = lineWidth * 1f / 2
            borderPath.addRect(l2, l2, measuredWidth - l2, measuredHeight - l2, Path.Direction.CW)
            paint.color = lineColor
            canvas.drawPath(borderPath, paint)
        }

//        if (SHOW_DEBUG) {
//            canvas.drawText("$showType $schemeVersion", 0f, debugPaint.textHeight(), debugPaint)
//        }
    }

    open fun onDrawError() {

    }

    val drawDrawable: Drawable?
        get() {
            return drawable
        }

    companion object {
        const val NORMAL = 0
        const val CIRCLE = 1
        const val ROUND = 2 //正方形的圆角
        const val ROUND_RECT = 3 //长方形的圆角

        /**使用clip的方式实现圆角*/
        const val SCHEME_CLIP_PATH = 1
        /**使用canvas的方式实现圆角*/
        const val SCHEME_CANVAS_BITMAP = 2
        /**使用mask xf实现*/
        const val SCHEME_MASK_DRAWABLE = 3

        val SHOW_DEBUG = false
    }
}