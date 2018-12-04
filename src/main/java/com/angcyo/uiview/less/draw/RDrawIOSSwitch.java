package com.angcyo.uiview.less.draw;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v4.math.MathUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import com.angcyo.uiview.less.R;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/04
 */
public class RDrawIOSSwitch extends BaseDraw {

    //默认宽度 dp
    static int DEFAULT_WIDTH = 50;

    //默认高度 dp
    static int DEFAULT_HEIGHT = 25;

    /**
     * 轨道圆角
     */
    private float trackRadius = 450 * density();

    /**
     * 浮子与轨道间隔距离
     */
    private int thumbOffset = (int) (3 * density());

    private int thumbColor = Color.WHITE;

    private int trackOffColor = Color.parseColor("#EBEBEB");
    private int trackOnColor = Color.parseColor("#FFE300");
    private int shadowColor = Color.parseColor("#10000000");
    private int borderColor = Color.parseColor("#e9e9e9");
    private int borderWidth = (int) (2 * density());

    /**
     * 浮子阴影
     */
    private float shadowRadius = 1 * density();

    /*浮子进度*/
    private float thumbDrawProgress = 0f;
    /*轨道进度*/
    private float trackDrawProgress = 0f;

    private ArgbEvaluator argbEvaluator;

    /**
     * 动画轨道矩形控制
     */
    private RectF animThumbRectF = new RectF();
    private RectF borderRectF = new RectF();

    public RDrawIOSSwitch(View view) {
        super(view);
    }

    @Override
    public void initAttribute(AttributeSet attr) {
        argbEvaluator = new ArgbEvaluator();

        TypedArray array = obtainStyledAttributes(attr, R.styleable.RDrawIOSSwitch);
        thumbColor = array.getColor(R.styleable.RDrawIOSSwitch_r_switch_thumb_color, thumbColor);
        trackOffColor = array.getColor(R.styleable.RDrawIOSSwitch_r_switch_track_off_color, trackOffColor);
        trackOnColor = array.getColor(R.styleable.RDrawIOSSwitch_r_switch_track_on_color, trackOnColor);
        shadowColor = array.getColor(R.styleable.RDrawIOSSwitch_r_switch_shadow_color, shadowColor);
        borderColor = array.getColor(R.styleable.RDrawIOSSwitch_r_switch_border_color, borderColor);

        shadowRadius = array.getDimensionPixelOffset(R.styleable.RDrawIOSSwitch_r_switch_shadow_radius, (int) shadowRadius);
        thumbOffset = array.getDimensionPixelOffset(R.styleable.RDrawIOSSwitch_r_switch_thumb_offset, thumbOffset);
        borderWidth = array.getDimensionPixelOffset(R.styleable.RDrawIOSSwitch_r_switch_border_width, borderWidth);

        array.recycle();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //关闭硬件加速, 才有阴影
        mView.setLayerType(View.LAYER_TYPE_SOFTWARE, mBasePaint);

//        if (BuildConfig.DEBUG) {
//            canvas.drawColor(Color.parseColor("#20000000"));
//        }

        /*浮子绘制参数*/
        //浮子绘制进度 0-1f
        float progress = thumbDrawProgress;
        //轨道进度 快一点执行, 视差效果
        float thumbProgress = trackDrawProgress * 2;
        //浮子半径
        float thumbRadius = (mDrawRectF.height() - 2 * thumbOffset) / 2;
        //浮子中心点允许绘制开始的x坐标
        float thumbStartX = mDrawRectF.left + thumbOffset + thumbRadius;
        //结束坐标
        float thumbEndX = mDrawRectF.right - thumbOffset - thumbRadius;

        float thumbDrawX = thumbStartX + (thumbEndX - thumbStartX) * progress;

        /*轨道绘制*/
        mBasePaint.setStrokeWidth(0f);
        mBasePaint.setStyle(Paint.Style.FILL);
        mBasePaint.clearShadowLayer();
        mBasePaint.setColor(trackOnColor);
        canvas.drawRoundRect(mDrawRectF, trackRadius, trackRadius, mBasePaint);

        //需要施展动画的轨道
        mBasePaint.setColor(trackOffColor);
        animThumbRectF.set(mDrawRectF);

        //这段代码可以实现平移动画
        animThumbRectF.left = mDrawRectF.left + mDrawRectF.width() * thumbProgress;

        //左平移动画
        if (animThumbRectF.left >= thumbEndX - thumbRadius) {
            //限制left最大坐标
            animThumbRectF.left = thumbEndX - thumbRadius;

            thumbProgress = thumbProgress / 2;

            //轨道缩小动画
            animThumbRectF.inset((animThumbRectF.height() * thumbProgress) / 2,
                    (mDrawRectF.height() * thumbProgress) / 2);
        }


//        //这段代码可以实现缩放动画
//        //轨道缩小动画
//        animThumbRectF.inset((mDrawRectF.width() * thumbProgress) / 2,
//                (mDrawRectF.height() * thumbProgress) / 2);
//
//        animThumbRectF.offset((mDrawRectF.width() * thumbProgress) / 2, 0f);

        canvas.drawRoundRect(animThumbRectF, trackRadius, trackRadius, mBasePaint);
        /*end*/

        if (progress <= 0.2f) {
            //绘制边框
            mBasePaint.setColor(borderColor);
            mBasePaint.setStrokeWidth(borderWidth);
            mBasePaint.setStyle(Paint.Style.STROKE);
            borderRectF.set(mDrawRectF);
            borderRectF.inset(borderWidth / 2, borderWidth / 2);
            canvas.drawRoundRect(borderRectF, trackRadius, trackRadius, mBasePaint);
        }

        /*浮子绘制*/
        mBasePaint.setStrokeWidth(0f);
        mBasePaint.setStyle(Paint.Style.FILL);
        mBasePaint.setColor(thumbColor);
        mBasePaint.setShadowLayer(shadowRadius, 0, 0, shadowColor);
        canvas.drawCircle(thumbDrawX, mDrawRectF.centerY(), thumbRadius, mBasePaint);
        /*end*/

    }

