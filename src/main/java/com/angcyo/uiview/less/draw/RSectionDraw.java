package com.angcyo.uiview.less.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * 分段绘制
 * <p>
 * 比如: 第一段 是横线, 第二段 是竖线, 第三段是半圆等
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/01/10
 */
public class RSectionDraw extends BaseDraw {

    /**
     * 需要分成几段绘制.
     * 如 {0.2f 0.3f 0.3f 0.1f 0.1f} 总和要为1
     */
    protected float[] sections = new float[]{1};

    /**
     * 总进度, 100表示需要绘制path的全部, 这个值用来触发动画
     */
    protected int progress = 100;

    public RSectionDraw(@NonNull View view) {
        super(view);
    }

    @Override
    public void initAttribute(AttributeSet attr) {

    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.GRAY);

        if (sections != null && sections.length > 0) {
            ensureProgress();

            //总进度
            float totalProgress = progress * 1f / 100f;

            int maxSection = sections.length;

            //绘制前
            onDrawSectionBefore(canvas, maxSection, totalProgress);

            float sum = 0f;
            for (int i = 0; i < maxSection; i++) {
                float section = sections[i];
                //绘制中
                onDrawSection(canvas, maxSection, i, totalProgress, (totalProgress - sum) / section);
                sum += section;
            }

            //绘制后
            onDrawSectionAfter(canvas, maxSection, totalProgress);
        }
    }

    protected void ensureSection() {
        if (sections != null && sections.length > 0) {
            float sum = 0;
            for (float section : sections) {
                sum += section;
            }
            if (sum > 1f) {
                throw new IllegalStateException("Section 总和不能超过1f");
            }
            return;
        }
        throw new IllegalStateException("请设置 Section");
    }

    protected void onDrawSectionBefore(@NonNull Canvas canvas, int maxSection, float totalProgress) {

    }

    /**
     * 重写此方法, 根据section绘制不同内容
     */
    protected void onDrawSection(@NonNull Canvas canvas,
                                 int maxSection, /*path 最多分成了几段, 至少1段*/
                                 int index /*当前绘制的第几段, 0开始*/,
                                 float totalProgress, /*总进度 0-1*/
                                 float progress  /*当前path段的进度 0-1*/
    ) {

    }

    protected void onDrawSectionAfter(@NonNull Canvas canvas, int maxSection, float totalProgress) {

    }

    /**
     * 设置path绘制的进度
     */
    public void setProgress(@IntRange(from = 0, to = 100) int progress) {
        this.progress = progress;
        ensureProgress();
        postInvalidateOnAnimation();
    }

    protected void ensureProgress() {
        if (progress < 0) {
            progress = 0;
        } else if (progress > 100) {
            progress = 100;
        }
    }

    /**
     * 设置分段信息
     */
    public void setSections(float[] sections) {
        this.sections = sections;
        ensureSection();
        postInvalidateOnAnimation();
    }
}
