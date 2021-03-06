package com.angcyo.uiview.less.recycler;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.angcyo.lib.L;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.RApplication;
import com.angcyo.uiview.less.draw.RDrawIndicator;
import com.angcyo.uiview.less.kotlin.ViewExKt;
import com.angcyo.uiview.less.recycler.adapter.RBaseAdapter;
import com.angcyo.uiview.less.resources.AnimUtil;
import com.angcyo.uiview.less.skin.SkinHelper;
import com.angcyo.uiview.less.utils.Reflect;
import com.angcyo.uiview.less.utils.ScreenUtil;
import com.angcyo.uiview.less.utils.UI;
import com.angcyo.uiview.less.widget.CanScrollUpCallBack;

/**
 * 简单封装的RecyclerView
 * <p>
 * 动画样式:https://github.com/wasabeef/recyclerview-animators
 * Created by angcyo on 16-03-01-001.
 */
public class RRecyclerView extends RecyclerView implements CanScrollUpCallBack {
    public static final long AUTO_SCROLL_TIME = 1500;

    protected LayoutManager mBaseLayoutManager;
    protected int spanCount = 2;
    protected int orientation = LinearLayout.VERTICAL;
    protected RBaseAdapter mAdapterRaw;
    protected boolean mItemAnim = false;
    protected boolean supportsChangeAnimations = false;
    protected boolean isFirstAnim = true;//布局动画只执行一次
    protected boolean layoutAnim = false;//是否使用布局动画
    /**
     * 当前自动滚动到的位置
     */
    protected int curScrollPosition = 0;
    /**
     * 是否激活滚动, 激活滚动是自动滚动的前提
     */
    protected boolean enableScroll = false;
    /**
     * 滚动时间间隔(毫秒)
     */
    protected long autoScrollTimeInterval = AUTO_SCROLL_TIME;
    protected Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            curScrollPosition++;
            if (getAdapter() != null) {
                int maxItemCount = getAdapter().getItemCount();
                if (curScrollPosition >= maxItemCount) {
                    curScrollPosition = 0;
                    scrollTo(0, false);
                } else {
                    int firstVisibleItemPosition = curScrollPosition;
                    LayoutManager layoutManager = getLayoutManager();
                    if (layoutManager instanceof LinearLayoutManager) {
                        firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    }
                    //L.e("call: run([])-> " + curScrollPosition + " " + firstVisibleItemPosition);
                    scrollTo(curScrollPosition, Math.abs(firstVisibleItemPosition - curScrollPosition) < 2);
                }
            }

