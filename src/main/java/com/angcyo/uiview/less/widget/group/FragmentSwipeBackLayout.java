package com.angcyo.uiview.less.widget.group;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.base.BaseAppCompatActivity;
import com.angcyo.uiview.less.base.IFragment;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.kotlin.ViewExKt;
import com.angcyo.uiview.less.kotlin.ViewGroupExKt;
import com.angcyo.uiview.less.resources.AnimUtil;
import com.angcyo.uiview.less.utils.Debug;
import com.angcyo.uiview.less.utils.RUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.angcyo.uiview.less.utils.ScreenUtil.density;

/**
 * 可以用来显示IView的布局, 每一层的管理, 重写于2018-3-2
 * Created by angcyo on 2016-11-12.
 */

public class FragmentSwipeBackLayout extends SwipeBackLayout {

    private static final String TAG = "FragmentSwipeBackLayout";
    /**
     * 多指是否显示debug layout
     */
    public static boolean showDebugLayout = true;
    public static boolean showDebugInfo = false;
    public static boolean SHOW_DEBUG_TIME = L.LOG_DEBUG;
    protected boolean isAttachedToWindow = false;

    int hSpace = (int) (30 * getResources().getDisplayMetrics().density);
    int vSpace = (int) (30 * getResources().getDisplayMetrics().density);
    int viewMaxHeight = 0; //debug模式下的成员变量
    boolean isInDebugLayout = false;
    Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    StringBuilder measureLogBuilder = new StringBuilder();
    Rect viewVisibleRectTemp = new Rect();
    /**
     * 已经按下返回键
     */
    private boolean isBackPress = false;

    private int[] mInsets = new int[4];
    /**
     * 锁定高度, 当键盘弹出的时候, 可以不改变size
     */
    private boolean lockHeight = false;
    private float mTranslationOffsetX;
    /**
     * 如果只剩下最后一个View, 是否激活滑动删除
     */
    private boolean enableRootSwipe = false;
    /**
     * 是否正在拖拽返回.
     */
    private boolean isSwipeDrag = false;

    /**
     * 是否需要滑动返回, 如果正在滑动返回,则阻止onLayout的进行
     */
    private boolean isWantSwipeBack = false;

    /**
     * 三指首次按下的时间
     */
    private long firstDownTime = 0;
    /**
     * 拦截所有touch事件
     */
    private boolean interceptTouchEvent = false;
    /**
     * 覆盖在的所有IView上的Drawable
     */
    private Drawable overlayDrawable;
    /**
     * 高度使用DecorView的高度, 否则使用View的高度
     */
    private boolean isFullOverlayDrawable = false;

    /**
     * 触发滑动的时候, 是否隐藏键盘
     */
    private boolean hideSoftinputOnSwipe = false;

    public FragmentSwipeBackLayout(Context context) {
        super(context);
        initLayout();
    }

    public FragmentSwipeBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    /**
     * inflate之后, 有时会返回 父布局, 这个时候需要处理一下, 才能拿到真实的RootView.
     */
    public static View safeAssignView(final View parentView, final View childView) {
        if (parentView == childView) {
            if (parentView instanceof ViewGroup) {
                final ViewGroup viewGroup = (ViewGroup) parentView;
                return viewGroup.getChildAt(viewGroup.getChildCount() - 1);
            }
            return childView;
        } else {
            return childView;
        }
    }

    public static void saveToSDCard(final String data) {

    }

    public static String name(Object obj) {
        if (obj == null) {
            return "null object";
        }
        if (obj instanceof String) {
            return "String:" + obj;
        }
        return obj.getClass().getSimpleName();
    }

