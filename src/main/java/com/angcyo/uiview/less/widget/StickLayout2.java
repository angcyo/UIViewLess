package com.angcyo.uiview.less.widget;

import android.content.Context;
import android.support.annotation.Px;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.OverScroller;
import android.widget.RelativeLayout;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.kotlin.ViewExKt;
import com.angcyo.uiview.less.recycler.RRecyclerView;
import com.angcyo.uiview.less.utils.Reflect;


/**
 * Created by angcyo on 2017-03-15.
 * <p>
 * 布局规则
 * 1: 先布局 头部
 * 2: 内容部分
 * 3: 悬停布局, 可以不需要
 */

public class StickLayout2 extends RelativeLayout {

    View mFloatView;
    int floatTopOffset = 0;
    int floatTop = 0;//
    float downY, downX, lastX;
    CanScrollUpCallBack mScrollTarget;
    boolean inTopTouch = false;
    boolean isFirst = true;
    OnScrollListener mOnScrollListener;
    /**
     * 是否滚动了, 滚动了之后, 拦截Touch事件, 防止传递给子View
     */
    boolean isScroll = false;
    boolean isNestedScrollAccepted = false;
    private OverScroller mOverScroller;
    private GestureDetectorCompat mGestureDetectorCompat;
    private int maxScrollY, topHeight;
    private RRecyclerView.OnFlingEndListener mOnFlingEndListener;
    private boolean handleTouch = true;
    private int lastOffsetY;
    private float mLastVelocity = 0f;
    private boolean isFling;
    /**
     * 滚动顶部后, 是否可以继续滚动, 增益效果
     */
    private boolean edgeScroll = false;
    private int viewMaxHeight;
    private boolean mWantV = true;
    private int mStartScrollY;

    public StickLayout2(Context context) {
        this(context, null);
    }

    public StickLayout2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickLayout2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    public static void setNotHandleNestedScroll(View targetView) {
        targetView.setTag(R.id.tag_not_handle_nested_scroll, "true");
    }

    public void setEdgeScroll(boolean edgeScroll) {
        this.edgeScroll = edgeScroll;
    }

    private void initLayout() {
        mOverScroller = new OverScroller(getContext());
        mGestureDetectorCompat = new GestureDetectorCompat(getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, final float velocityY) {
                        if (Math.abs(velocityX) > Math.abs(velocityY)) {
                            return false;
                        }

                        if (isFloat() /*&& velocityY < 0*/) {
                            return false;
                        }
                        fling(velocityY);
                        return true;
                    }
                });
    }

    private void fling(float velocityY) {
        if (!isEnabled()) {
            return;
        }

        isFling = true;
        int maxY = viewMaxHeight;
        final RecyclerView recyclerView = mScrollTarget.getRecyclerView();
        if (recyclerView != null) {
            maxY = Math.max(maxY, recyclerView.computeVerticalScrollRange());
        }
        mOverScroller.fling(0, getScrollY(), 0, (int) -velocityY, 0, 0, 0, maxY);
        postInvalidate();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        if (getChildCount() > 1) {
            View scrollView = getChildAt(1);
            if (scrollView instanceof CanScrollUpCallBack) {
                mScrollTarget = (CanScrollUpCallBack) scrollView;
            } else {
                if (mScrollTarget == null) {
                    mScrollTarget = new CanScrollUpCallBack() {
                        @Override
                        public boolean canChildScrollUp() {
                            return false;
                        }

                        @Override
                        public RecyclerView getRecyclerView() {
                            return null;
                        }
                    };
                }
            }
        }

        if (getChildCount() > 2) {
            mFloatView = getChildAt(2);
        }
    }

    @Override
    public void computeScroll() {
        if (mOverScroller.computeScrollOffset() && isEnabled()) {
            int currY = mOverScroller.getCurrY();
            if (currY - maxScrollY >= 0) {
                if (isFling) {
                    final RecyclerView recyclerView = mScrollTarget.getRecyclerView();
                    if (recyclerView != null) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                //recyclerView.fling(0, Math.max(0, 1000));


                                final float lastVelocity = getLastVelocity();
                                recyclerView.fling(0, (int) Math.max(0, Math.abs(lastVelocity)));

//                                int velocityDecay = getChildAt(0).getMeasuredHeight() * 3;//速度衰减值
//                                if (lastVelocity < velocityDecay) {
//                                    recyclerView.fling(0, Math.max(0, (int) Math.abs(lastVelocity)));
//                                } else {
//                                    recyclerView.fling(0, Math.max(0, (int) lastVelocity - velocityDecay));
//                                }
                            }
                        });
                    }
                }
                isFling = false;
            }
            scrollTo(0, currY);
            postInvalidate();
        }
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        int offset = Math.min(maxScrollY, edgeScroll ? y : Math.max(0, y));
        boolean layout = false;
        if (getScrollY() != offset) {
            layout = true;
        }
        super.scrollTo(0, offset);
        if (layout) {
            requestLayout();
        }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollTo(offset);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        View topView = getChildAt(0);
        View scrollView = getChildAt(1);

        if (topView instanceof IWebView) {
            int webViewContentHeight = ((IWebView) topView).getWebViewContentHeight();
            topView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(1 << 30 - 1, MeasureSpec.AT_MOST));
            int topViewMeasuredHeight = topView.getMeasuredHeight();

            //L.e("测量高度:" + topViewMeasuredHeight + " 内容高度:" + webViewContentHeight + " Range:" + ((IWebView) topView).getWebViewVerticalScrollRange());

            if (webViewContentHeight > 0 && topViewMeasuredHeight > 10_000 && (topViewMeasuredHeight - webViewContentHeight > 1_00)) {
                //topView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(webViewContentHeight * 2 + 100, MeasureSpec.AT_MOST));
            }