            if (enableScroll) {
                postDelayed(autoScrollRunnable, autoScrollTimeInterval);
            }
        }
    };
    /**
     * 无限循环
     */
    protected boolean mInfiniteLoop = true;
    OnTouchListener mInterceptTouchListener;
    OnFastTouchListener mOnFastTouchListener;
    OnFlingEndListener mOnFlingEndListener;
    boolean isAutoStart = false;
    float fastDownX, fastDownY, lastMoveX, lastMoveY;
    long fastDownTime = 0L;
    RDrawIndicator mRDrawIndicator;
    private OnScrollListener mScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Adapter adapter = getAdapterRaw();
            if (adapter != null && adapter instanceof RBaseAdapter) {
                ((RBaseAdapter) adapter).onScrollStateChanged(RRecyclerView.this, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //滚动状态结束
                    ((RBaseAdapter) adapter).onScrollStateEnd(RRecyclerView.this,
                            isFirstItemVisible(), isLastItemVisible(),
                            UI.canChildScrollUp(recyclerView), UI.canChildScrollDown(recyclerView));
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Adapter adapter = getAdapterRaw();
            if (adapter != null && adapter instanceof RBaseAdapter) {
                ((RBaseAdapter) adapter).onScrolled(RRecyclerView.this, dx, dy);
                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    //滚动结束
                    ((RBaseAdapter) adapter).onScrollStateEnd(RRecyclerView.this,
                            isFirstItemVisible(), isLastItemVisible(),
                            UI.canChildScrollUp(recyclerView), UI.canChildScrollDown(recyclerView));
                }
            }
        }
    };
    private float mLastVelocity;
    private int mLastScrollOffset;
    private boolean isFling;
    /**
     * 当onAttachedToWindow时, 是否自动滚动到 {@link #onDetachedFromWindow()}时的位置
     */
    private boolean autoScrollToLastPosition = false;
    /**
     * 是否自动开始滚动, 当界面onAttachedToWindow的时候 有效
     */
    private boolean isEnableAutoStartScroll = false;
    private int lastVisiblePosition = -1;
    private int lastVisibleItemOffset = -1;
    private String widthHeightRatio;
    private boolean equWidth = false;
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Adapter adapter = getAdapterRaw();
            if (adapter != null && adapter instanceof RBaseAdapter) {
                ((RBaseAdapter) adapter).onScrolledInTouch(RRecyclerView.this, e1, e2, distanceX, distanceY);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //performClick();//模拟了 点击事件
            callOnClick();//很直接的调用点击listener
            return super.onSingleTapUp(e);
        }
    });
    private OnSizeChangedListener mOnSizeChangedListener;
    private OnTouchScrollListener mOnTouchScrollListener;

    public RRecyclerView(Context context) {
        this(context, null);
    }

    public RRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RRecyclerView);
        isEnableAutoStartScroll = typedArray.getBoolean(R.styleable.RRecyclerView_r_enable_auto_start_scroll, isEnableAutoStartScroll);
        enableScroll = typedArray.getBoolean(R.styleable.RRecyclerView_r_enable_scroll, enableScroll);
        autoScrollToLastPosition = typedArray.getBoolean(R.styleable.RRecyclerView_r_auto_scroll_to_last_position, autoScrollToLastPosition);
        autoScrollTimeInterval = typedArray.getInt(R.styleable.RRecyclerView_r_auto_scroll_time_interval, (int) autoScrollTimeInterval);
        widthHeightRatio = typedArray.getString(R.styleable.RRecyclerView_r_width_height_ratio);
        equWidth = typedArray.getBoolean(R.styleable.RRecyclerView_r_is_aeq_width, equWidth);
        supportsChangeAnimations = typedArray.getBoolean(R.styleable.RRecyclerView_r_supports_change_animations, supportsChangeAnimations);
        mInfiniteLoop = typedArray.getBoolean(R.styleable.RRecyclerView_r_loop_scroll, mInfiniteLoop);

        mRDrawIndicator = new RDrawIndicator(this, attrs);
        mRDrawIndicator.setShowIndicator(typedArray.getBoolean(R.styleable.RRecyclerView_r_show_indicator, false));
        setWillNotDraw(false);

        String layoutMatch = typedArray.getString(R.styleable.RRecyclerView_r_layout_match);
        if (!TextUtils.isEmpty(layoutMatch)) {
            resetLayoutManager(context, layoutMatch);
        }

        typedArray.recycle();

        initView(context);
    }

    public static void ensureGlow(RecyclerView recyclerView, int color) {
        if (!RApplication.isLollipop()) {
            if (recyclerView != null) {
                recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
            }
            return;
        }

        try {
            Reflect.invokeMethod(RecyclerView.class, recyclerView, "ensureTopGlow");
            Reflect.invokeMethod(RecyclerView.class, recyclerView, "ensureBottomGlow");
            Reflect.invokeMethod(RecyclerView.class, recyclerView, "ensureRightGlow");
            Reflect.invokeMethod(RecyclerView.class, recyclerView, "ensureLeftGlow");

            setEdgeEffect(recyclerView, color);
        } catch (Exception e) {
            L.e(e.getMessage());
        }
    }

    //-----------获取 默认的adapter, 获取 RBaseAdapter, 获取 AnimationAdapter----------//

    private static void setEdgeEffect(RecyclerView recyclerView, int color) {
        Object mGlow = Reflect.getMember(RecyclerView.class, recyclerView, "mTopGlow");
        setEdgetEffect(mGlow, color);
        mGlow = Reflect.getMember(RecyclerView.class, recyclerView, "mLeftGlow");
        setEdgetEffect(mGlow, color);
        mGlow = Reflect.getMember(RecyclerView.class, recyclerView, "mRightGlow");
        setEdgetEffect(mGlow, color);
        mGlow = Reflect.getMember(RecyclerView.class, recyclerView, "mBottomGlow");
        setEdgetEffect(mGlow, color);
    }

    public static void setEdgetEffect(Object edgeEffectCompat, @ColorInt int color) {
        Object mEdgeEffect = Reflect.getMember(edgeEffectCompat, "mEdgeEffect");
        Object mPaint;
        if (mEdgeEffect != null) {
            mPaint = Reflect.getMember(mEdgeEffect, "mPaint");
        } else {
            mPaint = Reflect.getMember(edgeEffectCompat, "mPaint");
        }

        if (mPaint instanceof Paint) {
            ((Paint) mPaint).setColor(color);
        }
    }

    protected void initView(Context context) {
        resetLayoutManager(context, getTagString());

        setItemAnim(mItemAnim);
        //clearOnScrollListeners();
        removeOnScrollListener(mScrollListener);
        //添加滚动事件监听
        addOnScrollListener(mScrollListener);
    }

    protected void resetLayoutManager(Context context, String match) {
        if (TextUtils.isEmpty(match) || "V".equalsIgnoreCase(match)) {
            mBaseLayoutManager = new LinearLayoutManagerWrap(context, orientation, false);
        } else {
            //线性布局管理器
            if ("H".equalsIgnoreCase(match)) {
                orientation = LinearLayoutManagerWrap.HORIZONTAL;
                mBaseLayoutManager = new LinearLayoutManagerWrap(context, orientation, false);
            } else {
                //读取其他配置信息(数量和方向)
                final String type = match.substring(0, 1);
                if (match.length() >= 3) {
                    try {
                        spanCount = Integer.valueOf(match.substring(2));//数量
                    } catch (Exception e) {
                    }
                }
                if (match.length() >= 2) {
                    if ("H".equalsIgnoreCase(match.substring(1, 2))) {
                        orientation = StaggeredGridLayoutManager.HORIZONTAL;//方向
                    }
                }

                //交错布局管理器
                if ("S".equalsIgnoreCase(type)) {
                    mBaseLayoutManager = new StaggeredGridLayoutManagerWrap(spanCount, orientation);
                }
                //网格布局管理器
                else if ("G".equalsIgnoreCase(type)) {
                    mBaseLayoutManager = new GridLayoutManagerWrap(context, spanCount, orientation, false);
                }
            }
        }

        if (mBaseLayoutManager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) mBaseLayoutManager).setRecycleChildrenOnDetach(true);
        }
        this.setLayoutManager(mBaseLayoutManager);
    }

    //----------------end--------------------//

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (TextUtils.equals("aequilate", getContentDescription()) || equWidth) {
            /**自动设置等宽的RecyclerView*/
            setMeasuredDimension(getMeasuredWidth(), Math.min(getMeasuredWidth(), getMeasuredHeight()));
        } else {
            int[] ints = ViewExKt.calcWidthHeightRatio(this, widthHeightRatio);
            if (ints != null) {
                setMeasuredDimension(ints[0], ints[1]);
            }
        }
    }

