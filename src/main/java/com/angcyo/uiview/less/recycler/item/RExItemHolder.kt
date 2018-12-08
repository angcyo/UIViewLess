package com.angcyo.uiview.less.recycler.item

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.support.annotation.ColorRes
import android.text.TextPaint
import android.view.View
import com.angcyo.uiview.less.recycler.RBaseViewHolder
import com.angcyo.uiview.less.recycler.RExItemDecoration
import com.angcyo.uiview.less.utils.ScreenUtil
import rx.Subscription

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：Item处理类
 * 创建人员：Robi
 * 创建时间：2018/03/16 16:53
 * 修改人员：Robi
 * 修改时间：2018/03/16 16:53
 * 修改备注：
 * Version: 1.0.0
 */
abstract class RExItemHolder<DataType> {
    var exItemAdapter: RExItemAdapter<*, DataType>? = null

    fun init(exItemAdapter: RExItemAdapter<*, DataType>?) {
        this.exItemAdapter = exItemAdapter
    }

    /**当创建完之后*/
    open fun onCreateItemHolderAfter() {

    }

    /**重写此方法, 核心, onBindItemDataView 会优先 onChildViewAttachedToWindow 调用*/
    abstract fun onBindItemDataView(holder: RBaseViewHolder, posInData: Int, dataBean: DataType?)

    open fun onChildViewAttachedToWindow(view: View, adapterPosition: Int, layoutPosition: Int) {
    }

    open fun onChildViewDetachedFromWindow(view: View, adapterPosition: Int, layoutPosition: Int) {
    }

    /**用来返回 RecyclerView的分割线距离*/
    open fun getItemOffsets(itemDecoration: RExItemDecoration, outRect: Rect, position: Int, edge: Int) {

    }

    /**绘制分割线*/
    open fun draw(
        itemDecoration: RExItemDecoration,
        canvas: Canvas,
        paint: TextPaint,
        itemView: View,
        offsetRect: Rect,
        itemCount: Int,
        position: Int
    ) {
    }

    fun drawTop(canvas: Canvas, paint: TextPaint, itemView: View, offsetRect: Rect) {
        canvas.drawRect(
            0f,
            itemView.top.toFloat() - offsetRect.top,
            itemView.right.toFloat(),
            itemView.top.toFloat(),
            paint
        )
    }

    fun drawBottom(canvas: Canvas, paint: TextPaint, itemView: View, offsetRect: Rect) {
        canvas.drawRect(
            0f,
            itemView.bottom.toFloat(),
            itemView.right.toFloat(),
            itemView.bottom.toFloat() + offsetRect.bottom,
            paint
        )
    }

    fun deleteItem(position: Int) {
        exItemAdapter?.deleteItem(position)
    }

    fun deleteItem(data: DataType) {
        exItemAdapter?.deleteItem(data)
    }

    fun notifyItemChanged(position: Int) {
        exItemAdapter?.notifyItemChanged(position)
    }

    fun notifyItemRangeChanged(position: Int, itemCount: Int) {
        exItemAdapter?.notifyItemRangeChanged(position, itemCount)
    }

    fun notifyItemChanged(bean: DataType) {
        exItemAdapter?.notifyItemChanged(bean)
    }
}