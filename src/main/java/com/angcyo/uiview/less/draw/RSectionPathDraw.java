package com.angcyo.uiview.less.draw;

import android.content.res.TypedArray;
import android.graphics.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.PathUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.resources.PathUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 将一个图形, 用 多个path合并, 分段 绘制.
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/01/10
 */
public class RSectionPathDraw extends RSectionDraw {

    /**
     * 存放用来获取Path的进度
     */
    protected List<PathMeasure> pathMeasureList;
    /**
     * 原始的path
     */
    protected List<Path> pathList;

    /**
     * 用来绘制的path
     */
    protected List<Path> dstList;

    //sub path 填充距离
    protected int subPaddingVertical;
    protected int subPaddingHorizontal;

    public RSectionPathDraw(@NonNull View view) {
        super(view);
        pathMeasureList = new ArrayList<>();
        dstList = new ArrayList<>();
        setInterpolatorList(new AccelerateInterpolator(), new BounceInterpolator());
    }

    @Override
    public void initAttribute(AttributeSet attr) {
        super.initAttribute(attr);

        TypedArray typedArray = obtainStyledAttributes(attr, R.styleable.RSectionPathDraw);
        int strokeWidth = typedArray.getDimensionPixelOffset(R.styleable.RSectionPathDraw_r_draw_paint_width, (int) (2 * density()));
        subPaddingVertical = typedArray.getDimensionPixelOffset(R.styleable.RSectionPathDraw_r_draw_sub_path_padding_vertical, (int) (2 * density()));
        subPaddingHorizontal = typedArray.getDimensionPixelOffset(R.styleable.RSectionPathDraw_r_draw_sub_path_padding_horizontal, (int) (6 * density()));
        int paintColor = typedArray.getColor(R.styleable.RSectionPathDraw_r_draw_paint_color, getBaseColor());
        typedArray.recycle();

        //只能使用Paint.Style.STROKE 样式, 否则分段绘制Path 不会生效
        mBasePaint.setStyle(Paint.Style.STROKE);
        mBasePaint.setStrokeWidth(strokeWidth);
        mBasePaint.setStrokeCap(Paint.Cap.ROUND);
        mBasePaint.setStrokeJoin(Paint.Join.ROUND);
        mBasePaint.setColor(paintColor);
    }

    /**
     * 调用此方法, 初始化相应的path
     */
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initPath();
    }

    protected void initPath() {
        if (pathList == null) {
            pathList = new ArrayList<>();
            pathList.add(new Path());
            pathList.add(new Path());
        }

        //圆
        Path circlePath = pathList.get(0);
        //勾
        Path tickPath = pathList.get(1);

        circlePath.reset();
        tickPath.reset();

        //圆的半径, 使用View最小边的一半 减去 画笔的宽度
        circlePath.addCircle(minViewSize() / 2, minViewSize() / 2, minViewSize() / 2 - getPaintWidth() / 2, Path.Direction.CW);

        //计算勾, 出现的位置和大小
        float tickWidth = minViewSize() - 2 * getPaintWidth() - subPaddingHorizontal;
        float tickHeight = tickWidth * 2 / 3 - subPaddingVertical;

        int startX = (int) ((minViewSize() - tickWidth) / 2 + 1f / 2 * getPaintWidth());
        int startY = (int) ((minViewSize() - tickHeight) / 2 + 1f / 2 * getPaintWidth());
        PathUtil.addTickPath(tickPath, startX, startY, ((int) (tickWidth - getPaintWidth())), (int) (tickHeight - getPaintWidth()));

        setSelectionPath(pathList, new float[]{0.2f, 0.2f});
    }

    @Override
    protected void onDrawProgressSection(@NonNull Canvas canvas, int index, float startProgress, float endProgress, float totalProgress, float sectionProgress) {
        super.onDrawProgressSection(canvas, index, startProgress, endProgress, totalProgress, sectionProgress);
        float cx = minViewSize() / 4;
        float cy = minViewSize() / 4;
        canvas.drawCircle(cx * (index + 1), cy, 10 * sectionProgress, mBasePaint);
    }

    @Override
    protected void onDrawSectionBefore(@NonNull Canvas canvas, int maxSection, float totalProgress) {
        super.onDrawSectionBefore(canvas, maxSection, totalProgress);
        for (Path p : dstList) {
            p.reset();
        }
        if (isInEditMode()) {
            drawPath(canvas, pathList.get(0));
            drawPath(canvas, pathList.get(1));
        }
    }

    @Override
    protected void onDrawSection(@NonNull Canvas canvas, int maxSection, int index, float totalProgress, float progress) {
        super.onDrawSection(canvas, maxSection, index, totalProgress, progress);
        PathMeasure pathMeasure = pathMeasureList.get(index);
        Path path = dstList.get(index);
        pathMeasure.getSegment(0, progress * pathMeasure.getLength(), path, true);
        drawPath(canvas, path);
    }

    @Override
    protected void onDrawSectionAfter(@NonNull Canvas canvas, int maxSection, float totalProgress) {
        super.onDrawSectionAfter(canvas, maxSection, totalProgress);
    }

    protected void drawPath(@NonNull Canvas canvas, @NonNull Path path) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        canvas.drawPath(path, mBasePaint);
        canvas.restore();
    }

    /**
     * 设置需要绘制的Path段
     */
    public void setSelectionPath(@NonNull Path... paths) {
        setSelectionPath(Arrays.asList(paths), null);
    }

    /**
     * 设置需要绘制的图形path, 和 分段时间占比
     */
    public void setSelectionPath(@NonNull List<Path> pathList, @Nullable float[] selections) {
        this.pathList = pathList;
        if (selections == null) {
            selections = new float[pathList.size()];
            for (int i = 0; i < pathList.size(); i++) {
                selections[i] = 1f / pathList.size();
            }
        }

        if (pathList.size() != selections.length) {
            throw new IllegalStateException("Path 和 selection 数量不匹配.");
        }

        pathMeasureList.clear();
        dstList.clear();
        for (int i = 0; i < pathList.size(); i++) {
            dstList.add(new Path());
            pathMeasureList.add(new PathMeasure(pathList.get(i), false));
        }

        setSections(selections);
    }

    public void setSubPadding(int subPaddingVertical, int subPaddingHorizontal) {
        this.subPaddingVertical = subPaddingVertical;
        this.subPaddingHorizontal = subPaddingHorizontal;
        initPath();
    }

    public void setSubPaddingVertical(int subPaddingVertical) {
        this.subPaddingVertical = subPaddingVertical;
        initPath();
    }

    public void setSubPaddingHorizontal(int subPaddingHorizontal) {
        this.subPaddingHorizontal = subPaddingHorizontal;
        initPath();
    }
}