//    @Override
//    public RBaseAdapter getAdapter() {
//        return (RBaseAdapter) super.getAdapter();
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
        if (!isInEditMode()) {
            ensureGlow(RRecyclerView.this, SkinHelper.getSkin().getThemeSubColor());
        }
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        resetLayoutManager(getContext(), getTagString());
    }

    public void setLayoutMatch(String match) {
        resetLayoutManager(getContext(), match);
    }

    private String getTagString() {
        Object tag = getTag();
        if (tag == null) {
            return "";
        }
        if (tag instanceof String) {
            return (String) tag;
        }
        return "";
    }

    @Override
    public void startLayoutAnimation() {
        if (isFirstAnim) {
            super.startLayoutAnimation();
        }
        isFirstAnim = false;
    }

    /**
     * 是否设置布局动画
     */
    public void setLayoutAnim(boolean layoutAnim) {
        this.layoutAnim = layoutAnim;
        if (layoutAnim) {
            AnimUtil.applyLayoutAnimation(this);
        } else {
            setLayoutAnimation(null);
        }
    }

    /**
     * 请在{@link RRecyclerView#setAdapter(Adapter)}方法之前调用
     */
    public void setItemAnim(boolean itemAnim) {
        mItemAnim = itemAnim;
        if (mItemAnim) {
            this.setItemAnimator(new FadeInDownAnimator());
        } else {
            this.setItemAnimator(new DefaultItemAnimator());
            setSupportsChangeAnimations(supportsChangeAnimations);
        }
    }

    /**
     * 取消默认动画
     */
    public void setItemNoAnim() {
        supportsChangeAnimations = false;
        setItemAnim(false);
        this.setItemAnimator(null);
    }

    /**
     * @see SimpleItemAnimator#setSupportsChangeAnimations(boolean)
     */
    public void setSupportsChangeAnimations(boolean supportsChangeAnimations) {
        this.supportsChangeAnimations = supportsChangeAnimations;
        ItemAnimator itemAnimator = getItemAnimator();
        if (itemAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) itemAnimator).setSupportsChangeAnimations(supportsChangeAnimations);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof RBaseAdapter) {
            mAdapterRaw = (RBaseAdapter) adapter;
            addOnChildAttachStateChangeListener(mAdapterRaw);
        }

        super.setAdapter(adapter);
    }

    public RBaseAdapter getAdapterRaw() {
        return mAdapterRaw;
    }

    /**
     * 设置Item 动画类, 用于 添加 和 删除 Item时候的动画
     */
    public RRecyclerView setBaseItemAnimator(Class<? extends BaseItemAnimator> animator) {
        try {
            super.setItemAnimator(animator.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isEnabled()) {
            return false;
        }
        boolean onTouchEvent = super.onTouchEvent(e);
        mGestureDetector.onTouchEvent(e);
        if (getAdapter() == null || getLayoutManager() == null) {
            return false;
        }
        return onTouchEvent;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (Math.abs(velocityY) > 200) {
            isFling = true;
        }
        return super.fling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int actionMasked = MotionEventCompat.getActionMasked(ev);
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            isFling = false;

            lastMoveX = fastDownX = ev.getX();
            lastMoveY = fastDownY = ev.getY();
            fastDownTime = ev.getDownTime();

            if (enableScroll && isEnabled()) {
                stopAutoScroll();
            }
        } else if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL) {

            if (actionMasked == MotionEvent.ACTION_UP) {

                long eventTime = ev.getEventTime();
                int dv = (int) (10 * ScreenUtil.density());

                float x = ev.getX();
                float y = ev.getY();


                if (eventTime - fastDownTime <= OnFastTouchListener.FAST_TIME) {

                    if (mOnFastTouchListener != null) {
                        if (Math.abs(x - fastDownX) <= dv && Math.abs(y - fastDownY) <= dv) {
                            mOnFastTouchListener.onFastClick();
                        }
                    }
                }

                if (eventTime - fastDownTime <= OnFastTouchListener.FAST_TIME * 2
                        && mOnTouchScrollListener != null) {
                    if (fastDownY - y > 3 * dv) {
                        mOnTouchScrollListener.onFastScrollToTop(this);
                    }
                }
            }

            if (enableScroll && isEnabled()) {
                startAutoScroll();
            }
        } else if (actionMasked == MotionEvent.ACTION_MOVE) {
            float x = ev.getX();
            float y = ev.getY();

            if (mOnTouchScrollListener != null) {
                mOnTouchScrollListener.onTouchScroll(this,
                        fastDownX, fastDownY,
                        x, y,
                        (int) (lastMoveX - x), (int) (lastMoveY - y));
            }

            lastMoveX = x;
            lastMoveY = y;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (mInterceptTouchListener != null) {
            mInterceptTouchListener.onTouch(this, e);
        }

        //项目特殊处理, 可以删除
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(0);
            Rect rect = new Rect();
            childAt.getGlobalVisibleRect(rect);
            if (childAt instanceof RecyclerView && rect.contains(((int) e.getRawX()), (int) e.getRawY())) {
                //如果touch在另一个RecycleView上面, 那么不拦截事件
                return false;
            }
        }
        //--------end--------
        return super.onInterceptTouchEvent(e);
    }

    public void setOnInterceptTouchListener(OnTouchListener l) {
        mInterceptTouchListener = l;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (autoScrollToLastPosition) {
            saveLastPosition();
        }
        //L.e("call: onDetachedFromWindow([]) 1-> " + computeHorizontalScrollRange() + ":" + computeHorizontalScrollExtent() + ":" + computeHorizontalScrollOffset());
        super.onDetachedFromWindow();
        stopAutoScroll();
        //L.e("call: onDetachedFromWindow([]) 2-> " + computeHorizontalScrollRange() + ":" + computeHorizontalScrollExtent() + ":" + computeHorizontalScrollOffset());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isEnableAutoStartScroll) {
            startAutoScroll();
        }

        resetToLastPosition();
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        //L.e("call: onScrolled([dx, dy])-> " + getLastVelocity());
    }

    /**
     * 恢复滚动信息
     */
    public void resetToLastPosition() {
        if (autoScrollToLastPosition &&
                lastVisiblePosition >= 0) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(lastVisiblePosition, lastVisibleItemOffset);
            }
        }
    }

    /**
     * 保存滚动的位置信息
     */
    public void saveLastPosition() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            lastVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

            if (layoutManager.getChildCount() > 0) {
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    lastVisibleItemOffset = getChildAt(0).getLeft();
                } else {
                    lastVisibleItemOffset = getChildAt(0).getTop();
                }
            }
        }
    }

    public void startAutoScroll() {
        LayoutManager layoutManager = getLayoutManager();
        if (enableScroll && getAdapter() != null && getAdapter().getItemCount() > 1 &&
                layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            curScrollPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            autoScroll();
        }
    }

    protected void autoScroll() {
        if (isAutoStart) {
            return;
        }
        isAutoStart = true;
        postDelayed(autoScrollRunnable, autoScrollTimeInterval);
    }

    public void setEnableAutoStartScroll(boolean enableAutoStartScroll) {
        isEnableAutoStartScroll = enableAutoStartScroll;
        if (enableAutoStartScroll) {
            startAutoScroll();
        } else {
            stopAutoScroll();
        }
    }

    public void setEnableScroll(boolean enableScroll) {
        this.enableScroll = enableScroll;
    }

    public void stopAutoScroll() {
        isAutoStart = false;
        removeCallbacks(autoScrollRunnable);
    }

    @Override
    public void onScrollStateChanged(int state) {
        //L.e("call: onScrollStateChanged([state])-> " + state + " :" + getLastVelocity());
        final int scrollOffset = computeVerticalScrollOffset();
        if (state == SCROLL_STATE_IDLE && isFling && scrollOffset == 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (mLastScrollOffset != scrollOffset && mOnFlingEndListener != null) {
                        if (UI.canChildScrollDown(RRecyclerView.this)) {
                            mOnFlingEndListener.onScrollTopEnd(getLastVelocity());
                        }
                    }
                    mLastScrollOffset = -1;
                }
            });
        } else {
            mLastScrollOffset = scrollOffset;
        }
    }

    public void setOnFlingEndListener(OnFlingEndListener onFlingEndListener) {
        mOnFlingEndListener = onFlingEndListener;
    }

    /**
     * 滚动结束后时的速率
     */
    public float getLastVelocity() {
        Object mViewFlinger = Reflect.getMember(RecyclerView.class, this, "mViewFlinger");
        Object mScroller = Reflect.getMember(mViewFlinger, "mScroller");
        float currVelocity = 0f;
        if (mScroller instanceof OverScroller) {
            currVelocity = ((OverScroller) mScroller).getCurrVelocity();
        } else if (mScroller instanceof ScrollerCompat) {
            currVelocity = ((ScrollerCompat) mScroller).getCurrVelocity();
        } else {
            throw new IllegalArgumentException("未兼容的mScroller类型:" + mScroller.getClass().getSimpleName());
        }

        if (Float.isNaN(currVelocity)) {
            currVelocity = mLastVelocity;
        } else {
            mLastVelocity = currVelocity;
        }
        return currVelocity;
    }

    /**
     * 去掉动画, 即可吸顶
     */
    public void scrollTo(int position, boolean anim) {
        LayoutManager manager = getLayoutManager();
        if (manager == null || position < 0) {
            return;
        }
        stopScroll();
        if (anim) {
            View view = manager.findViewByPosition(position);
            if (view != null) {
                //view已经在界面上显示, 调用smoothScrollToPosition是不会有滚动效果的
                smoothScrollBy(0, view.getTop());
            } else {
                smoothScrollToPosition(position);
            }
            return;
        }

        if (manager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, 0);
        } else {
            ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(position, 0);
        }
    }

    public void scrollToFirst(int position) {
        scrollTo(position, false);
    }

    /**
     * 滚动到底部
     */
    public void scrollToLastBottom(boolean anim) {
        scrollToLastBottom(anim, true);
    }

    public void scrollToLastBottom(boolean anim,
                                   boolean checkScroll /*是否检查已经滚动到底部, 或者已经不能滚动了*/) {
        int itemCount = -1;

        if (getAdapter() != null) {
            itemCount = getAdapter().getItemCount();
        }

        final LayoutManager manager = getLayoutManager();
        if (manager == null) {
            return;
        }

        if (itemCount > 0 && checkScroll
                && !ViewCompat.canScrollVertically(this, 1)
                && isLastItemVisible(true)) {
            //已经是底部
            L.w("已经在底部,无需滚动 ");
            return;
        }

        itemCount = manager.getItemCount();
        if (itemCount < 1) {
            return;
        }
        final int position = itemCount - 1;

        if (anim) {
            View view = manager.findViewByPosition(position);
            if (view != null) {
                //view已经在界面上显示, 调用smoothScrollToPosition是不会有滚动效果的
                smoothScrollBy(0, -view.getTop());
            } else {
                smoothScrollToPosition(position);
            }
            return;
        }

        if (manager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, 0);
            post(new Runnable() {
                @Override
                public void run() {
                    View target = manager.findViewByPosition(position);//然后才能拿到这个View
                    if (target != null) {
                        int offset = getMeasuredHeight() - target.getMeasuredHeight();
                        ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, offset);//滚动偏移到底部
                        //L.i("滚动至:" + position + " offset:" + offset);
                    }
                }
            });

        } else {
            ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(position, 0);
            post(new Runnable() {
                @Override
                public void run() {
                    View target = manager.findViewByPosition(position);//然后才能拿到这个View
                    if (target != null) {
                        int offset = getMeasuredHeight() - target.getMeasuredHeight();
                        ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(position,
                                offset);//滚动偏移到底部
                        //L.i("滚动至:" + position + " offset:" + offset);
                    }
                }
            });
        }
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);

        mRDrawIndicator.onDraw(c);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.save();
