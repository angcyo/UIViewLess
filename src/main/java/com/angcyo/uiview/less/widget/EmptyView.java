package com.angcyo.uiview.less.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.R;

import java.util.Random;

/**
 * Created by angcyo on 2017-01-01 10:46.
 */
public class EmptyView extends View {
    public static final int SHOW_TYPE_1 = 1;
    public static final int SHOW_TYPE_2 = 2;
    Paint mPaint;
    /**
     * 默认的颜色
     */
    @ColorInt
    int defaultColor = Color.parseColor("#80E3E3E3");
    /**
     * 多少个组,每一组由一个大的三个小的组成, 仅在AT_MOST时有效
     */
    int mGroupCount = 10;
    /**
     * 每个大的矩形, 对应多少个小的矩形
     */
    int mGroupLittleCount = 4;
    int mDefaultGroupHeight = 80;//
    /**
     * 横向空隙大小
     */
    int mHSpace = 10;//
    /**
     * 竖向空隙大小
     */
    int mVSpace = 10;//
    int mLittleVSpace = 6;//
    int littlePaddingTop = 10;
    int littlePaddingBottom = 10;
    /**
     * 圆角大小
     */
    int mRoundRadius = 6;//
    RectF mRectF;
    RectF mRectFLittle;
    /**
     * 动画因子
     */
    int last = 0;
    float factor = 1f;
    private Random mRandom;
    private ValueAnimator mObjectAnimator;
    private int showType = SHOW_TYPE_1;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDefaultGroupHeight *= getDensity();
        mHSpace *= getDensity();
        mVSpace *= getDensity();
        mRoundRadius *= getDensity();
        mLittleVSpace *= getDensity();
        littlePaddingBottom *= getDensity();
        littlePaddingTop *= getDensity();

        if (!isInEditMode()) {
            final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.EmptyView);

            defaultColor = array.getColor(R.styleable.EmptyView_ev_default_color, defaultColor);
            mGroupCount = array.getInt(R.styleable.EmptyView_ev_group_count, mGroupCount);
            mDefaultGroupHeight = array.getDimensionPixelSize(R.styleable.EmptyView_ev_group_height, mDefaultGroupHeight);
            mHSpace = array.getDimensionPixelSize(R.styleable.EmptyView_ev_h_space, mHSpace);
            mVSpace = array.getDimensionPixelSize(R.styleable.EmptyView_ev_v_space, mVSpace);
            mRoundRadius = array.getDimensionPixelSize(R.styleable.EmptyView_ev_round_radius, mRoundRadius);

            array.recycle();
        }

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(defaultColor);

        mRectF = new RectF();
        mRectFLittle = new RectF();
    }

    private float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mRandom = new Random(System.currentTimeMillis());
    }

    private void initAnimator() {
        if (mObjectAnimator == null) {
            mObjectAnimator = ObjectAnimator.ofInt(3, 6);
            mObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (value != last) {
                        last = value;
                        factor = last / 10f;
                        L.e("call: onAnimationUpdate([animation])-> " + factor);
                        postInvalidate();
                    }
                }
            });
            mObjectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mObjectAnimator.setRepeatMode(ValueAnimator.REVERSE);
            mObjectAnimator.setDuration(300);
            mObjectAnimator.start();
        }
    }

    public void startAnimator() {
        initAnimator();
    }

    public void stopAnimator() {
        clearAnimator();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            //initAnimator();
        } else {
            clearAnimator();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimator();
    }

    private void clearAnimator() {
        if (mObjectAnimator != null) {
            mObjectAnimator.cancel();
            mObjectAnimator = null;
        }
    }

    /**
     * 每一个组的高度
     *
     * @param defaultGroupHeight px高度
     */
    public void setDefaultGroupHeight(int defaultGroupHeight) {
        mDefaultGroupHeight = defaultGroupHeight;
    }

    /**
     * 多少个组
     */
    public void setGroupCount(int groupCount) {
        mGroupCount = groupCount;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = getPaddingTop() + getPaddingBottom() + mDefaultGroupHeight * mGroupCount + mVSpace * (mGroupCount - 1);
        } else {
            mGroupCount = (int) Math.ceil(heightSize * 1.f / (mDefaultGroupHeight + mVSpace));
        }

        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = (int) (getPaddingLeft() + getPaddingRight() + mDefaultGroupHeight + mHSpace + mDefaultGroupHeight * 1.5f);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());

        mRectF.set(0, 0, mDefaultGroupHeight, mDefaultGroupHeight);

        int translateY = 0;
        for (int i = 1; i <= mGroupCount; i++) {

            if (showType == SHOW_TYPE_2) {
                canvas.save();
                canvas.translate(0, translateY);
            }
            drawBigRect(canvas, i);

            translateY = mDefaultGroupHeight * i;

            if (showType == SHOW_TYPE_2) {
                canvas.translate(0, translateY + mLittleVSpace * i);
                drawLittleRect(canvas, 0, 2, (int) (3 * getDensity()));
                canvas.restore();
            }
        }

        canvas.restore();
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }

    public void setObjectAnimator(ValueAnimator objectAnimator) {
        mObjectAnimator = objectAnimator;
    }

    /**
     * 绘制大矩形
     */
    private void drawBigRect(Canvas canvas, int i) {
        canvas.save();
        canvas.translate(0, (i - 1) * mDefaultGroupHeight + (i - 1) * mVSpace);
        drawRect(canvas);
        drawLittleRect(canvas, mDefaultGroupHeight + mHSpace, mGroupLittleCount, mRoundRadius);
        canvas.restore();
    }

    private void drawRect(Canvas canvas) {
        canvas.drawRoundRect(mRectF, mRoundRadius, mRoundRadius, mPaint);
    }

    private void drawLittleRect(Canvas canvas, int left, int drawableCount, int radius) {

        int height = (mDefaultGroupHeight - littlePaddingTop - littlePaddingBottom - (mGroupLittleCount - 1) * mLittleVSpace) / mGroupLittleCount;
        final int right = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();

        int top = littlePaddingTop;
        for (int i = 0; i < drawableCount; i++) {
            mRectFLittle.set(left, top, left + (right - left) * ratio(0.3f + 0.3f * mRandom.nextFloat()), top + height);
            canvas.drawRoundRect(mRectFLittle, radius, radius, mPaint);
            top += height + mLittleVSpace;
            if ((top + littlePaddingBottom) > mDefaultGroupHeight) {
                break;
            }
        }

//        mRectFLittle.set(mDefaultGroupHeight + mHSpace, height + mVSpace,
//                (getPaddingLeft() + mDefaultGroupHeight / 2 + mHSpace / 2 + right / 2) * ratio(0.3f),
//                2 * height + mVSpace);

//        mRectFLittle.set(left, height + mVSpace, left + (right - left) * ratio(0.3f),
//                2 * height + mVSpace);
//        canvas.drawRoundRect(mRectFLittle, mRoundRadius, mRoundRadius, mPaint);
//
//        mRectFLittle.set(left,
//                2 * height + 2 * mVSpace, left + (right - left) * ratio(0.5f),
//                3 * height + 2 * mVSpace);
//        canvas.drawRoundRect(mRectFLittle, mRoundRadius, mRoundRadius, mPaint);
    }

    private float ratio(float min) {
        return Math.min(min * factor + 0.6f * mRandom.nextFloat(), 0.8f);
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setHSpace(int HSpace) {
        mHSpace = HSpace;
    }

    public void setRoundRadius(int roundRadius) {
        mRoundRadius = roundRadius;
    }

    public void setVSpace(int VSpace) {
        mVSpace = VSpace;
    }
}
