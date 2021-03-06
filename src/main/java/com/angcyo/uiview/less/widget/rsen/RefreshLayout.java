package com.angcyo.uiview.less.widget.rsen;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.TextView;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.kotlin.ExKt;
import com.angcyo.uiview.less.kotlin.ViewGroupExKt;
import com.angcyo.uiview.less.skin.SkinHelper;
import com.angcyo.uiview.less.utils.ScreenUtil;
import com.angcyo.uiview.less.utils.T_;
import com.angcyo.uiview.less.utils.UI;
import com.angcyo.uiview.less.widget.RTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：刷新控件
 * 创建人员：Robi
 * 创建时间：2016/12/05 17:30
 * 修改人员：Robi
 * 修改时间：2016/12/05 17:30
 * 修改备注：
 * Version: 1.0.0
 */
public class RefreshLayout extends ViewGroup {
    /**
     * 不支持刷新和上拉
     */
    public static final int NONE = -1;
    /**
     * 支持刷新,或者刷新中
     */
    public static final int TOP = 1;
    /**
     * 支持上拉,或者上拉中
     */
    public static final int BOTTOM = 2;
    /**
     * 支持刷新和上拉
     */
    public static final int BOTH = 3;
    /**
     * 刷新,上拉控件 正在移动中
     */
    public static final int MOVE = 4;
    /**
     * 刷新,上拉 完成, 但是手指还在拖动
     */
    public static final int FINISH = 5;
    /**
     * 当前状态, 显示菜单模式
     */
    public static final int MENU_LAYOUT = 6;
    /**
     * 正常
     */
    public static final int NORMAL = 0;
    public static final String TOP_VIEW = "top_view";
    public static final String BOTTOM_VIEW = "bottom_view";
    public static final String TARGET_VIEW = "target_view";
    public static final String TIP_VIEW = "tip_view";
    public static final long ANIM_TIME = 300;
    protected View mTopView, mBottomView, mTargetView, mTipView;
    float downRawY, downRawX, downY, downX, lastY;
    boolean checkInnerChildScroll = false;
    private OverScroller mScroller;
    private int mTouchSlop;
    /**
     * 支持的滚动方向
     */
    @Direction
    private int mDirection = BOTH;
    /**
     * 当前刷新的状态
     */
    @State
    private int mCurState = NORMAL;
    /**
     * 手指未离屏
     */
    private boolean isTouchDown = false;
    /**
     * 刷新的意向, 比如刷新的时候抓起了View, 那么不允许上拉加载
     */
    private int order = NONE;
    /**
     * 按下的时候, 已经滚动的距离.
     */
    private int mDownScrollY = 0;
    /**
     * 是否激活延迟加载, 防止刷新太快,就结束了.
     */
    private boolean delayLoadEnd = true;
    /**
     * 是否需要通知事件, 如果为false, 那么只有滑动效果, 没有事件监听
     */
    //private boolean mNotifyListener = true;
    private long refreshTime = 0;
    /**
     * 设置通知事件的方向, 其他方向不通知监听.
     */
    private int mNotifyListener = BOTH;
    private boolean mShowTip = false;
    Runnable hideTipRunnable = new Runnable() {
        @Override
        public void run() {
            mShowTip = false;

            mTipView.clearAnimation();
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 1, 0,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
            scaleAnimation.setDuration(ANIM_TIME);
            scaleAnimation.setFillAfter(true);
            mTipView.startAnimation(scaleAnimation);
        }
    };
    private ArrayList<OnTopViewMoveListener> mTopViewMoveListeners = new ArrayList<>();
    private ArrayList<OnBottomViewMoveListener> mBottomViewMoveListeners = new ArrayList<>();
    private ArrayList<OnRefreshListener> mRefreshListeners = new ArrayList<>();

    /**
     * 模仿微信小程序菜单, 用来包裹菜单的布局, 当菜单显示了1/5, 表示需要开启菜单显示
     */
    private FrameLayout menuLayout;

    /*当滚动Y值>=topView高度+menuOpenThreshold时, 开始出现menuLayout*/
    private int menuOpenThreshold = (int) (20 * ScreenUtil.density());
    private int lastTranslationTo = 0;
    private int startScrollMenuY = 0;//需要从这个值,开始滚动
    private int startScrollToMenuY = 0;//滚动到目标值, 用来计算比率
    private int startScrollMenuHeight = 0;//菜单已经滚动了多少值
    /*滚动到了,需要显示菜单的条件*/
    private boolean scrollNeedShowMenuLayout = false;
    /*进入菜单模式*/
    private boolean scrollShowMenuLayout = false;

