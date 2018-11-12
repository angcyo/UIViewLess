package com.angcyo.uiview.less.iview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.AnimUtil;

/**
 * 可以在Activity 中, 弹出自定义布局, 并且控制
 * <p>
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/17
 */
public class StateUI {
    static int DEFAULT_START_COLOR = Color.TRANSPARENT;
    static int DEFAULT_END_COLOR = Color.parseColor("#60000000");

    static Animation defaultStartAnimation = AnimUtil.translateAlphaStartAnimation();
    static Animation defaultFinishAnimation = AnimUtil.translateAlphaFinishAnimation();

    ViewGroup parent;
    Context context;
    boolean canCancelOnTouchOutside = false;

    Animation startAnimation = defaultStartAnimation;
    Animation finishAnimation = defaultFinishAnimation;

    int bgStartColor = DEFAULT_START_COLOR;
    int bgEndColor = DEFAULT_END_COLOR;

    int layoutId;

    OnStateInitListener listener;

    public RBaseViewHolder baseViewHolder;

    public StateUI(@NonNull ViewGroup parent, @LayoutRes int layoutId) {
        this.parent = parent;
        this.layoutId = layoutId;
        context = parent.getContext();
    }

    public static StateUI custom(@NonNull Activity activity, @LayoutRes int layoutId) {
        View decorView = activity.getWindow().getDecorView();
        View viewWithTag = decorView.findViewWithTag(layoutId);
        if (viewWithTag != null) {
            return (StateUI) viewWithTag.getTag(R.id.tag_state_ui);
        }
        return new StateUI((ViewGroup) decorView, layoutId);
    }

    /**
     * 请调用 {@link #show()} 方法, 才能显示
     */
    public static StateUI flow(@NonNull Activity activity) {
//        View decorView = activity.getWindow().getDecorView();
        //View contentView = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);

        //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //int statusBarHeight = contentView.getTop();//小米8se 110
        //int navigationBarHeight = decorView.getMeasuredHeight() - contentView.getMeasuredHeight();//小米8se 130

//        return new StateUI((ViewGroup) decorView);
        return custom(activity, R.layout.base_flow_loading_layout);
    }

    /**
     * @return true 表示window中没有添加状态显示布局
     */
    public static boolean hide(@NonNull Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        if (decorView instanceof ViewGroup) {
            //一层一层隐藏
            int childCount = ((ViewGroup) decorView).getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View childAt = ((ViewGroup) decorView).getChildAt(i);
                if (childAt instanceof StateLayout) {
                    StateUI stateUI = (StateUI) childAt.getTag(R.id.tag_state_ui);
                    stateUI.hide();
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    /**
     * 是否还有布局在显示
     */
    public static boolean haveStateUI(@NonNull Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        if (decorView instanceof ViewGroup) {
            int childCount = ((ViewGroup) decorView).getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View childAt = ((ViewGroup) decorView).getChildAt(i);
                if (childAt instanceof StateLayout) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private void hideSoftInputFromWindow() {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(parent.getWindowToken(), 0);
    }

    public StateUI setInitListener(OnStateInitListener listener) {
        this.listener = listener;
        return this;
    }

    public StateUI show() {
        if (baseViewHolder != null) {
            return this;
        }

        hideSoftInputFromWindow();

        LayoutInflater inflater = LayoutInflater.from(context);

        //套一层
        StateLayout overlay = new StateLayout(context);
        baseViewHolder = new RBaseViewHolder(overlay);
        overlay.setTag(R.id.tag_state_ui, this);

        overlay.setTag(layoutId);
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canCancelOnTouchOutside) {
                    hide();
                }
            }
        });
        AnimUtil.startArgb(overlay, bgStartColor, bgEndColor, 300);

        inflater.inflate(layoutId, overlay);

        if (listener != null) {
            listener.onStateInit(this);
        }

        if (parent instanceof FrameLayout) {
            parent.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        } else if (parent instanceof ConstraintLayout) {
            parent.addView(overlay, new ConstraintLayout.LayoutParams(-1, -1));
        }

        if (startAnimation != null) {
            overlay.getChildAt(0).startAnimation(startAnimation);
        }

        return this;
    }

    public void hide() {
        final View view = parent.findViewWithTag(layoutId);
        if (view instanceof ViewGroup) {
            if (finishAnimation == null) {
                parent.removeView(view);
            } else {
                finishAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        parent.removeView(view);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                AnimUtil.startArgb(view, bgEndColor, bgStartColor, 300);
                ((ViewGroup) view).getChildAt(0).startAnimation(finishAnimation);
            }
        }
    }

    public interface OnStateInitListener {
        void onStateInit(StateUI stateUI);
    }

    private static class StateLayout extends FrameLayout {

        public StateLayout(@NonNull Context context) {
            super(context);
        }
    }
}
