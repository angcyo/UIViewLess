package com.angcyo.uiview.less.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.skin.SkinHelper;

import java.util.Arrays;
import java.util.List;

/**
 * 波浪侧边栏
 * author: imilk
 * https://github.com/Solartisan/WaveSideBar.git
 */
public class WaveSideBarView extends View {

    private static final String TAG = "WaveSlideBarView";

    // 计算波浪贝塞尔曲线的角弧长值
    private static final double ANGLE = Math.PI * 45 / 180;
    private static final double ANGLE_R = Math.PI * 90 / 180;
    // 用于过渡效果计算
    ValueAnimator mRatioAnimator;
    private OnTouchLetterChangeListener listener;
    // 渲染字母表
    private List<String> mLetters;
    private List<IGetSideString> mLetters2;
    // 当前选中的位置
    private int mChoose = -1;
    //需要绘制的位置, 只用来判断是否绘制, 不用来判断是否需要回调
    private int mDrawChoose = -1;
    // 字母列表画笔
    private Paint mLettersPaint = new Paint();
    // 提示字母画笔
    private Paint mTextPaint = new Paint();
    // 波浪画笔
    private Paint mWavePaint = new Paint();
    private float mTextSize;
    private float mLargeTextSize;
    private int mTextColor;
    private int mWaveColor;
    private int mTextColorChoose;
    private int mWidth;
    private int mHeight;
    private int mItemHeight;
    private int mPadding;
    // 波浪路径
    private Path mWavePath = new Path();
    // 圆形路径
    private Path mBallPath = new Path();
    // 手指滑动的Y点作为中心点
    private int mCenterY; //中心点Y
    // 贝塞尔曲线的分布半径
    private int mRadius;
    // 圆形半径
    private int mBallRadius;
    // 用于绘制贝塞尔曲线的比率
    private float mRatio;

    // 选中字体的坐标
    private float mPosX, mPosY;

    // 圆形中心点X
    private float mBallCentreX;
    private boolean isTouchDown;

    public WaveSideBarView(Context context) {
        this(context, null);
    }

    public WaveSideBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveSideBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mLetters = Arrays.asList(context.getResources().getStringArray(R.array.waveSideBarLetters));