    public RefreshLayout(Context context) {
        this(context, null);
    }


    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initRefreshView();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout);
        setNotifyListener(typedArray.getBoolean(R.styleable.RefreshLayout_r_notify_listener, true));
        mDirection = typedArray.getInteger(R.styleable.RefreshLayout_r_refresh_direction, mDirection);

        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {

            if (isInEditMode()) {
                if (mTargetView == null) {
                    setMeasuredDimension(widthSize, heightSize);
                } else {
                    mTargetView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                    setMeasuredDimension(mTargetView.getMeasuredWidth(), mTargetView.getMeasuredHeight());
                }
                return;
            }

            if (mTargetView != null) {
                mTargetView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                setMeasuredDimension(mTargetView.getMeasuredWidth(), mTargetView.getMeasuredHeight());
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

        } else {
            if (isInEditMode()) {
                if (mTargetView == null) {
                    setMeasuredDimension(widthSize, heightSize);
                } else {
                    mTargetView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(heightSize, heightMode));
                    setMeasuredDimension(mTargetView.getMeasuredWidth(), mTargetView.getMeasuredHeight());
                }
                return;
            }

            if (mTargetView != null) {
                mTargetView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(heightSize, heightMode));
                setMeasuredDimension(mTargetView.getMeasuredWidth(), mTargetView.getMeasuredHeight());
            } else {
                setMeasuredDimension(widthSize, heightSize);
            }
        }

        if (mTopView != null) {
            mTopView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
        }
        if (mBottomView != null) {
            mBottomView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
        }
        if (mTipView != null) {
            mTipView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
        }
        if (menuLayout != null) {
            measureChild(menuLayout, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (isInEditMode()) {
            mTargetView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
            return;
        }

        if (mTargetView != null) {
            mTargetView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }
        if (mTopView != null) {
            //自动居中布局
            mTopView.layout((r - l) / 2 - mTopView.getMeasuredWidth() / 2, -mTopView.getMeasuredHeight(),
                    (r - l) / 2 + mTopView.getMeasuredWidth() / 2, 0);
        }
        if (mBottomView != null) {
            //自动居中布局
            mBottomView.layout((r - l) / 2 - mBottomView.getMeasuredWidth() / 2, getMeasuredHeight(),
                    (r - l) / 2 + mBottomView.getMeasuredWidth() / 2, getMeasuredHeight() + mBottomView.getMeasuredHeight());
        }

        layoutMenuView(lastTranslationTo);

        layoutTipView();
    }

    private void layoutTipView() {
        if (!mShowTip) {
            return;
        }
        if (mTipView != null) {
            mTipView.bringToFront();
            int top = getScrollY();
            mTipView.layout(0, top, getMeasuredWidth(), top + mTipView.getMeasuredHeight());
        }
    }

    /*菜单底部开始, 不要露出的高度*/
    private void layoutMenuView(int translationTo /*当菜单显示到Y坐标, 不受scroll影响的坐标*/) {
        if (menuLayout != null) {
            if (scrollNeedShowMenuLayout) {
                lastTranslationTo = translationTo;
                int top = getScrollY() + Math.min(translationTo, menuLayout.getMeasuredHeight());
                menuLayout.layout(0, top - menuLayout.getMeasuredHeight(), menuLayout.getMeasuredWidth(), top);
            } else {
                lastTranslationTo = 0;
                menuLayout.layout(0, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mCurState == TOP) {
            scrollToTop(false);
        } else if (mCurState == BOTTOM) {
            scrollToBottom(false);
        } else if (mCurState == MENU_LAYOUT) {
            scrollToMenu(false);
        } else {
            resetScroll(false);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTouchSlop = 0;//ViewConfiguration.get(getContext()).getScaledTouchSlop();

        if (!isInEditMode()) {
            addViewSafe(mTopView);
            addViewSafe(mBottomView);
            addViewSafe(mTipView);
        }
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        for (int i = 0; i < getChildCount() && mTargetView == null; i++) {
            final View childAt = getChildAt(i);

            if (childAt instanceof OnBottomViewMoveListener &&
                    childAt instanceof OnTopViewMoveListener) {
                continue;
            }

            if (childAt instanceof OnBottomViewMoveListener) {
                mBottomView = childAt;
            } else if (childAt instanceof OnTopViewMoveListener) {
                mTopView = childAt;
            } else if (childAt == mTipView) {
            } else {
                mTargetView = childAt;
            }
        }
    }

    protected void initRefreshView() {
        mScroller = new OverScroller(getContext(), new DecelerateInterpolator());

        if (isInEditMode()) {
            return;
        }
        if (mTopView == null) {
            mTopView = new BasePointRefreshView(getContext());
            mTopView.setTag(TOP_VIEW);
        }
        if (mBottomView == null) {
            mBottomView = new BasePointRefreshView(getContext());
            mBottomView.setTag(TOP_VIEW);
        }
        if (mTipView == null) {
            RTextView textView = new RTextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.WHITE);
            textView.setRBackgroundColor(SkinHelper.getSkin().getThemeSubColor());
            textView.setBackgroundResource(R.drawable.base_bg_selector);
            textView.setText("测试专用...");
            textView.setTag(TIP_VIEW);
            int left = getResources().getDimensionPixelOffset(R.dimen.base_xhdpi);
            int top = getResources().getDimensionPixelOffset(R.dimen.base_ldpi);
            textView.setPadding(left, top, left, top);
            mTipView = textView;

            mTipView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    T_.show("Test");
                }
            });
        }
    }

    public void setTopView(View topView) {
        resetView(mTopView);
        mTopView = topView;
        mTopView.setTag(TOP_VIEW);
        addViewSafe(mTopView);
    }

    public void setBottomView(View bottomView) {
        resetView(mBottomView);
        mBottomView = bottomView;
        mBottomView.setTag(TOP_VIEW);
        addViewSafe(mBottomView);
    }

    private void resetView(View view) {
        if (view != null && view.getParent() != null) {
            if (view.getParent() instanceof ViewGroup) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        }
    }

    private void addViewSafe(View view) {
        if (view != null && view.getParent() == null) {
            addView(view);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View topView = findViewWithTag(TOP_VIEW);
        if (topView != null) {
            setTopView(topView);
        }

        View bottomView = findViewWithTag(BOTTOM_VIEW);
        if (bottomView != null) {
            setBottomView(bottomView);
        }

        View targetView = findViewWithTag(TARGET_VIEW);
        if (targetView != null) {
            mTargetView = targetView;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            if (currY == 0 && mCurState == FINISH) {
                mCurState = NORMAL;
            }
            scrollTo(mScroller.getCurrX(), currY);
            postInvalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //int actionMasked = ev.getActionMasked();
        if (ExKt.isFinish(ev)) {
            if (scrollShowMenuLayout && getScrollY() == 0) {
                resetMenuLayout(false);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.onInterceptTouchEvent(event);
        }
        int action = MotionEventCompat.getActionMasked(event);//event.getActionMasked();
        //L.e("call: onInterceptTouchEvent([event])-> " + action);

        if (action == MotionEvent.ACTION_DOWN) {
            mDownScrollY = getScrollY();
            handleTouchDown(event);
        } else if (action == MotionEvent.ACTION_MOVE) {
            float y = event.getY();
            float x = event.getX();
            float dy = y - downY;
            float dx = x - downX;

            boolean touchIntercept = Math.abs(downRawY - y) > 10;

            if ((Math.abs(dy) > Math.abs(dx)) && Math.abs(dy) > mTouchSlop) {
                int scrollY = getScrollY();
                if (mCurState == MENU_LAYOUT && dy < 0 && scrollY < 0) {
                    scrollTo(0, (int) Math.min(0, (scrollY - dy)));
                    downY = event.getY();
                    downX = event.getX();
                    return super.onInterceptTouchEvent(event);
                } else if (mCurState == TOP && dy < 0 && scrollY < 0) {
                    //如果已经处理加载状态, 通过滚动, View 隐藏, 使得内容全屏显示
                    scrollTo(0, (int) Math.min(0, (scrollY - dy)));
                    downY = event.getY();
                    downX = event.getX();
                    //L.e("call: onInterceptTouchEvent([event])-> 1");
                    return super.onInterceptTouchEvent(event);
                } else if (mCurState == BOTTOM && dy > 0 && scrollY > 0) {
                    scrollTo(0, (int) Math.max(0, scrollY - dy));
                    downY = event.getY();
                    downX = event.getX();
                    //L.e("call: onInterceptTouchEvent([event])-> 2");
                    return super.onInterceptTouchEvent(event);
                } else {
                    if (dy > 0 && canScrollDown() &&
                            !innerCanChildScrollVertically(mTargetView, -1,
                                    event.getRawX(), event.getRawY(),
                                    event.getX(), event.getY())) {
                        order = TOP;
                        //L.e("call: onInterceptTouchEvent([event])-> 3");
                        return super.onInterceptTouchEvent(event) || touchIntercept;

                    } else if (dy < 0 && canScrollUp() &&
                            !innerCanChildScrollVertically(mTargetView, 1,
                                    event.getRawX(), event.getRawY(),
                                    event.getX(), event.getY())) {
                        order = BOTTOM;
                        //L.e("call: onInterceptTouchEvent([event])-> 4");
                        //return true;//this 星期六 2017-9-30
                        return super.onInterceptTouchEvent(event) || touchIntercept;

                    } else {
                        if (getScrollY() > 0 && dy > 0) {
                            //return true;//this 星期六 2017-9-30
                            return super.onInterceptTouchEvent(event) || touchIntercept;

                        }
                        if (getScrollY() < 0 && dy < 0) {
                            // return true;//this 星期六 2017-9-30
                            return super.onInterceptTouchEvent(event) || touchIntercept;
                        }
                    }
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            int count = event.getPointerCount();
            if (count == 1) {
                isTouchDown = false;
            }

            boolean interceptTouchEvent = super.onInterceptTouchEvent(event);
            if (!interceptTouchEvent) {
                handleTouchUp(event, true);
                //L.e("call: onInterceptTouchEvent([event])-> 5 " + interceptTouchEvent);
                return interceptTouchEvent;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        //L.e("call: onTouchEvent([event])-> " + action + " " + mDownScrollY);

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            int count = event.getPointerCount();
            if (count == 1) {
                isTouchDown = false;
            }
            handleTouchUp(event, false);
        } else if (action == MotionEvent.ACTION_MOVE) {
            float y = event.getY();
            float dy = lastY - y;
            isTouchDown = true;

            if (order == NONE) {
                if (dy > 0) {
                    order = BOTTOM;
                } else {
                    order = TOP;
                }
            }

            if (Math.abs(dy) > mTouchSlop) {
                int scrollY = getScrollY();
                int needScrollerY = (int) (dy * (1 - 0.4 - Math.abs(scrollY) * 1.f / getMeasuredHeight()));
                if (mDownScrollY != 0) {
                    if (mDownScrollY < 0 && (scrollY + needScrollerY) > 0) {
                        needScrollerY = -scrollY;
                    } else if (mDownScrollY > 0 && (scrollY + needScrollerY) < 0) {
                        needScrollerY = -scrollY;
                    }
                }

                scrollBy(0, needScrollerY);
                lastY = y;
                if (mCurState == NORMAL /*|| mCurState == FINISH*/) {
                    mCurState = MOVE;
                }
            }
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            //多个手指按下
            lastY = event.getY();
        } else if (action == MotionEvent.ACTION_DOWN) {
            handleTouchDown(event);
        }
        return true;
    }

    private void handleTouchDown(MotionEvent downEvent) {
        downY = downEvent.getY();
        downX = downEvent.getX();
        downRawX = downX;
        downRawY = downY;

        lastY = downY;
        mScroller.abortAnimation();
    }

    /**
     * 释放手指之后的处理
     */
    private void handleTouchUp(MotionEvent upEvent, boolean fromIntercept) {
        int scrollY = getScrollY();
        int absScrollY = Math.abs(scrollY);

        int topHeight = mTopView.getMeasuredHeight();
        int bottomHeight = mBottomView.getMeasuredHeight();

        if (order == NONE) {
            if (scrollY != 0) {
                if (mCurState == MENU_LAYOUT) {
                    if (fromIntercept && ExKt.isClickEvent(upEvent, getContext(), downRawX, downRawY)) {

                    } else {
                        resetScroll();
                    }
                } else if (mCurState == FINISH || mCurState == NORMAL) {
                    resetScroll();
                } else if (mCurState == TOP && scrollY > -topHeight) {
                    resetScroll();
                } else if (mCurState == BOTTOM && scrollY < bottomHeight) {
                    resetScroll();
                }
            }
            return;
        }

        order = NONE;

//        if (!mNotifyListener) {
//            resetScroll();
//            return;
//        }

        if (scrollY < 0) {

            if (isNeedToMenuLayout()) {
                scrollShowMenuLayout = true;
                scrollToMenu(true);
                return;
            }

            //处理刷新
            if (mTopView == null || mCurState == FINISH) {
                resetScroll();
                return;
            }

            if (absScrollY >= topHeight) {
                if (mNotifyListener == TOP || mNotifyListener == BOTH) {
                    refreshTop();
                } else {
                    resetScroll();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (OnRefreshListener listener : mRefreshListeners) {
                                if (listener instanceof SimpleRefreshListener) {
                                    ((SimpleRefreshListener) listener).onNoNotifyRefresh(TOP);
                                }
                            }
                        }
                    }, 100);
                }
            } else {
                resetScroll();
            }
        } else if (scrollY > 0) {
            //处理加载
            if (mBottomView == null || mCurState == FINISH) {
                resetScroll();
                return;
            }

            if (absScrollY >= bottomHeight) {
                if (mNotifyListener == BOTTOM || mNotifyListener == BOTH) {
                    refreshBottom();
                } else {
                    resetScroll();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (OnRefreshListener listener : mRefreshListeners) {
                                if (listener instanceof SimpleRefreshListener) {
                                    ((SimpleRefreshListener) listener).onNoNotifyRefresh(BOTTOM);
                                }
                            }
                        }
                    }, 100);
                }
            } else {
                resetScroll();
            }
        }

    }

    /**
     * 设置支持刷新的方向
     */
    public void setRefreshDirection(@Direction int direction) {
        mDirection = direction;
    }

    /**
     * 开始刷新
     */
    public void startRefresh() {
        setRefreshState(TOP);
    }

    /**
     * 开始加载更多
     */
    public void startLoadMore() {
        setRefreshState(BOTTOM);
    }

    /**
     * 触发刷新状态
     */
    public void setRefreshState(@State int state) {
        if (mCurState == MOVE) {
            resetScroll(false);
        }
        if (mCurState == NORMAL || mCurState == FINISH) {
            if (state == TOP) {
                if (mDirection == TOP || mDirection == BOTH) {
                    refreshTop();
                }
            } else if (state == BOTTOM) {
                if (mDirection == BOTTOM || mDirection == BOTH) {
                    refreshBottom();
                }
            }
        } else {
            if (mCurState == TOP && TOP == state) {
                refreshTop();
            }
            if (mCurState == BOTTOM && BOTTOM == state) {
                refreshBottom();
            }
        }
    }

    /**
     * 结束刷新
     */
    public void setRefreshEnd() {
        /**如果激活了延迟加载, ...*/
        if (delayLoadEnd && System.currentTimeMillis() - refreshTime < 600) {
            post(new Runnable() {
                @Override
                public void run() {
                    setRefreshEnd();
                }
            });
            return;
        }

//        if (mCurState == FINISH || mCurState == NORMAL /*|| mCurState == MOVE*/) {
//            return;
//        }
//
//        if (mCurState == MOVE && isTouchDown) {
//            return;
//        }

        mCurState = FINISH;
        if (isTouchDown) {
            //scrollTo(getScrollX(), getScrollY());
        } else {
            startScroll(0);
        }
    }

    public void setRefreshEnd(boolean anim) {
        if (anim) {
            setRefreshEnd();
        } else {
            mCurState = FINISH;
            if (!isTouchDown) {
                scrollTo(0, 0);
            }
        }
    }

    private void scrollToMenu(boolean anim) {
        if (menuLayout != null) {
            mCurState = MENU_LAYOUT;

            int menuLayoutHeight = getMenuLayoutHeight();

            startScrollMenuY = Math.abs(getScrollY());
            startScrollToMenuY = menuLayoutHeight;
            startScrollMenuHeight = lastTranslationTo;

            if (anim) {
                startScroll(-menuLayoutHeight);
            } else {
                scrollTo(0, -menuLayoutHeight);
            }
        }
    }

    private void scrollToTop(final boolean anim) {
        if (mTopView != null) {
            int height = mTopView.getMeasuredHeight();
            if (height == 0) {
                this.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        RefreshLayout.this.removeOnLayoutChangeListener(this);
                        scrollToTop(anim);
                    }
                });
            } else {
                if (anim) {
                    startScroll(-height);
                } else {
                    scrollTo(0, -height);
                }
            }
        }
    }

    private void scrollToBottom(boolean anim) {
        if (mBottomView != null) {
            if (anim) {
                startScroll(mBottomView.getMeasuredHeight());
            } else {
                scrollTo(0, mBottomView.getMeasuredHeight());
            }
        }
    }

    private void refreshTop() {
        refreshTime = System.currentTimeMillis();
        if (mTopView != null) {
            //设置正在刷新
            mCurState = TOP;

            scrollToTop(true);

            //防止还没回到刷新位置, 就已经调用了刷新结束的方法
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (OnRefreshListener listener : mRefreshListeners) {
                        listener.onRefresh(TOP);
                    }
                }
            }, 100);
        }
    }

    private void refreshBottom() {
        refreshTime = System.currentTimeMillis();
        if (mBottomView != null) {
            //设置正在上拉
            mCurState = BOTTOM;

            scrollToBottom(true);

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (OnRefreshListener listener : mRefreshListeners) {
                        listener.onRefresh(BOTTOM);
                    }
                }
            }, 100);

        }
    }

    /**
     * 恢复到默认的滚动状态
     */
    private void resetScroll() {
        resetScroll(true);
    }

    private void resetScroll(boolean anim) {
        mScroller.abortAnimation();
        if (mCurState != TOP && mCurState != BOTTOM /*&& mCurState != FINISH*/) {
            mCurState = NORMAL;
        }

        resetMenuLayout(anim);

        if (anim) {
            startScroll(0);
        } else {
            scrollTo(0, 0);
        }
    }

    /*将菜单等状态恢复*/
    private void resetMenuLayout(boolean anim) {
        if (scrollShowMenuLayout) {
            scrollShowMenuLayout = false;
            if (anim) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollNeedShowMenuLayout = false;
                        mTopView.setAlpha(1f);
                        layoutMenuView(0);

                        startScrollMenuY = 0;
                        startScrollToMenuY = 0;
                    }
                }, 160);
            } else {
                scrollNeedShowMenuLayout = false;
                mTopView.setAlpha(1f);
                layoutMenuView(0);

                startScrollMenuY = 0;
                startScrollToMenuY = 0;
            }
        }
    }

    private void startScroll(int to) {
        int scrollY = getScrollY();
        mScroller.startScroll(0, scrollY, 0, to - scrollY);
        postInvalidate();
    }

    @Override
    public void scrollBy(int x, int y) {
        int scrollY = getScrollY();
        final int endY = scrollY + y;

        if (order == TOP) {
            if (endY > 0) {
                y = -scrollY;
            }
        } else if (order == BOTTOM) {
            if (endY < 0) {
                y = -scrollY;
            }
        }
        super.scrollBy(x, y);
    }

    /*是否满足进入菜单模式的条件*/
    private boolean isNeedToMenuLayout() {
        int scrollY = getScrollY();
        //菜单滚动处理
        int menuLayoutHeight = getMenuLayoutHeight();
        if (menuLayoutHeight > 0) {
            //激活了菜单
            int topHeight = mTopView.getMeasuredHeight();
            int translationTo = (-scrollY - topHeight - menuOpenThreshold) * 2;

            if (translationTo >= menuLayoutHeight / 3 /*菜单显示了三分之一, 进入菜单模式*/) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (getScrollY() == 0) {
            if (y < 0) {
                //需要滚动刷新, 判断刷新的方向是否支持刷新. 不支持不处理滚动
                if (mDirection == TOP || mDirection == BOTH) {

                } else {
                    return;
                }
            }
            if (y > 0) {
                //需要上拉加载
                if (mDirection == BOTTOM || mDirection == BOTH) {

                } else {
                    return;
                }
            }
        }

        //修正滚动y值
        if (mDirection == TOP) {
            y = Math.min(y, 0);
        }
        if (mDirection == BOTTOM) {
            y = Math.max(y, 0);
        }

        if (mCurState == TOP) {
            y = Math.min(y, 0);
        } else if (mCurState == BOTTOM) {
            y = Math.max(y, 0);
        }

        super.scrollTo(0, y);

        int scrollY = getScrollY();
        int absScrollY = Math.abs(scrollY);

        layoutTipView();

        //菜单滚动处理
        if (scrollShowMenuLayout) {
            //菜单模式
            if (startScrollToMenuY > startScrollMenuY) {
                int translationTo = (int) (startScrollMenuHeight +
                        (startScrollToMenuY - startScrollMenuHeight) * (
                                (absScrollY - startScrollMenuY) * 1f / (startScrollToMenuY - startScrollMenuY)));
                layoutMenuView(translationTo);
                mTopView.setAlpha(0f);
            } else {
                int translationTo = -y;
                layoutMenuView(translationTo);
                mTopView.setAlpha(0f);
                //Log.e("angcyo", translationTo + "");
            }
        } else {
            int menuLayoutHeight = getMenuLayoutHeight();
            if (menuLayoutHeight > 0) {
                //激活了菜单
                int topHeight = mTopView.getMeasuredHeight();
                //int bottomHeight = mBottomView.getMeasuredHeight();
                scrollNeedShowMenuLayout = -y > topHeight + menuOpenThreshold;
                if (scrollNeedShowMenuLayout) {
                    int translationTo = (-y - topHeight - menuOpenThreshold) * 2;
                    layoutMenuView(translationTo);

//                if (translationTo > menuLayoutHeight / 2 /*菜单显示了三分之一, 进入菜单模式*/) {
//                    scrollShowMenuLayout = true;
//                }

                    if (scrollShowMenuLayout) {
                    } else {
                        float alpha = 1 - translationTo * 1f / (menuLayoutHeight / 2);
                        mTopView.setAlpha(alpha);
                    }
                }
            }
        }

        if (scrollY < 0) {
            //刷新
            notifyTopListener(absScrollY);
        } else if (scrollY > 0) {
            //加载
            notifyBottomListener(absScrollY);
        } else {
            if (mCurState == FINISH || mCurState == NORMAL) {
                mCurState = NORMAL;
                notifyTopListener(absScrollY);
                notifyBottomListener(absScrollY);
            }
        }
    }

    private void notifyBottomListener(int rawY) {
        if (mBottomView != null /*&& mCurState != BOTTOM*/) {
            if (mBottomView instanceof OnBottomViewMoveListener
                    && !mBottomViewMoveListeners.contains(mBottomView)) {
                ((OnBottomViewMoveListener) mBottomView).onBottomMoveTo(this, rawY, mBottomView.getMeasuredHeight(), mCurState);
            }
            for (OnBottomViewMoveListener listener : mBottomViewMoveListeners) {
                listener.onBottomMoveTo(mBottomView, rawY, mBottomView.getMeasuredHeight(), mCurState);
            }
        }
    }

    private void notifyTopListener(int rawY) {
        if (mTopView != null /*&& mCurState != TOP*/) {
            if (mTopView instanceof OnTopViewMoveListener
                    && !mTopViewMoveListeners.contains(mTopView)) {
                ((OnTopViewMoveListener) mTopView).onTopMoveTo(this, rawY, mTopView.getMeasuredHeight(), mCurState);
            }
            for (OnTopViewMoveListener listener : mTopViewMoveListeners) {
                listener.onTopMoveTo(mTopView, rawY, mTopView.getMeasuredHeight(), mCurState);
            }
        }
    }

    public RefreshLayout addTopViewMoveListener(OnTopViewMoveListener listener) {
        mTopViewMoveListeners.add(listener);
        return this;
    }

    public RefreshLayout addBottomViewMoveListener(OnBottomViewMoveListener listener) {
        mBottomViewMoveListeners.add(listener);
        return this;
    }

    public RefreshLayout addOnRefreshListener(OnRefreshListener listener) {
        mRefreshListeners.add(listener);
        return this;
    }

    public RefreshLayout removeTopViewMoveListener(OnTopViewMoveListener listener) {
        mTopViewMoveListeners.remove(listener);
        return this;
    }

    public RefreshLayout removeBottomViewMoveListener(OnBottomViewMoveListener listener) {
        mBottomViewMoveListeners.remove(listener);
        return this;
    }

    public RefreshLayout removeRefreshListener(OnRefreshListener listener) {
        mRefreshListeners.remove(listener);
        return this;
    }

    /**
     * 是否拦截向下滚动, 影响下拉刷新的功能**
     *
     * @return true 激活下拉刷新功能
     */
    private boolean canScrollDown() {
        if (isEnabled() && mTopView != null &&
                (mDirection == TOP || mDirection == BOTH)) {
            if (mCurState == BOTTOM) {
                //如果当前正在上拉加载,则禁止刷新功能, 当然~~~你可以取消此限制
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * 是否拦截向上滚动, 影响上拉加载的功能
     *
     * @return true 激活上拉加载功能
     */
    private boolean canScrollUp() {
        if (isEnabled() && mBottomView != null &&
                (mDirection == BOTTOM || mDirection == BOTH)) {
            if (mCurState == TOP) {
                //如果当前正在下拉刷新,则禁止上拉功能, 当然~~~你可以取消此限制
                return false;
            }
            return true;
        }

        return false;
    }

    public void setCheckInnerChildScroll(boolean checkInnerChildScroll) {
        this.checkInnerChildScroll = checkInnerChildScroll;
    }

    /**
     * Child是否可以滚动
     *
     * @param direction 如果是大于0, 表示视图底部没有数据了, 即不能向上滚动了, 反之...
     */
    protected boolean innerCanChildScrollVertically(View view, int direction, float rawX, float rawY, float x, float y) {
        if (checkInnerChildScroll) {
            //项目特殊处理,可以注释掉
            if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                if (recyclerView.getChildCount() > 0) {
                    View childAt = recyclerView.getChildAt(0);
                    Rect rect = new Rect();
                    childAt.getGlobalVisibleRect(rect);
                    if (childAt instanceof RecyclerView && rect.contains(((int) rawX), (int) rawY)) {
                        return ViewCompat.canScrollVertically(childAt, direction);
                    }
                }
                return ViewCompat.canScrollVertically(view, direction);
            }
        }
        //---------------ebd-----------------
        if (mTargetView != null &&
                !(mTargetView instanceof RecyclerView)) {
            RecyclerView touchOnRecyclerView = ViewGroupExKt.getTouchOnRecyclerView(this, x, y);
            if (touchOnRecyclerView != null) {
                view = touchOnRecyclerView;
            }
        }

//        if (view instanceof StickTopLayout) {
//            if (direction == -1) {
//                return !((StickTopLayout) view).isTopStick();
//            } else {
//                return true;
//            }
//        }

        return UI.canChildScroll(view, direction);
    }

    /**
     * 关闭或者开启下拉刷新功能
     */
    public void setNotifyListener(boolean notifyListener) {
        if (notifyListener) {
            setNotifyListener(BOTH);
        } else {
            setNotifyListener(NONE);
            setPlaceholderView();
        }
    }

    /**
     * 设置需要监听事件的方向
     */
    public void setNotifyListener(int notifyListener) {
        this.mNotifyListener = notifyListener;
    }


    public void setPlaceholderView() {
        setTopView(new PlaceholderView(getContext()));
        setBottomView(new PlaceholderView(getContext()));
    }

    /**
     * 取消事件监听, 取消刷新视图
     */
    public void setNoNotifyPlaceholder() {
        setPlaceholderView();
        setNotifyListener(false);
    }

    public void setShowTip(String tip) {
        boolean oldShow = mShowTip;
        mShowTip = true;
        ((TextView) mTipView).setText(tip);

        if (oldShow) {
            mTipView.removeCallbacks(hideTipRunnable);
        } else {
            mTipView.clearAnimation();
            ScaleAnimation scaleAnimation = new ScaleAnimation(0.6f, 1, 1, 1,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(ANIM_TIME);
            mTipView.startAnimation(scaleAnimation);
        }
        mTipView.postDelayed(hideTipRunnable, 1000);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRefreshListeners.clear();
        mBottomViewMoveListeners.clear();
        mTopViewMoveListeners.clear();

        mTipView = null;
        mTargetView = null;
        mBottomView = null;
        mTopView = null;
    }

    /*菜单需要占用的高度*/
    private int getMenuLayoutHeight() {
        if (menuLayout == null) {
            return 0;
        }
        return menuLayout.getMeasuredHeight();
    }

    public FrameLayout getMenuLayout() {
        return menuLayout;
    }

    /**
     * 启动菜单, 并且设置菜单布局
     */
    public void addMenuLayout(View menuView) {
        if (menuLayout == null) {
            menuLayout = new FrameLayout(getContext());
            menuLayout.setId(R.id.base_refresh_menu);
        } else {
            if (menuLayout.getChildCount() > 0) {
                menuLayout.removeAllViews();
            }
        }

        LayoutParams layoutParams = menuView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER);
        }
        menuLayout.addView(menuView, layoutParams);

        if (menuLayout.getParent() == null) {
            addView(menuLayout, new LayoutParams(-1, -2));
        }
    }

    /**
     * 支持的刷新方向
     */
    @IntDef({TOP, BOTTOM, BOTH, NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
    }

    /**
     * 当前的刷新状态
     */
    @IntDef({TOP, BOTTOM, NORMAL, MOVE, FINISH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public interface OnTopViewMoveListener {
        /**
         * @param top       距离父View顶部的距离
         * @param maxHeight view的高度
         */
        void onTopMoveTo(View view, int top, int maxHeight, @State int state);
    }

    public interface OnBottomViewMoveListener {
        /**
         * @param bottom    距离父View底部的距离
         * @param maxHeight view的高度
         */
        void onBottomMoveTo(View view, int bottom, int maxHeight, @State int state);
    }

    /**
     * 刷新,上拉回调
     */
    public interface OnRefreshListener {
        /**
         * 正常的刷新事件回调
         */
        void onRefresh(@Direction int direction);
    }

    /**
     * 基类用来实现 下拉/上拉 放大缩小指示图, 加载中进度变化
     */
    public static abstract class BaseRefreshView extends View implements OnBottomViewMoveListener, OnTopViewMoveListener {

        Drawable mDrawable;
        Bitmap mBitmap;
        Rect mCenterRect, mProgressRect, mDrawRect;
        Paint mPaint;
        ValueAnimator mObjectAnimator;
        float mProgress = 0;
        private PorterDuffXfermode mXfermodeDstIn;
        private int mMoveOffset = 0;
        private int mLastMoveOffset = 0;
        private int mTouchSlop;

        public BaseRefreshView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            mDrawable.draw(canvas);
            int sc = canvas.saveLayer(mCenterRect.left, mCenterRect.top,
                    mCenterRect.right, mCenterRect.bottom,
                    null, Canvas.ALL_SAVE_FLAG);
            canvas.drawRect(mProgressRect, mPaint);
            mPaint.setXfermode(mXfermodeDstIn);
            canvas.drawBitmap(mBitmap, mCenterRect.left, mCenterRect.top, mPaint);
            mPaint.setXfermode(null);
            canvas.restoreToCount(sc);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mObjectAnimator != null) {
                mObjectAnimator.cancel();
            }
            mBitmap.recycle();
            mBitmap = null;
            mDrawable = null;
            mObjectAnimator = null;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            mDrawable = getResources().getDrawable(R.drawable.base_refresh_top_book);
            mBitmap = getBitmapFromDrawable();
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(SkinHelper.getSkin().getThemeSubColor());
            mCenterRect = new Rect();
            mProgressRect = new Rect();
            mDrawRect = new Rect();
            mXfermodeDstIn = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

            mTouchSlop = 0;//(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            //ViewConfiguration.get(getContext()).getScaledTouchSlop();

            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            setPadding(0, padding, 0, padding);

            mObjectAnimator = ObjectAnimator.ofFloat(0f, 1f);
            mObjectAnimator.setInterpolator(new LinearInterpolator());
            mObjectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mObjectAnimator.setRepeatMode(ValueAnimator.RESTART);
            mObjectAnimator.setDuration(1000);

            mObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    updateProgress(value);
                }
            });
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int height = mBitmap.getHeight();
            setMeasuredDimension(widthMeasureSpec, height + getPaddingTop() + getPaddingBottom());
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            centerRect();
            updateMove(mMoveOffset, h);
        }

        /**
         * 计算小图显示的矩形区域
         */
        protected abstract void centerRect();

        /**
         * 加载中的进度刷新
         */
        protected abstract void updateProgress(float progress);

        /**
         * 移动的时候,用来放大缩小指示图
         */
        protected abstract void updateMove(int move, int maxHeight);

        protected void startProgress() {
            if (!mObjectAnimator.isRunning()) {
                mObjectAnimator.start();
            }
        }

        protected void endProgress() {
            if (mObjectAnimator.isRunning()) {
                mObjectAnimator.end();
            }
            updateProgress(1);
        }


        private Bitmap getBitmapFromDrawable() {
            return BitmapFactory.decodeResource(getResources(), R.drawable.base_refresh_top_book);
        }

        private void onMove(int move, int maxHeight, @State int state) {
            if (state == FINISH) {
                endProgress();
            } else if (state == MOVE) {
                if (Math.abs(move - mLastMoveOffset) > mTouchSlop) {
                    updateMove(move, maxHeight);
                    mLastMoveOffset = move;
                }
            } else if (state == TOP || state == BOTTOM) {
                startProgress();
            } else if (state == NORMAL) {
                updateProgress(0);
            }

        }

        @Override
        public void onBottomMoveTo(View view, int bottom, int maxHeight, @State int state) {
            onMove(bottom, maxHeight, state);

        }

        @Override
        public void onTopMoveTo(View view, int top, int maxHeight, @State int state) {
            onMove(top, maxHeight, state);
        }
    }

    /**
     * 默认实现的刷新布局
     */
    public static class BaseRefreshTopView extends BaseRefreshView {


        public BaseRefreshTopView(Context context) {
            super(context);
        }

        @Override
        protected void centerRect() {
            int viewWidth = getMeasuredWidth();
            int viewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            mCenterRect.set(viewWidth / 2 - width / 2, getPaddingTop() + viewHeight / 2 - height / 2,
                    viewWidth / 2 + width / 2, getPaddingTop() + viewHeight / 2 + height / 2);
        }

        @Override
        protected void updateProgress(float progress) {
            if (mProgress == progress) {
                return;
            }
            mProgress = progress;
            mProgressRect.set(mCenterRect.left, (int) (mCenterRect.bottom - (mCenterRect.height() * progress)),
                    mCenterRect.right, mCenterRect.bottom);
            postInvalidate();
        }

        @Override
        protected void updateMove(int move, int maxHeight) {
            if (move <= getPaddingBottom()) {
                return;
            }

            int viewWidth = getMeasuredWidth();
            int viewHeight = getMeasuredHeight();
            int rawTop = move - getPaddingBottom();
            rawTop = Math.min(rawTop, maxHeight - getPaddingBottom() - getPaddingTop());

            int left = viewWidth / 2 - rawTop / 2;
            mDrawRect.set(left, viewHeight - rawTop - getPaddingBottom(), viewWidth / 2 + rawTop / 2, viewHeight - getPaddingBottom());
            mDrawable.setBounds(mDrawRect);
            postInvalidate();
        }
    }

    /**
     * 默认实现的上拉视图
     */
    public static class BaseRefreshBottomView extends BaseRefreshView {


        public BaseRefreshBottomView(Context context) {
            super(context);
        }

        @Override
        protected void centerRect() {
            int viewWidth = getMeasuredWidth();
            int viewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            mCenterRect.set(viewWidth / 2 - width / 2, getPaddingTop() + viewHeight / 2 - height / 2,
                    viewWidth / 2 + width / 2, getPaddingTop() + viewHeight / 2 + height / 2);
        }

        @Override
        protected void updateProgress(float progress) {
            if (mProgress == progress) {
                return;
            }
            mProgress = progress;
            mProgressRect.set(mCenterRect.left, getPaddingTop(),
                    mCenterRect.right, (int) (getPaddingTop() + mCenterRect.height() * progress));
            postInvalidate();
        }

        @Override
        protected void updateMove(int move, int maxHeight) {
            if (move <= getPaddingTop()) {
                return;
            }

            int viewWidth = getMeasuredWidth();
            int viewHeight = getMeasuredHeight();
            int rawBottom = move - getPaddingTop();
            rawBottom = Math.min(rawBottom, maxHeight - getPaddingBottom() - getPaddingTop());

            int left = viewWidth / 2 - rawBottom / 2;
            mDrawRect.set(left, getPaddingTop(), viewWidth / 2 + rawBottom / 2, getPaddingTop() + rawBottom);
            mDrawable.setBounds(mDrawRect);
            postInvalidate();
        }
    }

    public static abstract class SimpleRefreshListener implements OnRefreshListener {

        /**
         * 当刷新事件被忽略时, 会回调此方法
         */
        public void onNoNotifyRefresh(@Direction int direction) {

        }
    }

}