//        try {
//            canvas.translate(getScrollX(), getScrollY());
//            (((ViewGroup) getChildAt(0))).getChildAt(0).onDraw(canvas);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        canvas.restore();
    }

    public void setAutoScrollToLastPosition(boolean autoScrollToLastPosition) {
        this.autoScrollToLastPosition = autoScrollToLastPosition;
    }

    public int getLastVisiblePosition() {
        return lastVisiblePosition;
    }

    public int getLastVisibleItemOffset() {
        return lastVisibleItemOffset;
    }

    public void setCurScrollPosition(int curScrollPosition) {
        this.curScrollPosition = curScrollPosition;
    }

    public void setLastItemInGridLayoutManager(final GridLayoutManager.SpanSizeLookup spanSizeLookup /*可以为null*/) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;

            Adapter adapter = getAdapter();
            if (adapter instanceof RBaseAdapter) {
                final RBaseAdapter baseAdapter = (RBaseAdapter) adapter;

                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (baseAdapter.isLast(position) && baseAdapter.isEnableLoadMore()) {
                            return gridLayoutManager.getSpanCount();
                        }
                        if (spanSizeLookup != null && spanSizeLookup.getSpanSize(position) > 0) {
                            return spanSizeLookup.getSpanSize(position);
                        }
                        return 1;
                    }
                });
            }
        }
    }

    /**
     * 是否已经到了顶部
     */
    public boolean isTopEnd() {
        return !UI.canChildScrollUp(this);
    }

    /**
     * 是否已经到了底部
     */
    public boolean isBottomEnd() {
        return !UI.canChildScrollDown(this);
    }

    public void setOnFastTouchListener(OnFastTouchListener onFastTouchListener) {
        mOnFastTouchListener = onFastTouchListener;
    }

    /**
     * 获取第一个可见item的 adapter position
     */
    public int getFirstVisibleItemIndex() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        return -1;
    }

    /**
     * 第一个Item是否可见
     */
    public boolean isFirstItemVisible() {
        boolean visible = false;

        Adapter adapter = getAdapter();
        if (adapter != null && adapter.getItemCount() > 0) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                visible = firstVisibleItemPosition == 0;
            }
        }
        return visible;
    }

    public boolean isLastItemVisible(boolean completelyVisible) {
        return isLastItemVisible(completelyVisible, false);
    }

    /**
     * 最后一个Item是否可见
     */
    public boolean isLastItemVisible(boolean completelyVisible /*是否需要完全可见*/,
                                     boolean ignoreChildCount /*当child数量为0时, 是否当作可见*/) {
        boolean visible = false;

        Adapter adapter = getAdapter();
        if (adapter != null && adapter.getItemCount() > 0) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int lastVisibleItemPosition;
                LinearLayoutManager lm = (LinearLayoutManager) layoutManager;
                int childCount = lm.getChildCount();
                int count = lm.getItemCount();

                if (ignoreChildCount && childCount == 0 && count != 0) {
                    //有数据, 但是还没有开始布局, 一般在刚设置adapter或者adapter的data时候出现
                    return true;
                }

                //如果触发过RecyclerView的scroll, 这个字段会有值
                Object member = Reflect.getMember(LinearLayoutManager.class, lm, "mPendingScrollPosition");
                if (member != null) {
                    int mPendingScrollPosition = (int) member;
                    if (mPendingScrollPosition != NO_POSITION) {
                        return mPendingScrollPosition == adapter.getItemCount() - 1;
                    }
                }

                if (completelyVisible) {
                    /**
                     * 最后一个Item完全可见, 顶部和底部 都在屏幕内
                     */
                    lastVisibleItemPosition = lm.findLastCompletelyVisibleItemPosition();

                    if (lastVisibleItemPosition == NO_POSITION) {
                        //没有找到
                        ViewHolder viewHolder = findViewHolderForAdapterPosition(count - 1);
                        if (viewHolder != null) {
                            if (viewHolder.itemView.getBottom() <= getBottom()) {
                                //最后一个Item的底部可见状态
                                lastVisibleItemPosition = count - 1;
                            }
                        }
                    }
                } else {
                    lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                }

                visible = lastVisibleItemPosition == adapter.getItemCount() - 1;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {

            }
        }
        return visible;
    }

    /**
     * 最后一个item是否可见
     */
    public boolean isLastItemVisible() {
        return isLastItemVisible(false);
    }

    @Override
    public boolean canChildScrollUp() {
        return UI.canChildScrollUp(this);
    }

    @Override
    public RecyclerView getRecyclerView() {
        return this;
    }

    public OnSizeChangedListener getOnSizeChangedListener() {
        return mOnSizeChangedListener;
    }

    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        mOnSizeChangedListener = onSizeChangedListener;
    }

    /**
     * 局部刷新
     */
    public void localRefresh(RBaseAdapter.OnLocalRefresh onLocalRefresh) {
        RBaseAdapter.localRefresh(this, onLocalRefresh);
    }

    /**
     * 取消滚动增益特效
     */
    public void setNoOverScrollMode() {
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void setOnTouchScrollListener(OnTouchScrollListener onTouchScrollListener) {
        mOnTouchScrollListener = onTouchScrollListener;
    }

    public RDrawIndicator getRDrawIndicator() {
        return mRDrawIndicator;
    }

    /**
     * RecyclerView滚动结束后的回调
     */
    public interface OnFlingEndListener {
        /**
         * 突然滚动到顶部, 还剩余的滚动速率
         */
        void onScrollTopEnd(float currVelocity);
    }

    public interface OnFastTouchListener {

        int FAST_TIME = 100;

        /**
         * 快速单击事件监听 (100毫秒内的DOWN UP)
         */
        void onFastClick();
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }

    /**
     * Touch事件, 触发的Scrolll监听
     */
    public static class OnTouchScrollListener {

        /**
         * Touch事件触发的Scroll
         */
        public void onTouchScroll(@NonNull RRecyclerView recyclerView,
                                  float downX, float downY,
                                  float eventX, float eventY,
                                  int dx, int dy) {

        }

        /**
         * 快速手指向上滑动, 用来在聊天界面显示键盘
         */
        public void onFastScrollToTop(@NonNull RRecyclerView recyclerView) {

        }
    }

    public static class LinearLayoutManagerWrap extends LinearLayoutManager {

        public LinearLayoutManagerWrap(Context context) {
            super(context);
        }

        public LinearLayoutManagerWrap(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public LinearLayoutManagerWrap(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onLayoutChildren(Recycler recycler, State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (Exception e) {
                L.e("LinearLayoutManagerWrap onLayoutChildren异常-> " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static class GridLayoutManagerWrap extends GridLayoutManager {

        public GridLayoutManagerWrap(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public GridLayoutManagerWrap(Context context, int spanCount) {
            super(context, spanCount);
        }

        public GridLayoutManagerWrap(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(Recycler recycler, State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (Exception e) {
                L.e("GridLayoutManagerWrap onLayoutChildren异常-> " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static class StaggeredGridLayoutManagerWrap extends StaggeredGridLayoutManager {

        public StaggeredGridLayoutManagerWrap(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public StaggeredGridLayoutManagerWrap(int spanCount, int orientation) {
            super(spanCount, orientation);
        }

        @Override
        public void onLayoutChildren(Recycler recycler, State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (Exception e) {
                L.e("StaggeredGridLayoutManagerWrap onLayoutChildren异常-> " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
