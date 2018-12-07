package com.angcyo.uiview.less.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.angcyo.uiview.less.draw.RDrawLine;
import com.angcyo.uiview.less.draw.RDrawText;


/**
 * 自绘制的简单文本控件
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/20
 */
public class RDrawTextView extends View {
    protected RDrawText drawText;
    protected RDrawLine drawLine;

    public RDrawTextView(Context context) {
        this(context, null);
    }

    public RDrawTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        drawText = new RDrawText(this, attrs);
        drawLine = new RDrawLine(this, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] measureDraw = drawText.measureDraw(widthMeasureSpec, heightMeasureSpec);
        //super.onMeasure(measureDraw[0], measureDraw[1]);
        setMeasuredDimension(measureDraw[0] + drawText.getPaddingHorizontal(),
                measureDraw[1] + drawText.getPaddingVertical());
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (drawLine.drawLineFront) {
            drawLine.onDraw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!drawLine.drawLineFront) {
            drawLine.onDraw(canvas);
        }
        drawText.onDraw(canvas);
    }

    public RDrawText getDrawText() {
        return drawText;
    }

    public RDrawLine getDrawLine() {
        return drawLine;
    }
}