    FragmentManager fragmentManager;

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    /**
     * 查找锚点处, 最近一个有效的 Fragment
     * <p>
     * 如果锚点为null, 那么查找最后一个有效的Fragment
     */
    public Fragment findLastFragment(@Nullable Fragment anchor) {
        if (fragmentManager == null) {
            return null;
        }
        boolean isFindAnchor = anchor == null;

        List<Fragment> fragments = fragmentManager.getFragments();

        Fragment fragment = null;
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment f = fragments.get(i);
            if (isFindAnchor) {
                if (f.isAdded() && f.getView() != null) {
                    fragment = f;
                    break;
                }
            } else {
                isFindAnchor = anchor == f;
            }
        }
        return fragment;
    }

    public Fragment findFragment(@Nullable View view) {
        if (fragmentManager == null || view == null) {
            return null;
        }

        List<Fragment> fragments = fragmentManager.getFragments();

        Fragment fragment = null;
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment f = fragments.get(i);
            if (f.isAdded() && f.getView() != null && f.getView() == view) {
                fragment = f;
                break;
            }
        }
        return fragment;
    }

    /**
     * 获取有效Fragment的数量
     */
    public int getFragmentsCount() {
        if (fragmentManager == null) {
            return 0;
        }
        int count = 0;

        List<Fragment> fragments = fragmentManager.getFragments();

        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment f = fragments.get(i);
            if (f.isAdded() && f.getView() != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * 滑动返回处理
     */
    @Override
    protected boolean canTryCaptureView(View child) {
        if (fragmentManager == null) {
            return false;
        }

        Fragment lastFragment = findLastFragment(null);
        if (isBackPress ||
                lastFragment == null ||
                mViewDragState != ViewDragHelper.STATE_IDLE) {
            return false;
        }

        if (getScreenOrientation() != Configuration.ORIENTATION_PORTRAIT) {
            //非竖屏, 禁用滑动返回
            return false;
        }

        if (lastFragment instanceof IFragment) {
            if (!((IFragment) lastFragment).canSwipeBack()) {
                return false;
            }
        }

        if (getFragmentsCount() <= 0) {
            return false;
        }

        if (getFragmentsCount() > 1) {
            if (lastFragment.getView() == child) {
                if (hideSoftinputOnSwipe) {
                    hideSoftInput();
                }
                return true;
            } else {
                return false;
            }
        } else if (enableRootSwipe) {
            if (hideSoftinputOnSwipe) {
                hideSoftInput();
            }
            return true;
        }
        return false;
    }

    public void setEnableRootSwipe(boolean enableRootSwipe) {
        this.enableRootSwipe = enableRootSwipe;
    }

    private void initLayout() {
        if (getContext() instanceof FragmentActivity) {
            fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
        }

        if (getContext() instanceof BaseAppCompatActivity) {
            ((BaseAppCompatActivity) getContext()).setFragmentSwipeBackLayout(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //setFocusable(true);
        //setFocusableInTouchMode(true);
        //boolean old = this.isAttachedToWindow;
        this.isAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isAttachedToWindow) {
            isAttachedToWindow = false;
        }
    }

    /**
     * 为了确保任务都行执行完了, 延迟打印堆栈信息
     */
    private void printLog() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                logLayoutInfo();
            }
        }, 16);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mInsets[0] = insets.getSystemWindowInsetLeft();
            mInsets[1] = insets.getSystemWindowInsetTop();
            mInsets[2] = insets.getSystemWindowInsetRight();
            mInsets[3] = insets.getSystemWindowInsetBottom();

            return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
                    insets.getSystemWindowInsetRight(), lockHeight ? 0 : insets.getSystemWindowInsetBottom()));
        } else {
            return super.onApplyWindowInsets(insets);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private int getDebugWidthSize() {
        return getMeasuredWidth() - 2 * hSpace;
    }

    private int getDebugHeightSize() {
        return getMeasuredHeight() - 4 * vSpace;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //of java
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //of kotlin
//        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        int count = getChildCount();
        if (isInDebugLayout) {
            //int hCount = count > 9 ? 4 : (count > 6 ? 3 : 2);//横向放3个
            //int vCount = (int) Math.max(2, Math.ceil(count * 1f / hCount));//竖向至少2行

            //int wSize = (getMeasuredWidth() - (hCount + 1) * hSpace) / hCount;
            //int hSize = (getMeasuredHeight() - (vCount + 1) * vSpace) / vCount;
            int wSize = widthSize;//getDebugWidthSize();
            int hSize = heightSize;//getDebugHeightSize();

            for (int i = 0; i < count; i++) {
                View childAt = getChildAt(i);
                childAt.setVisibility(VISIBLE);
                childAt.measure(ViewExKt.exactlyMeasure(this, wSize), ViewExKt.exactlyMeasure(this, hSize));
            }

            setMeasuredDimension(widthSize, heightSize);
        } else {
            if (showDebugInfo) {
                Debug.logTimeStartD("\n开始测量, 共:" + getFragmentsCount());
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (showDebugInfo) {
                Debug.logTimeEndD("\n测量结束");
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //L.e("debug layout 1 " + isInDebugLayout + " " + getScrollX() + " " + getScrollY());
        if (isInDebugLayout) {
            int count = getChildCount();

//            int l = hSpace;
//            int t = vSpace;

            int l = getPaddingLeft();
            int t = -vSpace + getPaddingTop();

            int wSize = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//getDebugWidthSize();
            int hSize = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();//getDebugHeightSize();

            for (int i = 0; i < count; i++) {
                View childAt = getChildAt(i);
                childAt.layout(l, t, l + wSize, t + hSize);
                t += getDebugHeightSize() + vSpace;
//                t += hSize + vSpace;
            }
//            viewMaxHeight = t;
            viewMaxHeight = t + 2 * vSpace;
            return;
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    public void setLockHeight(boolean lockHeight) {
        this.lockHeight = lockHeight;
    }

    /**
     * 获取底部装饰物的高度 , 通常是键盘的高度
     */
    public int getInsersBottom() {
        return mInsets[3];
    }

    public void hideSoftInput() {
        if (isSoftKeyboardShow()) {
            InputMethodManager manager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    /**
     * 判断键盘是否显示
     */
    private boolean isSoftKeyboardShow() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int keyboardHeight = getSoftKeyboardHeight();
        return screenHeight != keyboardHeight && keyboardHeight > 100;
    }

    /**
     * 获取键盘的高度
     */
    private int getSoftKeyboardHeight() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        Rect rect = new Rect();
        getWindowVisibleDisplayFrame(rect);
        int visibleBottom = rect.bottom;
        return screenHeight - visibleBottom;
    }

    public void showSoftInput(View view) {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(view, 0);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            Fragment lastFragment = findLastFragment(null);
            if (lastFragment != null) {
                View view = null;
                if (L.LOG_DEBUG) {
                    ViewGroup targetViewGroup = null;
                    View lastFragmentView = lastFragment.getView();
                    if (lastFragmentView instanceof ViewGroup) {
                        targetViewGroup = (ViewGroup) lastFragmentView;
                    }

                    if (targetViewGroup != null) {
                        view = ViewGroupExKt.findView(targetViewGroup, ev.getRawX(), ev.getRawY());
                        StringBuilder builder = new StringBuilder("\ntouch on->");
                        if (view == null) {
                            builder.append("null");
                        } else {
                            view.getGlobalVisibleRect(viewVisibleRectTemp);
                            builder.append(viewVisibleRectTemp);
                            builder.append("#");
                            if (view instanceof TextView) {
                                builder.append(((TextView) view).getText());
                                builder.append("#");
                            } else if (view instanceof RecyclerView) {
                                builder.append(((RecyclerView) view).getAdapter());
                                builder.append("#");
                                builder.append(((RecyclerView) view).getLayoutManager());
                                builder.append("#");
                            }
                            if (view.hasOnClickListeners()) {
                                builder.append("$");
                            }
                            builder.append(view);
                        }
                        L.d(builder.toString());
                    } else {
                        L.d("\ntargetViewGroup is null.");
                    }
                }

                if (lastFragment instanceof IFragment) {
                    if (((IFragment) lastFragment).hideSoftInputOnTouchDown(view)) {
                        hideSoftInput();
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();

        if (handleDebugLayout(ev)) {
            return true;
        }

        if (isInDebugLayout) {
            return true;
        }

        if (needInterceptTouchEvent()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 返回是否需要拦截Touch事件
     */
    public boolean needInterceptTouchEvent() {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleDebugLayout(event);
        if (isInDebugLayout) {
            getOrientationGestureDetector().onTouchEvent(event);
        } else {
            super.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 多点按下, 是否处理
     */
    protected boolean handleDebugLayout(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        long downTime = ev.getDownTime();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            firstDownTime = downTime;
        }

        if (L.LOG_DEBUG &&
                showDebugLayout &&
                actionMasked == MotionEvent.ACTION_POINTER_DOWN &&
                ev.getPointerCount() == 6) {

            if (ev.getEventTime() - firstDownTime < 500) {
                //快速三指按下才受理操作

                //debug模式下, 三指按下
                if (isInDebugLayout) {
                    closeDebugLayout();
                } else {
                    startDebugLayout();
                }
                return true;
            }
        }
        return false;
    }

    public void finishActivity() {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).finish();
            ((Activity) getContext()).overridePendingTransition(0, 0);
        }
    }

    /**
     * 滚动到关闭状态
     */
    @Override
    protected void onRequestClose() {
        super.onRequestClose();
        translation(0);
        if (enableRootSwipe && getFragmentsCount() == 1) {
            finishActivity();
        } else {
            Fragment lastFragment = findLastFragment(null);
            if (lastFragment != null) {
                //lastFragment.mView.setAlpha(0f);
                swipeBackFragment(lastFragment);
            }
        }
    }

    /**
     * 默认状态
     */
    @Override
    protected void onRequestOpened() {
        super.onRequestOpened();
        isSwipeDrag = false;
        translation(0);
        printLog();
    }

    @Override
    protected void onSlideChange(float percent) {
        super.onSlideChange(percent);
        isSwipeDrag = true;
        translation(percent);
    }

    @Override
    protected void onStateIdle() {
        super.onStateIdle();
        isWantSwipeBack = false;
    }

    /**
     * 滑动中
     */
    @Override
    protected void onStateDragging() {
        super.onStateDragging();
        isWantSwipeBack = true;
        isSwipeDrag = true;

        //开始偏移时, 偏移的距离
        translation(-100f, mTranslationOffsetX = getMeasuredWidth() * 0.3f);
    }

    private void translation(float percent /*如果为0, 表示滑动关闭了*/) {
        translation(percent, 0);
    }

    private void translation(float percent /*如果为0, 表示滑动关闭了*/, float translationX /*强制偏移, percent=-100生效*/) {
        final Fragment preFragment = findLastFragment(findLastFragment(null));
        if (preFragment != null) {
            View preFragmentView = preFragment.getView();

            if (preFragmentView != null) {
                float tx;
                if (((int) percent) == -100) {
                    tx = translationX;
                } else {
                    tx = -mTranslationOffsetX * percent;
                }
                if (preFragmentView.getVisibility() == View.GONE) {
                    preFragmentView.setVisibility(VISIBLE);
                }
                if (preFragmentView.getTranslationX() != tx) {
                    preFragmentView.setTranslationX(tx);
                }
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSwipeDrag() {
        return isSwipeDrag;
    }

    /**
     * 打印堆栈信息
     */
    public String logLayoutInfo() {
//        StringBuilder stringBuilder = new StringBuilder(name(this) + " IViews:\n");
//        for (int i = 0; i < getAttachViewSize(); i++) {
//            Fragment Fragment = mAttachViews.get(i);
//            stringBuilder.append(i);
//            stringBuilder.append("-->");
//            stringBuilder.append(name(Fragment.mIView));
//            stringBuilder.append("");
//            int visibility = Fragment.mView.getVisibility();
//            String vis;
//            if (visibility == View.GONE) {
//                vis = "GONE";
//            } else if (visibility == View.VISIBLE) {
//                vis = "VISIBLE";
//            } else if (visibility == View.INVISIBLE) {
//                vis = "INVISIBLE";
//            } else {
//                vis = "NONE";
//            }
//            stringBuilder.append(" visibility-->");
//            stringBuilder.append(vis);
//            stringBuilder.append(" alpha-->");
//            stringBuilder.append(Fragment.mView.getAlpha());
//            stringBuilder.append(" isIViewHide-->");
//            stringBuilder.append(Fragment.isIViewHide);
//            stringBuilder.append(" W:");
//            stringBuilder.append(this.getMeasuredWidth());
//            stringBuilder.append("-");
//            stringBuilder.append(Fragment.mView.getMeasuredWidth());
//            stringBuilder.append(" H:");
//            stringBuilder.append(this.getMeasuredHeight());
//            stringBuilder.append("-");
//            stringBuilder.append(Fragment.mView.getMeasuredHeight());
//            stringBuilder.append(" R:");
//            stringBuilder.append(Fragment.mView.getRight());
//            stringBuilder.append(" B:");
//            stringBuilder.append(Fragment.mView.getBottom());
//            stringBuilder.append(" needLayout:");
//            stringBuilder.append(Fragment.mView.getTag(R.id.tag_need_layout));
//            stringBuilder.append("\n");
//        }
//        LAYOUT_INFO = stringBuilder.toString();
//        L.e(LAYOUT_INFO);
//        saveToSDCard(LAYOUT_INFO);
//        return LAYOUT_INFO;

        FragmentHelper.logFragments(fragmentManager);
        return "";
    }

    /**
     * 滑动返回的形式, 关闭一个Fragment
     */
    public void swipeBackFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        if (fragmentManager == null) {
            return;
        }
        FragmentHelper.build(fragmentManager)
                .showFragment(findLastFragment(fragment))
                .remove(fragment)
                .noAnim()
                .doIt();
    }

    public void startDebugLayout() {
        if (!isInDebugLayout) {
            isInDebugLayout = true;
            getOverScroller().abortAnimation();
            requestLayout();
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                //childAt.startAnimation(AnimationUtils.loadAnimation(mLayoutActivity, R.anim.base_scale_to_min));
                AnimUtil.scaleBounceView(childAt, getDebugWidthSize() * 1f / getMeasuredWidth(), getDebugHeightSize() * 1f / getMeasuredHeight());
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollTo(0, Integer.MAX_VALUE);//滚动到最后一个IView
                }
            }, 16);
        }
    }

    public void closeDebugLayout() {
        if (isInDebugLayout) {
            isInDebugLayout = false;
            getOverScroller().abortAnimation();
            scrollTo(0, 0);//恢复滚动坐标
            requestLayout();
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                //childAt.startAnimation(AnimationUtils.loadAnimation(mLayoutActivity, R.anim.base_scale_to_max));
                AnimUtil.scaleBounceView(childAt);
            }
        }
    }

    @Override
    protected void drawSwipeLine(Canvas canvas) {
        if (!isInDebugLayout) {
            super.drawSwipeLine(canvas);
        }
    }

    @Override
    protected void drawDimStatusBar(Canvas canvas) {
        if (!isInDebugLayout) {
            super.drawDimStatusBar(canvas);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        int maxScrollY = viewMaxHeight - getMeasuredHeight();
        if (y > maxScrollY) {
            y = maxScrollY;
        }
        if (y < 0) {
            y = 0;
        }
        super.scrollTo(x, y);
    }

    @Override
    public void onFlingChange(@NotNull ORIENTATION orientation, float velocity) {
        super.onFlingChange(orientation, velocity);
        if (isInDebugLayout && isVertical(orientation)) {
            if (velocity > 1000) {
                //快速向下滑动
                startFlingY(-(int) velocity, getScrollY());
            } else if (velocity < -1000) {
                //快速向上滑动
                startFlingY(-(int) velocity, viewMaxHeight);
            }
        }
    }

    private void initDebugPaint() {
        debugPaint.setStrokeJoin(Paint.Join.ROUND);
        debugPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        debugPaint.setStrokeCap(Paint.Cap.ROUND);
        debugPaint.setTextSize(12 * getResources().getDisplayMetrics().density);
        debugPaint.setColor(Color.WHITE);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        /*调试模式绘制*/
        if (isInDebugLayout) {
            initDebugPaint();
            int childCount = getChildCount();

            int l = hSpace;
            int t = vSpace;

            int wSize = getDebugWidthSize();
            int hSize = getDebugHeightSize();

            for (int i = 0; i < childCount; i++) {
                View childAt = getChildAt(i);

                Fragment fragmentByView = findFragment(childAt);

                if (fragmentByView == null) {
                    continue;
                }

                float textHeight = ViewExKt.textHeight(this, debugPaint);

                float dp2 = 1 * density();
                debugPaint.setShadowLayer(dp2, dp2, dp2, Color.BLACK);

                measureLogBuilder.delete(0, measureLogBuilder.length());
                measureLogBuilder.append(RUtils.getClassSimpleName(fragmentByView.getClass()));
                measureLogBuilder.append(" ");
                FragmentHelper.logFragmentStatus(fragmentByView, measureLogBuilder);

                canvas.drawText(measureLogBuilder.toString(), hSpace, t + textHeight, debugPaint);

                t += hSize + vSpace;
            }
        }

        /*全屏覆盖绘制Drawable*/
        if (overlayDrawable != null) {
            Context context = getContext();
            int screenHeight = getMeasuredHeight();

            if (isFullOverlayDrawable) {
                if (context instanceof Activity) {
                    screenHeight = ((Activity) context).getWindow().getDecorView().getMeasuredHeight();
                }
            }

            overlayDrawable.setBounds(0, 0, getMeasuredWidth(), screenHeight);
            overlayDrawable.draw(canvas);
        }
    }

    @Override
    public void onScrollChange(@NotNull ORIENTATION orientation, float distance) {
        super.onScrollChange(orientation, distance);
        if (isInDebugLayout && isVertical(orientation)) {
            scrollBy(0, (int) distance);
        }
    }

    public void setInterceptTouchEvent(boolean interceptTouchEvent) {
        this.interceptTouchEvent = interceptTouchEvent;
    }

    public void setOverlayDrawable(Drawable overlayDrawable) {
        this.overlayDrawable = overlayDrawable;
        postInvalidate();
    }

    public void setFullOverlayDrawable(boolean fullOverlayDrawable) {
        isFullOverlayDrawable = fullOverlayDrawable;
    }

    private void logTimeStart(String log) {
        if (SHOW_DEBUG_TIME) {
            Debug.logTimeStartI(log);
        }
    }

    private void logTimeEnd(String log) {
        if (SHOW_DEBUG_TIME) {
            Debug.logTimeEndI(log);
        }
    }

    public boolean isInDebugLayout() {
        return isInDebugLayout;
    }
}