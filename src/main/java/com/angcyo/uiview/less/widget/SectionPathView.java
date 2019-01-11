package com.angcyo.uiview.less.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.angcyo.uiview.less.draw.RSectionPathDraw;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/01/10
 */
public class SectionPathView extends View {

    RSectionPathDraw sectionPathDraw;

    public SectionPathView(Context context) {
        this(context, null);
    }

    public SectionPathView(Context context, AttributeSet attrs) {
        super(context, attrs);

        sectionPathDraw = new RSectionPathDraw(this);
        sectionPathDraw.initAttribute(attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        sectionPathDraw.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //animation.getAnimatedValue();
                sectionPathDraw.setProgress((int) (animation.getAnimatedFraction() * 100));
            }
        });
        valueAnimator.start();
    }
}