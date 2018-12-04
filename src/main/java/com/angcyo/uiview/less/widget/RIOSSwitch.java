package com.angcyo.uiview.less.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import com.angcyo.uiview.less.draw.RDrawIOSSwitch;

/**
 * Email:angcyo@126.com
 * 模仿IOS 开关控件
 *
 * @author angcyo
 * @date 2018/12/04
 */
public class RIOSSwitch extends View implements Checkable {

    /**
     * 选中状态
     */
    private boolean checkStatus = false;

    private RDrawIOSSwitch drawIOSSwitch;

    OnCheckChangeListener onCheckChangeListener;

    public RIOSSwitch(Context context) {
        this(context, null);
    }

    public RIOSSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        drawIOSSwitch = new RDrawIOSSwitch(this);
        drawIOSSwitch.initAttribute(attrs);

        setClickable(true);
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    public void setChecked(boolean checked) {
        if (checkStatus == checked || drawIOSSwitch.isMoving()) {
            return;
        }
        checkStatus = checked;
        Log.i("angcyo", "move.." + drawIOSSwitch.isMoving());
        if (checkStatus) {
            drawIOSSwitch.animationTo(1);
        } else {
            drawIOSSwitch.animationTo(0);
        }
        if (onCheckChangeListener != null) {
            onCheckChangeListener.onCheckChange(this, checked);
        }
    }

    @Override
    public boolean isChecked() {
        return checkStatus;
    }

    @Override
    public void toggle() {
        setChecked(!checkStatus);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int[] measureDraw = drawIOSSwitch.measureDraw(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureDraw[0], measureDraw[1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawColor(Color.GRAY);
        drawIOSSwitch.onDraw(canvas);
    }

    public void setOnCheckChangeListener(OnCheckChangeListener onCheckChangeListener) {
        this.onCheckChangeListener = onCheckChangeListener;
    }

    public interface OnCheckChangeListener {
        void onCheckChange(RIOSSwitch view, boolean isChecked);
    }
}
