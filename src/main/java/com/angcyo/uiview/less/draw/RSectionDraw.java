package com.angcyo.uiview.less.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.Arrays;
import java.util.List;

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
     * 每一段的差值器, 可以和sections数量不一致, 有的就取, 没有就默认
     */
    protected List<Interpolator> interpolatorList;

    /**
     * 总进度, 100表示需要绘制path的全部, 这个值用来触发动画
     */
    protected int progress = 100;

    /**
     * 所有sections加起来的总和
     */
    private float sumSectionProgress = 1f;

    public RSectionDraw(@NonNull View view) {
        super(view);
    }

    @Override
    public void initAttribute(AttributeSet attr) {

    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (sections != null && sections.length > 0) {
            ensureProgress();

            //总进度
            float totalProgress = progress * 1f / 100f;

            int maxSection = sections.length;

            //绘制前
            onDrawSectionBefore(canvas, maxSection, totalProgress);

            float sum = 0f;
            for (int i = 0; i < maxSection; i++) {

                float sectionProgress = -1;
                float section = sections[i];

                if (totalProgress <= sum + section) {
                    //绘制中
                    sectionProgress = (totalProgress - sum) / section;
                }

                //差值器
                if (interpolatorList != null && interpolatorList.size() > i) {
                    sectionProgress = interpolatorList.get(i).getInterpolation(sectionProgress);
                }

                if (totalProgress >= sum && totalProgress <= sum + section) {
                    onDrawProgressSection(canvas, i, sum, sum + section, totalProgress, sectionProgress);
                }

                /*小于总进度的 section 都会执行绘制*/
                if (totalProgress >= sum) {

                    if (totalProgress > sum + section) {
                        //当section的总和小于1时, 最后一个是section会多执行剩余的进度
                        sectionProgress = 1f;
                    }

                    onDrawSection(canvas, maxSection, i, totalProgress, sectionProgress);
                }

                sum += section;
            }

            //绘制后
            onDrawSectionAfter(canvas, maxSection, totalProgress);
        }
    }

    protected void ensureSection() {
        if (sections != null && sections.length > 0) {
            sumSectionProgress = 0;
            for (float section : sections) {
                sumSectionProgress += section;
            }
            if (sumSectionProgress > 1f) {
                throw new IllegalStateException("Section 总和不能超过1f");
            }
            return;
        }
        throw new IllegalStateException("请设置 Section");
    }

    /**
     * 当前进度在对应的section中, 只执行当前section的绘制
     */
    protected void onDrawProgressSection(@NonNull Canvas canvas,
                                         int index /*当前绘制的第几段, 0开始*/,
                                         float startProgress /*当前section开始的进度值*/,
                                         float endProgress /*当前section结束的进度值*/,
                                         float totalProgress /*总进度*/,
                                         float sectionProgress /*section中的进度*/) {

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

    public void setInterpolatorList(Interpolator... interpolators) {
        setInterpolatorList(Arrays.asList(interpolators));
    }

    public void setInterpolatorList(List<Interpolator> interpolatorList) {
        this.interpolatorList = interpolatorList;
    }
}
