package com.angcyo.uiview.less.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.widget.AppCompatImageView
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import com.angcyo.uiview.less.R
import com.angcyo.uiview.less.draw.RDrawNoRead
import com.angcyo.uiview.less.kotlin.density
import com.angcyo.uiview.less.kotlin.getDrawCenterCx
import com.angcyo.uiview.less.kotlin.textWidth

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：图片和文本混排的View
 * 创建人员：Robi
 * 创建时间：2017/06/26 17:27
 * 修改人员：Robi
 * 修改时间：2017/06/26 17:27
 * 修改备注：
 * Version: 1.0.0
 */
class ImageTextView(context: Context, attributeSet: AttributeSet? = null) : AppCompatImageView(context, attributeSet) {

    /**需要绘制显示的文本*/
    var showText: String? = null
        set(value) {
            field = value
            requestLayout()
        }
    var showTextSize: Float = 14 * density
        set(value) {
            field = value
            textPaint.textSize = field
        }

    var textOffset: Int = 0
        get() {
            if (showText.isNullOrEmpty()) {
                return 0
            }
            return field
        }
        set(value) {
            field = value
            requestLayout()
        }

    var textShowColor: Int = Color.WHITE

    var imageSize: Int = 0

    var drawNoRead: RDrawNoRead

    val textPaint: Paint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG)
    }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ImageTextView)
        showText = typedArray.getString(R.styleable.ImageTextView_r_show_text)
        showTextSize =
                typedArray.getDimensionPixelOffset(R.styleable.ImageTextView_r_show_text_size, showTextSize.toInt())
                    .toFloat()
        textOffset = typedArray.getDimensionPixelOffset(R.styleable.ImageTextView_r_text_offset, 0)
        textShowColor = typedArray.getColor(R.styleable.ImageTextView_r_show_text_color, textShowColor)
        typedArray.recycle()

        drawNoRead = RDrawNoRead(this)
        drawNoRead.initAttribute(attributeSet)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val wMode = MeasureSpec.getMode(widthMeasureSpec)

        val minWidth = minimumWidth

        if (drawable == null || drawable.intrinsicWidth < 0) {
            imageSize = 0

            //无图片
            if (wMode != MeasureSpec.EXACTLY) {
                if (!TextUtils.isEmpty(showText)) {
                    val width = (paddingLeft + paddingRight + textPaint.textWidth(showText!!)).toInt()
                    setMeasuredDimension(
                        Math.max(width, minWidth),
                        measuredHeight
                    )
                }
            }
        } else {
            //有图片
            imageSize = drawable.intrinsicWidth

            if (!TextUtils.isEmpty(showText)) {
                val width =
                    (paddingLeft + paddingRight + imageSize + textOffset + textPaint.textWidth(showText!!)).toInt()
                setMeasuredDimension(
                    Math.max(width, minWidth),
                    measuredHeight
                )
            }
        }

        //L.e("call: onMeasure -> $measuredWidth $wSize $showText")
    }

    override fun onDraw(canvas: Canvas) {
        if (!TextUtils.isEmpty(showText)) {
            textPaint.color = textShowColor

            if (imageSize > 0) {
                canvas.save()
                canvas.translate(-textWidth / 2, 0f)
                super.onDraw(canvas)
                canvas.restore()

                //绘制需要显示的文本文本
                val rawHeight = measuredHeight - paddingTop - paddingBottom
                canvas.drawText(
                    showText, paddingLeft + imageSize + textOffset - 4 * density,
                    paddingTop + rawHeight / 2 + textHeight / 2 - textPaint.descent(), textPaint
                )
            } else {
                super.onDraw(canvas)

                //绘制需要显示的文本文本
                val rawHeight = measuredHeight - paddingTop - paddingBottom
                canvas.drawText(
                    showText, getDrawCenterCx() - textWidth / 2,
                    paddingTop + rawHeight / 2 + textHeight / 2 - textPaint.descent(), textPaint
                )
            }
        } else {
            super.onDraw(canvas)
        }

        drawNoRead.onDraw(canvas)
    }

    val textHeight: Float
        get() {
            textPaint.textSize = showTextSize.toFloat()
            return textPaint.descent() - textPaint.ascent()
        }
    val textWidth: Float
        get() {
            if (showText.isNullOrEmpty()) {
                return 0f
            }
            textPaint.textSize = showTextSize.toFloat()
            return textPaint.measureText(showText)
        }
}
