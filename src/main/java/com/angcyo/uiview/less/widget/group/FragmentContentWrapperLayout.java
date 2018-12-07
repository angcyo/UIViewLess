package com.angcyo.uiview.less.widget.group;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.angcyo.uiview.less.R;

/**
 * Email:angcyo@126.com
 * 允许切换2中状态
 * <p>
 * 默认(可以指定)
 * 第一个childView 为内容
 * 第二个childView 为标题
 * 不支持3个child
 *
 * <p>
 * 1: 标题浮动在内容布局的上面
 * <p>
 * 2: 内容布局紧跟着标题的下面
 *
 * @author angcyo
 * @date 2018/12/07
 */
public class FragmentContentWrapperLayout extends FrameLayout {

    /**
     * 内容布局在标题布局的下面
     */
    public static final int CONTENT_BOTTOM_OF_TITLE = 0x01;
    /**
     * 标题浮动在内容的上面
     */
    public static final int CONTENT_BACK_OF_TITLE = CONTENT_BOTTOM_OF_TITLE << 1;

    /**
     * 内容布局的状态
     */
    private int contentLayoutState = CONTENT_BOTTOM_OF_TITLE;

    private int titleViewIndex = 1;
    private int contentViewIndex = 0;

    public FragmentContentWrapperLayout(@NonNull Context context) {
        this(context, null);
    }

    public FragmentContentWrapperLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FragmentContentWrapperLayout);
        contentLayoutState = array.getInt(R.styleable.FragmentContentWrapperLayout_r_content_layout_status, CONTENT_BOTTOM_OF_TITLE);
        titleViewIndex = array.getInt(R.styleable.FragmentContentWrapperLayout_r_title_view_index, titleViewIndex);
        contentViewIndex = array.getInt(R.styleable.FragmentContentWrapperLayout_r_content_view_index, contentViewIndex);
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() != 2 || contentLayoutState == CONTENT_BACK_OF_TITLE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            //内容在标题的下面, 线性布局方式
            int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

            View titleView = titleView();
            View contentView = contentView();

            //测量标题宽高
            measureChildWithMargins(titleView, widthMeasureSpec, 0, heightMeasureSpec, 0);

            //测量内容宽高
            int heightUsed = titleView.getMeasuredHeight() + marginVertical(titleView);
            measureChildWithMargins(contentView, widthMeasureSpec, 0, heightMeasureSpec, heightUsed);

            if (widthMode != View.MeasureSpec.EXACTLY) {
                //宽度 wrap_content
                widthSize = Math.max(titleView.getMeasuredWidth() + marginHorizontal(titleView),
                        contentView.getMeasuredWidth() + marginHorizontal(contentView)) + getPaddingHorizontal();
            }

            if (heightMode != View.MeasureSpec.EXACTLY) {
                heightSize = titleView.getMeasuredHeight() + marginVertical(titleView)
                        + contentView.getMeasuredHeight() + marginVertical(contentView)
                        + getPaddingVertical();
            }

            setMeasuredDimension(widthSize, heightSize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() != 2 || contentLayoutState == CONTENT_BACK_OF_TITLE) {
            super.onLayout(changed, left, top, right, bottom);
        } else {
            View titleView = titleView();
            View contentView = contentView();

            MarginLayoutParams titleParams = (MarginLayoutParams) titleView.getLayoutParams();
            int titleTop = getPaddingTop() + titleParams.topMargin;
            titleView.layout(getPaddingLeft() + titleParams.leftMargin, titleTop,
                    getPaddingLeft() + titleParams.leftMargin + titleView.getMeasuredWidth(),
                    titleTop + titleView.getMeasuredHeight());

            MarginLayoutParams contentParams = (MarginLayoutParams) contentView.getLayoutParams();
            int contentTop = titleView.getBottom() + titleParams.bottomMargin + contentParams.topMargin;
            contentView.layout(getPaddingLeft() + contentParams.leftMargin, contentTop,
                    getPaddingLeft() + contentParams.leftMargin + contentView.getMeasuredWidth(),
                    contentTop + contentView.getMeasuredHeight());
        }
    }

    private View titleView() {
        return getChildAt(titleViewIndex);
    }

    private View contentView() {
        return getChildAt(contentViewIndex);
    }

    /**
     * 竖直方向上的padding
     */
    public int getPaddingVertical() {
        return getPaddingTop() + getPaddingBottom();
    }

    /**
     * 水平方向上的padding
     */
    public int getPaddingHorizontal() {
        return getPaddingLeft() + getPaddingRight();
    }

    private int marginVertical(@NonNull View view) {
        MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        return params.topMargin + params.rightMargin;
    }

    private int marginHorizontal(@NonNull View view) {
        MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        return params.leftMargin + params.rightMargin;
    }

    public void setContentLayoutState(int contentLayoutState) {
        int oldState = this.contentLayoutState;
        this.contentLayoutState = contentLayoutState;
        if (oldState != contentLayoutState) {
            requestLayout();
        }
    }
}