        mTextColor = Color.parseColor("#969696");
        mWaveColor = SkinHelper.getSkin().getThemeTranColor(0x80); //Color.parseColor("#be69be91");
        mTextColorChoose = SkinHelper.getSkin().getThemeSubColor();//context.getResources().getColor(android.R.color.white);
        mTextSize = context.getResources().getDimensionPixelSize(R.dimen.textSize_sidebar);
        mLargeTextSize = context.getResources().getDimensionPixelSize(R.dimen.large_textSize_sidebar);
        mPadding = context.getResources().getDimensionPixelSize(R.dimen.textSize_sidebar_choose);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WaveSideBarView);
            mTextColor = a.getColor(R.styleable.WaveSideBarView_sidebarTextColor, mTextColor);
            mTextColorChoose = a.getColor(R.styleable.WaveSideBarView_sidebarChooseTextColor, mTextColorChoose);
            mTextSize = a.getFloat(R.styleable.WaveSideBarView_sidebarTextSize, mTextSize);
            mLargeTextSize = a.getFloat(R.styleable.WaveSideBarView_sidebarLargeTextSize, mLargeTextSize);
            mWaveColor = a.getColor(R.styleable.WaveSideBarView_sidebarBackgroundColor, mWaveColor);
            mRadius = a.getInt(R.styleable.WaveSideBarView_sidebarRadius, context.getResources().getDimensionPixelSize(R.dimen.radius_sidebar));
            mBallRadius = a.getInt(R.styleable.WaveSideBarView_sidebarBallRadius, context.getResources().getDimensionPixelSize(R.dimen.ball_radius_sidebar));
            a.recycle();
        }

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(mWaveColor);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColorChoose);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mLargeTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private int getLetterSize() {
        if (mLetters2 != null) {
            return mLetters2.size();
        }
        return mLetters.size();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float y = event.getY();
        final float x = event.getX();

        final int oldChoose = mChoose;

        int padding = mHeight - getLetterSize() * mItemHeight;
        float dy = y - padding / 2;

//        if (dy < 0) {
//            startAnimator(mRatio, 0f);
//            return true;
//        }
//
//        if (dy > getLetterSize() * mItemHeight) {
//            startAnimator(mRatio, 0f);
//            return true;
//        }

        int newChoose = (int) (dy / mItemHeight);

        if (newChoose < 0) {
            newChoose = 0;
        } else if (newChoose > getLetterSize() - 1) {
            newChoose = getLetterSize() - 1;
        }

        mDrawChoose = newChoose;
        mCenterY = (int) y;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (x < mWidth - 2 * mRadius) {
                    return false;
                }
                isTouchDown = true;

                startAnimator(mRatio, 1.0f);
                break;
            case MotionEvent.ACTION_MOVE:
                if (oldChoose != newChoose) {
                    if (newChoose >= 0 && newChoose < getLetterSize()) {
                        mChoose = newChoose;
                        if (listener != null) {
                            if (mLetters2 == null) {
                                listener.onLetterChange(null, mLetters.get(newChoose));
                            } else {
                                listener.onLetterChange(mLetters2.get(newChoose), mLetters.get(newChoose));
                            }
                        }
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isTouchDown = false;
                startAnimator(mRatio, 0f);
                mChoose = -1;
                mDrawChoose = -1;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mItemHeight = (int) mTextSize + mPadding;
        if (mItemHeight * getLetterSize() > mHeight) {
            mItemHeight = (mHeight - mPadding) / getLetterSize() - 5;// 更改item 大小 ;
        }
        mPosX = mWidth - 1.6f * mTextSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制字母列表
        drawLetters(canvas);

        //绘制波浪
        drawWavePath(canvas);

        //绘制圆
        drawBallPath(canvas);

        //绘制选中的字体
        drawChooseText(canvas);

    }

    private void drawLetters(Canvas canvas) {

        if (isTouchDown) {
            RectF rectF = new RectF();
            rectF.left = mPosX - mTextSize;
            rectF.right = mPosX + mTextSize;
            rectF.top = mTextSize / 2;
            rectF.bottom = mHeight - mTextSize / 2;

            mLettersPaint.reset();
            mLettersPaint.setStyle(Paint.Style.FILL);
            mLettersPaint.setColor(Color.parseColor("#F9F9F9"));
            mLettersPaint.setAntiAlias(true);
            canvas.drawRoundRect(rectF, mTextSize, mTextSize, mLettersPaint);

            mLettersPaint.reset();
            mLettersPaint.setStyle(Paint.Style.STROKE);
            mLettersPaint.setColor(mTextColor);
            mLettersPaint.setAntiAlias(true);
            canvas.drawRoundRect(rectF, mTextSize, mTextSize, mLettersPaint);
        }

        for (int i = 0; i < getLetterSize(); i++) {

            mLettersPaint.reset();
            mLettersPaint.setColor(mTextColor);
            mLettersPaint.setAntiAlias(true);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);

            Paint.FontMetrics fontMetrics = mLettersPaint.getFontMetrics();
            float baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top);

//            float posY = mItemHeight * i + baseline / 2 + mPadding;

            float centerY = mHeight / 2;

            int centerPos = getLetterSize() / 2;

            float posY = mItemHeight * (i - centerPos) + centerY;


            if (i == mDrawChoose) {
                mPosY = posY;
            } else {
                canvas.drawText(getLetterString(i), mPosX, posY, mLettersPaint);
            }
        }
    }

    private String getLetterString(int index) {
        if (mLetters2 != null) {
            return mLetters2.get(index).getSideString();
        }
        return mLetters.get(index);
    }

    private void drawChooseText(Canvas canvas) {
        //L.e("call: drawChooseText([canvas])-> " + mChoose + " " + mPosX + " " + mPosY);
        if (mDrawChoose != -1) {
            // 绘制右侧选中字符
            mLettersPaint.reset();
            mLettersPaint.setColor(mTextColorChoose);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(getLetterString(mDrawChoose), mPosX, mPosY, mLettersPaint);

            // 绘制提示字符
            if (mRatio >= 0.9f) {
                String target = getLetterString(mDrawChoose);
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                float baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top);
                float x = mBallCentreX;
                float y = mCenterY + baseline / 2;
                canvas.drawText(target, x, y, mTextPaint);
            }
        }
    }

    /**
     * 绘制波浪
     *
     * @param canvas
     */
    private void drawWavePath(Canvas canvas) {
        mWavePath.reset();
        // 移动到起始点
        mWavePath.moveTo(mWidth, mCenterY - 3 * mRadius);
        //计算上部控制点的Y轴位置
        int controlTopY = mCenterY - 2 * mRadius;

        //计算上部结束点的坐标
        int endTopX = (int) (mWidth - mRadius * Math.cos(ANGLE) * mRatio);
        int endTopY = (int) (controlTopY + mRadius * Math.sin(ANGLE));
        mWavePath.quadTo(mWidth, controlTopY, endTopX, endTopY);

        //计算中心控制点的坐标
        int controlCenterX = (int) (mWidth - 1.8f * mRadius * Math.sin(ANGLE_R) * mRatio);
        int controlCenterY = mCenterY;
        //计算下部结束点的坐标
        int controlBottomY = mCenterY + 2 * mRadius;
        int endBottomX = endTopX;
        int endBottomY = (int) (controlBottomY - mRadius * Math.cos(ANGLE));
        mWavePath.quadTo(controlCenterX, controlCenterY, endBottomX, endBottomY);

        mWavePath.quadTo(mWidth, controlBottomY, mWidth, controlBottomY + mRadius);

        mWavePath.close();
        canvas.drawPath(mWavePath, mWavePaint);
    }

    private void drawBallPath(Canvas canvas) {
        //x轴的移动路径
        mBallCentreX = (mWidth + mBallRadius) - (2.0f * mRadius + 2.0f * mBallRadius) * mRatio;

        mBallPath.reset();
        mBallPath.addCircle(mBallCentreX, mCenterY, mBallRadius, Path.Direction.CW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mBallPath.op(mWavePath, Path.Op.DIFFERENCE);
        }

        mBallPath.close();
        canvas.drawPath(mBallPath, mWavePaint);

    }


    private void startAnimator(float... value) {
        if (mRatioAnimator == null) {
            mRatioAnimator = new ValueAnimator();
        }
        mRatioAnimator.cancel();
        mRatioAnimator.setFloatValues(value);
        mRatioAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                mRatio = (float) value.getAnimatedValue();
                invalidate();
            }
        });
        mRatioAnimator.start();
    }

    public List<String> getLetters() {
        return mLetters;
    }

    /**
     * 设置字母集合
     */
    public void setLetters(List<String> letters) {
        this.mLetters = letters;
        invalidate();
    }

    public List<IGetSideString> getLetters2() {
        return mLetters2;
    }

    /**
     * 支持对象数据列表
     */
    public void setLetters2(List<IGetSideString> letters2) {
        mLetters2 = letters2;
        invalidate();
    }

    /**
     * 字母事件监听
     */
    public void setOnTouchLetterChangeListener(OnTouchLetterChangeListener listener) {
        this.listener = listener;
    }

    public interface OnTouchLetterChangeListener {
        void onLetterChange(@Nullable IGetSideString data, String letter);
    }
}
