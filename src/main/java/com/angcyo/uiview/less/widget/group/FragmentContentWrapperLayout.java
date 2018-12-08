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
 * <p>
 * 其他布局, 将按照FragmentLayout的布局方式 叠加在 内容布局上面
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
    @Deprecated
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
        //内容在标题的下面, 线性布局方式
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        View titleView = titleView();

        int titleViewHeight = 0;
        int titleViewWidth = 0;

        int titleViewMarginVertical = 0;
        int titleViewMarginHorizontal = 0;
        if (titleView != null && titleView.getVisibility() != View.GONE) {
            //测量标题宽高
            measureChildWithMargins(titleView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            titleViewWidth = titleView.getMeasuredWidth();
            titleViewHeight = titleView.getMeasuredHeight();

            titleViewMarginHorizontal = marginHorizontal(titleView);
            titleViewMarginVertical = marginVertical(titleView);
        }

        int heightUsed = 0;
        if (contentLayoutState == CONTENT_BOTTOM_OF_TITLE) {
            heightUsed = titleViewHeight + titleViewMarginVertical;
        }

        /*视图需要多大宽度, 可以容纳子view*/
        int maxWidth = titleViewWidth + titleViewMarginHorizontal;

        int maxTitleHeight = titleViewHeight + titleViewMarginVertical;
        int maxChildHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt == titleView) {
                continue;
            }

            if (childAt.getVisibility() != View.GONE) {
                //测量内容宽高
                measureChildWithMargins(childAt, widthMeasureSpec, 0, heightMeasureSpec, heightUsed);

                maxWidth = Math.max(maxWidth, childAt.getMeasuredWidth() + marginHorizontal(childAt));
                maxChildHeight = Math.max(maxChildHeight, childAt.getMeasuredHeight() + marginVertical(childAt));
            }
        }

        if (widthMode != View.MeasureSpec.EXACTLY) {
            //宽度 wrap_content
            widthSize = maxWidth + getPaddingHorizontal();
        }

        if (heightMode != View.MeasureSpec.EXACTLY) {
            if (contentLayoutState == CONTENT_BOTTOM_OF_TITLE) {
                heightSize = maxTitleHeight + maxChildHeight + getPaddingVertical();
            } else {
                heightSize = Math.max(maxTitleHeight, maxChildHeight) + getPaddingVertical();
            }
        }

        setMeasuredDimension(widthSize, heightSize);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View titleView = titleView();

        int titleViewHeight = 0;
        int titleViewWidth = 0;

        int titleViewMarginVertical = 0;
        int titleViewMarginHorizontal = 0;
        if (titleView != null && titleView.getVisibility() != View.GONE) {
            //测量标题宽高
            titleViewWidth = titleView.getMeasuredWidth();
            titleViewHeight = titleView.getMeasuredHeight();

            titleViewMarginHorizontal = marginHorizontal(titleView);
            titleViewMarginVertical = marginVertical(titleView);
        }

        int heightUsed = 0;
        if (contentLayoutState == CONTENT_BOTTOM_OF_TITLE) {
            heightUsed = titleViewHeight + titleViewMarginVertical;
        }

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() == View.GONE) {
                continue;
            }
            MarginLayoutParams params = (MarginLayoutParams) childAt.getLayoutParams();
            int layoutTop = getPaddingTop() + params.topMargin;
            int layoutLeft = getPaddingLeft() + params.leftMargin;

            if (childAt == titleView) {

            } else {
                layoutTop += heightUsed;
            }

            childAt.layout(layoutLeft, layoutTop,
                    layoutLeft + childAt.getMeasuredWidth(),
                    layoutTop + childAt.getMeasuredHeight());
        }
    }

    private View titleView() {
        if (getChildCount() > titleViewIndex) {
            return getChildAt(titleViewIndex);
        }
        return null;
    }

    @Deprecated
    private View contentView() {
        if (getChildCount() > contentViewIndex) {
            return getChildAt(contentViewIndex);
        }
        return null;
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