//            topView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
        } else {
            measureChild(topView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED));
        }

        if (mFloatView != null) {
            if (mFloatView.getVisibility() == View.GONE) {
                measureChild(mFloatView, ViewExKt.exactlyMeasure(this, 0), ViewExKt.exactlyMeasure(this, 0));
            } else {
                measureChild(mFloatView, widthMeasureSpec, heightMeasureSpec);
            }
        }
        measureChild(scrollView, widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(heightSize - floatViewHeight() - floatTopOffset, MeasureSpec.EXACTLY));

        floatTop = topView.getMeasuredHeight();
        maxScrollY = floatTop - floatTopOffset;
        topHeight = floatTop + floatViewHeight();

        viewMaxHeight = floatTop + floatViewHeight() + scrollView.getMeasuredHeight();
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    int floatViewHeight() {
        if (mFloatView != null) {
            return mFloatView.getMeasuredHeight();
        }
        return 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //super.onLayout(changed, l, t, r, b);
        //L.w("onLayout() -> " + changed + " l:" + l + " t:" + t + " r:" + r + " b:" + b);
        View firstView = getChildAt(0);
        firstView.layout(0, 0, r, firstView.getMeasuredHeight());

        View lastView = getChildAt(1);
        lastView.layout(0, firstView.getMeasuredHeight() + floatViewHeight(), r,
                firstView.getMeasuredHeight() + floatViewHeight() + lastView.getMeasuredHeight());

        if (mFloatView != null) {
            int scrollY = getScrollY();
            if (isFloat()) {
                mFloatView.layout(mFloatView.getLeft(), scrollY + floatTopOffset, r,
                        scrollY + floatTopOffset + mFloatView.getMeasuredHeight());
            } else {
                mFloatView.layout(mFloatView.getLeft(), firstView.getMeasuredHeight(), r,
                        firstView.getMeasuredHeight() + mFloatView.getMeasuredHeight());
            }
        }

        initScrollTarget();
    }

    private void initScrollTarget() {
        if (mScrollTarget != null && mScrollTarget.getRecyclerView() instanceof RRecyclerView) {
            if (mOnFlingEndListener == null) {
                mOnFlingEndListener = new RRecyclerView.OnFlingEndListener() {
                    @Override
                    public void onScrollTopEnd(float currVelocity) {
//                        if (!(currVelocity > 0)) {
//                            //向下滑动产生的fling操作, 才处理
//                            fling(currVelocity);
//                        }
                        fling(currVelocity);
                    }
                };
            }
            ((RRecyclerView) mScrollTarget.getRecyclerView()).setOnFlingEndListener(mOnFlingEndListener);
        }
    }

    private boolean isFloat() {
        return getScrollY() >= (floatTop - floatTopOffset);
    }

    public void setFloatTopOffset(int floatTopOffset) {
        this.floatTopOffset = floatTopOffset;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        L.e("call: onTouchEvent([event])-> " + ev.getAction());
        super.onTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        L.e("call: onInterceptTouchEvent([ev])-> " + ev.getAction());

        if (!isEnabled()) {
            return super.onInterceptTouchEvent(ev);
        }

        int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            onTouchUp();
            onTouchEnd();

            if (Math.abs(mStartScrollY - getScrollY()) >
                    ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                //ev.setAction(MotionEvent.ACTION_CANCEL);
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        L.e("call: dispatchTouchEvent([ev])-> " + ev.getAction());
//        if (!inTopTouch) {
//            return super.dispatchTouchEvent(ev);
//        }

//        if (getChildAt(0) instanceof IWebView) {
//            return super.dispatchTouchEvent(ev);
//        }

        if (!isEnabled()) {
            return super.dispatchTouchEvent(ev);
        }

        boolean event = mGestureDetectorCompat.onTouchEvent(ev);
        if (event) {
            ev.setAction(MotionEvent.ACTION_CANCEL);
            //return super.dispatchTouchEvent(ev);
        }

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!handleTouch) {
                    break;
                }

                float moveY = ev.getY() + 0.5f;
                float moveX = ev.getX() + 0.5f;
                int offsetY = (int) (downY - moveY);
                float offsetX = downX - moveX;

                downY = moveY;
                downX = moveX;

                int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

                if (Math.abs(offsetX) - Math.abs(offsetY) > 0
                    /*ViewConfiguration.get(getContext()).getScaledTouchSlop()*/) {
                    mWantV = false;
                } else if (Math.abs(offsetY) - Math.abs(offsetX) > 0
                    /*ViewConfiguration.get(getContext()).getScaledTouchSlop()*/) {
                    mWantV = true;
                } else {
                    if (isFirst) {
                        break;
                    }
                }

                boolean first = isFirst;
                if (Math.abs(offsetX) >= touchSlop || Math.abs(offsetY) >= touchSlop) {
                    isFirst = false;
                }

                //L.e("dispatchTouchEvent() -> " + offsetX + " " + offsetY + " w:" + wantV + "  f:" + first);

                if (first) {
                    if (!mWantV) {
                        if (Math.abs(offsetX) >= touchSlop || Math.abs(offsetY) >= touchSlop) {
                            handleTouch = false;
                        }
                        break;
                    }
                } else {
                    if (!inTopTouch) {
                        ev.setLocation(lastX, moveY);
                    }
                }
                if (!isNestedScrollAccepted) {
                    offsetTo(offsetY);
                }

                lastOffsetY = offsetY;
                //L.e("call: dispatchTouchEvent([ev])-> move..." + ensureOffset(offsetY));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onTouchUp();
                onTouchEnd();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private int ensureOffset(int offsetY) {
        int scrollY = getScrollY();
        int maxScrollY = this.maxScrollY;
        int scrollOffset = 0;

        int maxOffset = maxScrollY - scrollY;
        int minOffset = 0 - scrollY;
        scrollOffset = Math.max(minOffset, Math.min(maxOffset, offsetY));

        return scrollOffset;
    }

    private void onTouchUp() {
        downY = 0;
        downX = 0;
        isFirst = true;
        handleTouch = true;
        isScroll = false;
        mWantV = true;
    }

    private void onTouchEnd() {
        if (getScrollY() < 0) {
            startScrollTo(0);
        }
    }

    private void startScrollTo(int to) {
        int scrollY = getScrollY();
        mOverScroller.startScroll(0, scrollY, 0, to - scrollY);
        postInvalidate();
    }

    private boolean offsetTo(int offsetY) {
        if (Math.abs(offsetY) > 0) {
            if (offsetY < 0) {
                //手指下滑
                boolean scrollVertically = mScrollTarget.canChildScrollUp();

                if (getScrollY() < 0) {
                    offsetY *= (1 - 0.4 - Math.abs(getScrollY()) * 1.f / getMeasuredHeight());
                }

                if (!scrollVertically) {
                    isScroll = true;
                    scrollBy(0, offsetY);
                } else {
                    return true;
                }
            } else {
                if (isFloat()) {
                    return true;
                }
                isScroll = true;
                scrollBy(0, offsetY);
                lastOffsetY = offsetY;
            }
        }
        return false;
    }

    private void onTouchDown(MotionEvent ev) {
        onTouchUp();

        downY = ev.getY() + 0.5f;
        lastX = downX = ev.getX() + 0.5f;
        mStartScrollY = getScrollY();

        mOverScroller.abortAnimation();

        isFling = false;

        if (isFloat()) {
            if (floatViewHeight() + floatTopOffset > downY) {
                inTopTouch = true;
            } else {
                inTopTouch = false;
            }
        } else {
            if (topHeight - mStartScrollY > downY) {
                inTopTouch = true;
            } else {
                inTopTouch = false;
            }
        }
        isFirst = true;

        initScrollTarget();
    }

    /**
     * 滚动结束后时的速率
     */
    public float getLastVelocity() {
        Object mScrollerY = Reflect.getMember(OverScroller.class, mOverScroller, "mScrollerY");
        float currVelocity = (float) Reflect.getMember(mScrollerY, "mCurrVelocity");
        if (Float.isNaN(currVelocity)) {
            currVelocity = mLastVelocity;
        } else {
            mLastVelocity = currVelocity;
        }
        return currVelocity;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled();
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        isNestedScrollAccepted = true;
        mOverScroller.abortAnimation();
    }

    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
        isNestedScrollAccepted = false;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
        //L.e("call: onNestedPreScroll([target, dx, dy, consumed])-> scroll..." + dy);
//        if (target.getTag(R.id.tag_not_handle_nested_scroll) == null) {
        if (Math.abs(dy) > Math.abs(dx)) {
            offsetTo(dy);
            if (dy > 0) {
                consumed[1] = Math.min(dy, ensureOffset(lastOffsetY));
            }
        }
        //L.e("call: onNestedPreScroll([target, dx, dy, consumed])-> scroll..." + dy + " " + consumed[1] + "   " + lastOffsetY);
    }

//    @Override
//    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        if (Math.abs(velocityX) > Math.abs(velocityY)) {
//            return false;
//        }
//
//        if (isFloat() && velocityY > 0) {
//            //L.e("call: onFling return");
//            return super.onNestedPreFling(target, velocityX, velocityY);
//        }
//        fling(velocityY);
//        return super.onNestedPreFling(target, velocityX, velocityY);
//    }

    /**
     * 关闭头部
     */
    public void scrollToClose() {
        mOverScroller.startScroll(0, getScrollY(), 0, maxScrollY - getScrollY());
        postInvalidate();
    }

    /**
     * 打开头部
     */
    public void scrollToOpen() {
        mOverScroller.startScroll(0, getScrollY(), 0, -getScrollY());
        postInvalidate();
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    public void setScrollTarget(CanScrollUpCallBack scrollTarget) {
        mScrollTarget = scrollTarget;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mScrollTarget != null && mScrollTarget.getRecyclerView() instanceof RRecyclerView) {
            ((RRecyclerView) mScrollTarget.getRecyclerView()).setOnFlingEndListener(null);
        }
        mOnScrollListener = null;
        mOnFlingEndListener = null;
        mOnScrollListener = null;
    }
}
