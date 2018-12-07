package com.angcyo.uiview.less.widget.group;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.angcyo.uiview.less.widget.LoadingImageView;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：一定让标题居中的layout
 * 创建人员：Robi
 * 创建时间：2017/01/13 11:25
 * 修改人员：Robi
 * 修改时间：2017/01/13 11:25
 * 修改备注：
 * Version: 1.0.0
 */
public class RTitleCenterLayout extends ViewGroup {

    protected View mTitleView;
    protected View mLoadingView;

    int offset;
    int maxWidth;
    int widthPixels;

    public RTitleCenterLayout(Context context) {
        this(context, null);
    }

    public RTitleCenterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        widthPixels = displayMetrics.widthPixels;
        maxWidth = widthPixels * 3 / 4;
        offset = (int) (displayMetrics.density * 4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //内容在标题的下面, 线性布局方式
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);


        int loadingViewWidth = 0;
        int loadingViewHeight = 0;
        if (mLoadingView != null) {
            measureChild(mLoadingView, widthMeasureSpec, heightMeasureSpec);
            loadingViewWidth = mLoadingView.getMeasuredWidth();
            loadingViewHeight = mLoadingView.getMeasuredHeight();
        }

        int titleViewWidth = 0;
        int titleViewHeight = 0;
        if (mTitleView != null) {
            int widthUsed = loadingViewWidth * 2 + 2 * offset;
            if (widthMode != View.MeasureSpec.EXACTLY) {
                widthUsed += widthPixels - maxWidth;
            }

            measureChildWithMargins(mTitleView, widthMeasureSpec, widthUsed, heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom());
            titleViewWidth = mTitleView.getMeasuredWidth();
            titleViewHeight = mTitleView.getMeasuredHeight();
        }

        if (widthMode != View.MeasureSpec.EXACTLY) {
            //宽度 wrap_content
            widthSize = titleViewWidth + loadingViewWidth * 2 + getPaddingLeft() + getPaddingRight() + 2 * offset;
            //int widthPixels = getResources().getDisplayMetrics().widthPixels;
            //widthSize = Math.min(widthSize, widthPixels * 3 / 4);
        }

        if (heightMode != View.MeasureSpec.EXACTLY) {
            heightSize = Math.max(loadingViewHeight, titleViewHeight) + getPaddingTop() + getPaddingBottom();
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //super.onLayout(changed, l, t, r, b);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int loadViewRight = -1;
        //有标题view的情况
        if (mTitleView != null && mTitleView.getVisibility() == VISIBLE) {
            if (mTitleView instanceof TextView) {
                if (!TextUtils.isEmpty(((TextView) mTitleView).getText())) {
                    loadViewRight = layoutCenter(mTitleView, width, height) - offset;
                }
            } else {
                loadViewRight = layoutCenter(mTitleView, width, height) - offset;
            }
        }

        if (mLoadingView != null && mLoadingView.getVisibility() == VISIBLE) {
            int top = (height - mLoadingView.getMeasuredHeight()) / 2;

            if (loadViewRight == -1) {
                layoutCenter(mLoadingView, width, height);
            } else {
                mLoadingView.layout(loadViewRight - mLoadingView.getMeasuredWidth(), top,
                        loadViewRight, top + mLoadingView.getMeasuredHeight());
            }
        }

    }

    private int layoutCenter(View view, int width, int height) {
        int left = (width - view.getMeasuredWidth()) / 2;
        int top = (height - view.getMeasuredHeight()) / 2;
        view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
        return left;
    }


    //    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed, l, t, r, b);
//        int width = getMeasuredWidth();
//        int height = getMeasuredHeight();
//        if (mTitleView != null && mLoadingView != null) {
//            if (mTitleView.getVisibility() == VISIBLE) {
//                layoutCenter(mTitleView);
//                if (mLoadingView.getVisibility() == VISIBLE) {
//                    float offset = getResources().getDisplayMetrics().density * 4;
//                    mLoadingView.layout((int) (mTitleView.getLeft() - mLoadingView.getMeasuredWidth() - offset),
//                            (height - mLoadingView.getMeasuredHeight()) / 2,
//                            (int) (mTitleView.getLeft() - offset), height / 2 + mLoadingView.getMeasuredHeight() / 2);
//                }
//                //mTitleView.setBackgroundColor(Color.BLUE);
//            } else {
//                if (mLoadingView.getVisibility() == VISIBLE) {
//                    layoutCenter(mLoadingView);
//                }
//            }
//        }
//    }
//
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
//            Object tag = view.getTag();
//            if (mTitleView == null && (tag != null && "title".equalsIgnoreCase(tag.toString()))) {
//                mTitleView = view;
//            }
            if (view instanceof LoadingImageView) {
                mLoadingView = view;
                break;
            }
        }

        if (mLoadingView == null) {
            mLoadingView = findViewWithTag("loading_view");
        }

        if (mTitleView == null) {
            mTitleView = findViewWithTag("title_view");
        }
    }

    /**
     * Title View 会自动居中显示, 并且loading View始终会在TitleView 的左边
     */
    public void setTitleView(View titleView) {
        mTitleView = titleView;
        requestLayout();
    }

    public void setOffset(int offset) {
        this.offset = offset;
        requestLayout();
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        requestLayout();
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return generateLayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return generateLayoutParams(super.generateDefaultLayoutParams());
    }
}
