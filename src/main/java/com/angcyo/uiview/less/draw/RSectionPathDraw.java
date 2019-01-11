package com.angcyo.uiview.less.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * 本质上是用来draw path的, 但是提供了 分段的功能.
 * <p>
 * 比如: 第一段path是横线, 第二段path是竖线, 第三段是半圆等
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/01/10
 */
public class RSectionPathDraw extends RSectionDraw {
    protected Path path;

    public RSectionPathDraw(@NonNull View view) {
        super(view);
        sections = new float[]{0.15f, 0.15f, 0.15f, 0.15f, 0.15f, 0.25f};
    }

    @Override
    public void initAttribute(AttributeSet attr) {
        super.initAttribute(attr);
        path = new Path();
        //只能使用Paint.Style.STROKE 样式, 否则分段绘制Path 不会生效
        mBasePaint.setStyle(Paint.Style.STROKE);
        mBasePaint.setStrokeWidth(4 * density());
    }

    @Override
    protected void onDrawSectionBefore(@NonNull Canvas canvas, int maxSection, float totalProgress) {
        super.onDrawSectionBefore(canvas, maxSection, totalProgress);
        path.reset();
    }

    @Override
    protected void onDrawSection(@NonNull Canvas canvas, int maxSection, int index, float totalProgress, float progress) {
        super.onDrawSection(canvas, maxSection, index, totalProgress, progress);
        onSetSectionPath(path, maxSection, index, totalProgress, progress);
    }

    @Override
    protected void onDrawSectionAfter(@NonNull Canvas canvas, int maxSection, float totalProgress) {
        super.onDrawSectionAfter(canvas, maxSection, totalProgress);
        canvas.drawPath(path, mBasePaint);
    }

    protected void onSetSectionPath(@NonNull Path path,
                                    int maxSection, /*path 最多分成了几段, 至少1段*/
                                    int index /*当前绘制的第几段, 0开始*/,
                                    float totalProgress, /*总进度 0-1*/
                                    float progress  /*当前path段的进度 0-1*/
    ) {
        //4个边
        if (index == 0) {
            //path.addRect(0f, 0f, 100f, 100f, Path.Direction.CCW);
            path.moveTo(0, getViewHeight());
            path.lineTo(getViewWidth() * progress, getViewHeight());
        }
        if (index == 1) {
            path.moveTo(getViewWidth(), getViewHeight());
            path.lineTo(getViewWidth(), getViewHeight() - (getViewHeight() * progress));
        }
        if (index == 2) {
            path.moveTo(getViewWidth(), 0);
            path.lineTo(getViewWidth() - getViewWidth() * progress, 0);
        }
        if (index == 3) {
            path.moveTo(0, 0);
            path.lineTo(0, getViewHeight() * progress);
        }

        //对勾
        if (index == 4) {
            path.moveTo(0, 0);
            path.lineTo(0, getViewHeight() * progress);
        }
        if (index == 5) {
            path.moveTo(0, 0);
            path.lineTo(0, getViewHeight() * progress);
        }
    }
}
