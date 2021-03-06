package com.angcyo.uiview.less.widget.group

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.support.annotation.LayoutRes
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.OverScroller
import com.angcyo.uiview.less.R
import com.angcyo.uiview.less.draw.RDrawBorder
import com.angcyo.uiview.less.draw.RDrawText
import com.angcyo.uiview.less.draw.RTabIndicator
import com.angcyo.uiview.less.kotlin.*
import com.angcyo.uiview.less.resources.ResUtil
import com.angcyo.uiview.less.widget.RDrawTextView

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：https://github.com/H07000223/FlycoTabLayout 3合一
 * 创建人员：Robi
 * 创建时间：2018/06/20 15:48
 * 修改人员：Robi
 * 修改时间：2018/06/20 15:48
 * 修改备注：
 * Version: 1.0.0
 */
class RTabLayout(context: Context, attributeSet: AttributeSet? = null) : ViewGroup(context, attributeSet) {

    var tabIndicator: RTabIndicator
    var drawBorder: RDrawBorder

    var firstNotifyListener = true

    var onTabLayoutListener: OnTabLayoutListener? = null
        set(value) {
            field = value
            resetItemStyle()

            if (currentItem >= 0 && firstNotifyListener) {
                field?.onTabSelector(this, currentItem, currentItem)
            }
        }
    private var currentItem = 0
    var itemEquWidth = false
    var itemWidth = -3

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RTabLayout)
        itemEquWidth = typedArray.getBoolean(R.styleable.RTabLayout_r_item_equ_width, itemEquWidth)
        firstNotifyListener = typedArray.getBoolean(R.styleable.RTabLayout_r_first_notify_listener, firstNotifyListener)
        currentItem = typedArray.getInt(R.styleable.RTabLayout_r_current_item, currentItem)
        itemWidth = typedArray.getDimensionPixelOffset(R.styleable.RTabLayout_r_item_width, itemWidth)
        typedArray.recycle()

        setWillNotDraw(false)
        tabIndicator = RTabIndicator(this, attributeSet)
        drawBorder = RDrawBorder(this, attributeSet)

        tabIndicator.curIndex = currentItem
    }


    private var isClickScrollPager = false
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        child?.apply {
            clickIt {
                val toIndex = indexOfChild(it)

                if (onTabLayoutListener?.canSelectorTab(this@RTabLayout, currentItem, toIndex) == false) {
                } else {
                    //开启ViewPager滚动回调
                    isClickScrollPager = true
                    setCurrentItem(toIndex)
//                    this@RTabLayout.scrollTo(0, 0)

                }
            }
        }
    }

    /**请调用此方法 设置tab index*/
    fun setCurrentItem(index: Int, notify: Boolean = true) {
        val oldIndex = currentItem
        currentItem = index
        tabIndicator.curIndex = index

        if (oldIndex == index) {
            if (notify) {
                onTabLayoutListener?.onTabReSelector(this, getChildAt(oldIndex), oldIndex)
            }
        } else {
            if (notify && isClickScrollPager) {
                onTabLayoutListener?.onPageScrollStateChanged(ViewPager.SCROLL_STATE_SETTLING)
            }
            if (oldIndex in 0 until childCount) {
                onTabLayoutListener?.onUnSelectorItemView(this, getChildAt(oldIndex), oldIndex)
            }
            if (index in 0 until childCount) {
                onTabLayoutListener?.onSelectorItemView(this, getChildAt(index), index)
            }
            if (notify) {
                onTabLayoutListener?.onTabSelector(this, oldIndex, index)
            }
        }
    }

    fun getCurrentItem() = currentItem

    /**ViewPager 滚动监听*/
    fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (!isClickScrollPager) {
            tabIndicator.pagerPosition = position
            tabIndicator.pagerPositionOffset = positionOffset
        }

        onTabLayoutListener?.let {
            if (currentItem == position) {
                //view pager 往下一页滚
                it.onPageScrolled(
                    this,
                    getChildAt(currentItem), getChildAt(currentItem + 1),
                    currentItem, currentItem + 1,
                    positionOffset
                )
            } else {
                //往上一页滚
                it.onPageScrolled(
                    this,
                    getChildAt(currentItem), getChildAt(position),
                    currentItem, position,
                    1f - positionOffset
                )
            }
        }
    }

    private var isViewPagerDragging = false

    /**自动关联ViewPager*/
    fun setupViewPager(viewPager: ViewPager) {
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                //L.e("call: onPageScrollStateChanged -> $state")
                val isClickScrollPagerOld = isClickScrollPager

                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    isViewPagerDragging = true
                } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                    isViewPagerDragging = false
                    isClickScrollPager = false
                }

                onTabLayoutListener?.let {
                    it.onPageScrollStateChanged(state)
                }

                if (isClickScrollPagerOld) {
                    resetItemStyle()
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (isViewPagerDragging || isClickScrollPager) {
                    this@RTabLayout.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentItem(position, false)
            }
        })

        if (currentItem >= 0) {
            viewPager.setCurrentItem(currentItem, false)
        }

        /**如果没有item, 则使用默认的item*/
        if (childCount <= 0) {
            viewPager.adapter?.let {
                val titles = mutableListOf<String>()
                for (i in 0 until it.count) {
                    titles.add(it.getPageTitle(i)?.toString() ?: "pos:$i")
                }
                resetItems(R.layout.base_tab_layout_item, titles, object : OnAddViewCallback<String>() {
                    override fun onInitView(view: View, data: String?, index: Int) {
                        super.onInitView(view, data, index)
                        view.findViewById<RDrawTextView>(R.id.base_draw_text_view).drawText.setDrawText(data)
                    }
                })
            }

            /**默认实现*/
            onTabLayoutListener = DefaultTabLayoutListener(viewPager)
        }
    }

    fun <T> resetItems(@LayoutRes layoutId: Int, items: List<T>, callback: OnAddViewCallback<T>) {
        addView(items, object : OnAddViewCallback<T>() {
            override fun getLayoutId(): Int {
                return layoutId
            }

            override fun onCreateView(child: View) {
                super.onCreateView(child)
            }

            override fun onInitView(view: View, data: T?, index: Int) {
                super.onInitView(view, data, index)
                callback.onInitView(view, data, index)
            }
        })
    }

    /**重置每个Item的样式*/
    fun resetItemStyle() {
        onTabLayoutListener?.let {
            for (i in 0 until childCount) {
                if (i == currentItem) {
                    it.onSelectorItemView(this@RTabLayout, getChildAt(i), i)
                } else {
                    it.onUnSelectorItemView(this@RTabLayout, getChildAt(i), i)
                }
            }
        }
    }


    /**以下方法不必关注*/
    private var childMaxWidth = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var heightSpec: Int
        if (heightMode != MeasureSpec.EXACTLY) {
            //没有明确指定高度的情况下, 默认的高度
            heightSize = (40 * density).toInt() + paddingTop + paddingBottom
            heightSpec = exactlyMeasure(heightSize)
        } else {
            heightSpec = exactlyMeasure(heightSize - paddingTop - paddingBottom)
        }

        //child总共的宽度
        childMaxWidth = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            val lp = childView.layoutParams as LayoutParams
            //不支持竖向margin支持
            lp.topMargin = 0
            lp.bottomMargin = 0

            val widthHeight = calcLayoutWidthHeight(
                lp.layoutWidth, lp.layoutHeight,
                widthSize, heightSize, 0, 0
            )
            val childHeightSpec = if (widthHeight[1] > 0) {
                exactlyMeasure(widthHeight[1])
            } else {
                heightSpec
            }

            if (itemEquWidth) {
                childView.measure(
                    exactlyMeasure(if (itemWidth > 0) itemWidth else (widthSize - paddingLeft - paddingRight) / childCount),
                    childHeightSpec
                )
            } else {
                if (widthHeight[0] > 0) {
                    childView.measure(exactlyMeasure(widthHeight[0]), childHeightSpec)
                } else {
                    childView.measure(atmostMeasure(widthSize - paddingLeft - paddingRight), childHeightSpec)
                }
            }

            childMaxWidth += childView.measuredWidth + lp.leftMargin + lp.rightMargin
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = (childMaxWidth + paddingLeft + paddingRight).maxValue(widthSize)
        }

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = paddingLeft
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            val lp = childView.layoutParams as LayoutParams

            left += lp.leftMargin

            val top = if (lp.gravity.have(Gravity.CENTER_VERTICAL)) {
                measuredHeight / 2 - childView.measuredHeight / 2
            } else {
                paddingTop + (measuredHeight - paddingTop - paddingBottom) / 2 - childView.measuredHeight / 2
            }

            /*默认垂直居中显示*/
            childView.layout(
                left, top,
                left + childView.measuredWidth,
                top + childView.measuredHeight
            )
            left += childView.measuredWidth + lp.rightMargin
        }
        if (changed) {
            tabIndicator.curIndex = currentItem
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (tabIndicator.indicatorType == RTabIndicator.INDICATOR_TYPE_BOTTOM_LINE) {
            tabIndicator.onDraw(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBorder.onDraw(canvas)

        if (tabIndicator.indicatorType == RTabIndicator.INDICATOR_TYPE_ROUND_RECT_BLOCK) {
            tabIndicator.onDraw(canvas)
        }
    }


    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    /**滚动支持*/
    private val overScroller = OverScroller(context)
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            //L.e("call: onFling -> \n$e1 \n$e2 \n$velocityX $velocityY")

            val absX = Math.abs(velocityX)
            val absY = Math.abs(velocityY)

            if (absX > TouchLayout.flingVelocitySlop || absY > TouchLayout.flingVelocitySlop) {
                if (absY > absX) {
                    //竖直方向的Fling操作
                    onFlingChange(
                        if (velocityY > 0) TouchLayout.ORIENTATION.BOTTOM else TouchLayout.ORIENTATION.TOP,
                        velocityY
                    )
                } else if (absX > absY) {
                    //水平方向的Fling操作
                    onFlingChange(
                        if (velocityX > 0) TouchLayout.ORIENTATION.RIGHT else TouchLayout.ORIENTATION.LEFT,
                        velocityX
                    )
                }
            }

            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            //L.e("call: onScroll -> \n$e1 \n$e2 \n$distanceX $distanceY")

            val absX = Math.abs(distanceX)
            val absY = Math.abs(distanceY)


            var handle = false
            if (absX > TouchLayout.scrollDistanceSlop || absY > TouchLayout.scrollDistanceSlop) {
                if (absY > absX) {
                    //竖直方向的Scroll操作
                    handle = onScrollChange(
                        if (distanceY > 0) TouchLayout.ORIENTATION.TOP else TouchLayout.ORIENTATION.BOTTOM,
                        distanceY
                    )
                } else if (absX > absY) {
                    //水平方向的Scroll操作
                    handle = onScrollChange(
                        if (distanceX > 0) TouchLayout.ORIENTATION.LEFT else TouchLayout.ORIENTATION.RIGHT,
                        distanceX
                    )
                }
            }

            return handle
        }
    })

    private var interceptTouchEvent = false
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
//        if (ev.isDown()) {
//            interceptTouchEvent = canScroll()
//        }
        val result = gestureDetector.onTouchEvent(ev)
        return result /*&& interceptTouchEvent*/
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (isTouchFinish(event)) {
            parent.requestDisallowInterceptTouchEvent(false)
        } else if (event.isDown()) {
            overScroller.abortAnimation()
        }
        return true
    }

    fun maxScrollX() = Math.max(childMaxWidth + paddingLeft + paddingRight - measuredWidth, 0)

    override fun scrollTo(x: Int, y: Int) {
        //L.e("call: scrollTo -> $x")
        val maxScrollX = maxScrollX()
        when {
            x > maxScrollX -> super.scrollTo(maxScrollX, y)
            x < 0 -> super.scrollTo(0, y)
            else -> super.scrollTo(x, y)
        }
    }

    private fun canScroll(): Boolean {
        return childMaxWidth + paddingLeft + paddingRight > measuredWidth
    }

    @Override
    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.currX, overScroller.currY)
            postInvalidate()
            if (overScroller.currX < 0 || overScroller.currX > childMaxWidth - measuredWidth) {
                overScroller.abortAnimation()
            }
        }
    }

    /**Scroll操作的处理方法*/
    fun onScrollChange(orientation: TouchLayout.ORIENTATION, distance: Float): Boolean {
        if (canScroll()) {
            if (orientation == TouchLayout.ORIENTATION.LEFT && scrollX >= maxScrollX()) {
                return false
            }

            if (orientation == TouchLayout.ORIENTATION.RIGHT && scrollX <= 0) {
                return false
            }

            if (orientation == TouchLayout.ORIENTATION.LEFT || orientation == TouchLayout.ORIENTATION.RIGHT) {
                scrollBy(distance.toInt(), 0)

                parent.requestDisallowInterceptTouchEvent(true)

                return true
            }
        }
        return false
    }

    /**Fling操作的处理方法*/
    open fun onFlingChange(orientation: TouchLayout.ORIENTATION, velocity: Float /*瞬时值*/) {
        if (canScroll()) {
            if (orientation == TouchLayout.ORIENTATION.LEFT) {
                startFlingX(-velocity.toInt(), childMaxWidth)
            } else if (orientation == TouchLayout.ORIENTATION.RIGHT) {
                startFlingX(-velocity.toInt(), scrollX)
            }
        }
    }

    open fun startFlingX(velocityX: Int, maxDx: Int) {
        startFling(velocityX, 0, maxDx, 0)
    }

    fun startFling(velocityX: Int, velocityY: Int, maxDx: Int, maxDy: Int) {
        overScroller.abortAnimation()
        overScroller.fling(scrollX, scrollY, velocityX, velocityY, 0, maxDx, 0, maxDy, measuredWidth, measuredHeight)
        postInvalidate()
    }

    fun startScroll(dx: Int, dy: Int = 0) {
        overScroller.startScroll(scrollX, scrollY, dx, dy, 300)
        postInvalidate()
    }

    class LayoutParams : FrameLayout.LayoutParams {
        var layoutWidth = ""
        var layoutHeight = ""

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.RTabLayout_Layout)
            layoutWidth = a.getString(R.styleable.RTabLayout_Layout_r_layout_width) ?: ""
            layoutHeight = a.getString(R.styleable.RTabLayout_Layout_r_layout_height) ?: ""
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(source: MarginLayoutParams?) : super(source)
    }

    /**事件监听*/
    open class OnTabLayoutListener {

        protected var pagerScrollState = ViewPager.SCROLL_STATE_IDLE

        open fun isScrollEnd() = pagerScrollState == ViewPager.SCROLL_STATE_IDLE

        open fun onPageScrollStateChanged(state: Int) {
            pagerScrollState = state
            //L.w("滚动状态-> $state scrollEnd:${isScrollEnd()}")
        }

        /**ViewPager滚动回调*/
        @Deprecated("")
        open fun onPageScrolled(tabLayout: RTabLayout, currentView: View?, nextView: View?, positionOffset: Float) {
        }

        open fun onPageScrolled(
            tabLayout: RTabLayout,
            currentView: View?, nextView: View?,
            currentPosition: Int, nextPosition: Int,
            positionOffset: Float
        ) {
            onPageScrolled(tabLayout, currentView, nextView, positionOffset)
            //L.w("$currentPosition->$nextPosition $positionOffset scrollEnd:${isScrollEnd()}")
            //positionOffset 距离到达 nextView 的百分比; 1f 表示已经到达nextView
//            if (currentView is TextView) {
//                currentView.setTextSizeDp(14 + 4 * (1 - positionOffset))
//            }
//
//            if (nextView is TextView) {
//                nextView.setTextSizeDp(14 + 4 * positionOffset)
//            }
        }

        /**某个Item选中, 可以自定义样式*/
        open fun onSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
            //L.w("选择-> $index scrollEnd:${isScrollEnd()}")

//            if (itemView is TextView) {
//                itemView.setTextSizeDp(14 + 4f)
//            }
        }

        /**取消Item选中*/
        open fun onUnSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
            //L.w("取消选择-> $index scrollEnd:${isScrollEnd()}")