    @Override
    public int measureDrawWidth(int widthSize, int widthMode) {
        if (isWrapContent(widthMode)) {
            return (int) (DEFAULT_WIDTH * density() + getPaddingHorizontal());
        }
        return widthSize;
    }

    @Override
    public int measureDrawHeight(int heightSize, int heightMode) {
        if (isWrapContent(heightMode)) {
            return (int) (DEFAULT_HEIGHT * density() + getPaddingVertical());
        }
        return heightSize;
    }

    /**
     * 颜色过渡
     */
    private int evaluatorColor(float fraction, int startColor, int endColor) {
        return (int) argbEvaluator.evaluate(fraction, startColor, endColor);
    }

    public boolean isMoving() {
        boolean isRunning = false;
        if (thumbAnimation != null) {
            isRunning = thumbAnimation.isRunning();
        }
        if (trackAnimation != null) {
            isRunning = isRunning || trackAnimation.isRunning();
        }
        return isRunning;
    }

    /**
     * 无动画形式的开关控制
     */
    public void toggle(boolean off) {
        cancelAnim();

        if (off) {
            thumbDrawProgress = 0;
        } else {
            thumbDrawProgress = 1;
        }
        postInvalidate();
    }

    ValueAnimator thumbAnimation;
    ValueAnimator trackAnimation;

    private void cancelAnim() {
        if (thumbAnimation != null) {
            thumbAnimation.cancel();
        }
    }

    /**
     * 用动画的方式, 控制浮子滚动到
     *
     * @param progress 0-1f
     */
    public void animationTo(float progress) {
        progress = MathUtils.clamp(progress, 0f, 1f);
        cancelAnim();

        thumbAnimation = ValueAnimator.ofFloat(0f, 1f);
        trackAnimation = ValueAnimator.ofFloat(0f, 1f);

        final long duration = 500;
        final long delay = 160;

        thumbAnimation.setInterpolator(new LinearInterpolator());
        trackAnimation.setInterpolator(new AccelerateInterpolator());
        thumbAnimation.setDuration(duration);
        trackAnimation.setDuration(duration);

//        //延时任务执行, 视差效果
//        if (progress >= 1f) {
//            //需要打开
//            //thumbAnimation.setStartDelay(delay);
//
//            //thumbAnimation.setDuration(duration + delay);
//
//            trackAnimation.setInterpolator(new DecelerateInterpolator());
//        } else if (progress <= 0f) {
//            //需要关闭
//            //trackAnimation.setStartDelay(delay);
//            //trackAnimation.setDuration(duration + delay);
//
//            trackAnimation.setInterpolator(new AccelerateInterpolator());
//        }

        final float finalProgress = progress;
        thumbAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float interpolatedTime = animation.getAnimatedFraction();
                if (interpolatedTime > 0.9f) {
                    interpolatedTime = 1f;
                }
                thumbDrawProgress = thumbDrawProgress + (finalProgress - thumbDrawProgress) * interpolatedTime;
                postInvalidateOnAnimation();
            }
        });
        thumbAnimation.start();

        trackAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float interpolatedTime = animation.getAnimatedFraction();
                if (interpolatedTime > 0.9f) {
                    interpolatedTime = 1f;
                }
                trackDrawProgress = trackDrawProgress + (finalProgress - trackDrawProgress) * interpolatedTime;
                postInvalidateOnAnimation();
            }
        });
        trackAnimation.start();
    }
}