//            if (itemView is TextView) {
//                itemView.setTextSizeDp(14f)
//            }
        }

        /**是否可以选中tab*/
        open fun canSelectorTab(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int): Boolean {
            return true
        }

        /**选中某个tab*/
        open fun onTabSelector(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int) {

        }

        /**重复选中*/
        open fun onTabReSelector(tabLayout: RTabLayout, itemView: View, index: Int) {

        }
    }

    /**默认实现*/
    open class DefaultTabLayoutListener(val viewPager: ViewPager? = null) : OnTabLayoutListener() {
        var maxTextSize: Int = ResUtil.dpToPx(16f).toInt()
        var minTextSize: Int = ResUtil.dpToPx(12f).toInt()

        override fun onPageScrolled(
            tabLayout: RTabLayout,
            currentView: View?,
            nextView: View?,
            currentPosition: Int,
            nextPosition: Int,
            positionOffset: Float
        ) {
            super.onPageScrolled(tabLayout, currentView, nextView, currentPosition, nextPosition, positionOffset)

            val currentDrawTextView = currentView?.findViewById<RDrawTextView>(R.id.base_draw_text_view)
            val nextDrawTextView = nextView?.findViewById<RDrawTextView>(R.id.base_draw_text_view)

            currentDrawTextView?.drawText?.let {
                it.drawTextSize =
                        (minTextSize + (maxTextSize - minTextSize) * (1 - positionOffset)).toInt()

                selectorTextView(positionOffset < 0.5f, it)
            }

            if ((currentPosition - nextPosition).abs() == 1) {
                nextDrawTextView?.drawText?.let {
                    it.drawTextSize =
                            (minTextSize + (maxTextSize - minTextSize) * (positionOffset)).toInt()

                    selectorTextView(positionOffset > 0.5f, it)
                }
            }
        }

        override fun onSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
            super.onSelectorItemView(tabLayout, itemView, index)
            if (isScrollEnd()) {
                val drawTextView = itemView.findViewById<RDrawTextView>(R.id.base_draw_text_view)
                drawTextView?.drawText?.let {
                    it.drawTextSize = maxTextSize
                    selectorTextView(true, it)
                }
            }
        }

        override fun onUnSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
            super.onUnSelectorItemView(tabLayout, itemView, index)
            val drawTextView = itemView.findViewById<RDrawTextView>(R.id.base_draw_text_view)
            drawTextView?.drawText?.let {
                it.drawTextSize = minTextSize
                selectorTextView(false, it)
            }
        }

        override fun canSelectorTab(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int): Boolean {
            return super.canSelectorTab(tabLayout, fromIndex, toIndex)
        }

        override fun onTabSelector(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int) {
            super.onTabSelector(tabLayout, fromIndex, toIndex)
            viewPager?.setCurrentItem(toIndex, true)
        }

        override fun onTabReSelector(tabLayout: RTabLayout, itemView: View, index: Int) {
            super.onTabReSelector(tabLayout, itemView, index)
        }

        protected fun selectorTextView(selector: Boolean, view: RDrawText) {
            if (selector) {
                view.setBoldText(true)
                view.setTextColor(ColorStateList.valueOf(Color.BLACK))
            } else {
                view.setBoldText(false)
                view.setTextColor(ResUtil.getColor(R.color.base_text_color))
            }
        }
    }
}