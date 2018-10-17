package com.angcyo.uiview.less.iview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.resources.AnimUtil;

/**
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
    String tip = "请稍等...";
    boolean canCancelOnTouchOutside = false;

    Animation startAnimation = defaultStartAnimation;
    Animation finishAnimation = defaultFinishAnimation;

    int bgStartColor = DEFAULT_START_COLOR;
    int bgEndColor = DEFAULT_END_COLOR;

    public StateUI(@NonNull ViewGroup parent) {
        this.parent = parent;
        context = parent.getContext();
    }

    public static StateUI flow(@NonNull Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        //View contentView = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);

        //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //int statusBarHeight = contentView.getTop();//小米8se 110
        //int navigationBarHeight = decorView.getMeasuredHeight() - contentView.getMeasuredHeight();//小米8se 130

        return new StateUI((ViewGroup) decorView);
    }

    /**
     * @return true 表示window中没有添加状态显示布局
     */
    public static boolean hide(@NonNull Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        if (decorView instanceof ViewGroup) {
            final View view = decorView.findViewWithTag(R.layout.base_flow_loading_layout);
            if (view instanceof ViewGroup) {
                Animation finishAnimation = defaultFinishAnimation;
                finishAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ((ViewGroup) decorView).removeView(view);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                AnimUtil.startArgb(view, DEFAULT_END_COLOR, DEFAULT_START_COLOR, 300);
                ((ViewGroup) view).getChildAt(0).startAnimation(finishAnimation);

                return false;
            }
        }

        return true;
    }

    public StateUI setTipText(String tip) {
        this.tip = tip;
        initLayout();
        return this;
    }

    private void hideSoftInputFromWindow() {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(parent.getWindowToken(), 0);
    }

    private void initLayout() {
        if (parent == null) {
            return;
        }

        final View view = parent.findViewWithTag(R.layout.base_flow_loading_layout);
        if (view != null) {
            ((TextView) view.findViewById(R.id.base_load_tip_view)).setText(tip);
        }
    }

    public StateUI show() {
        initLayout();

        hideSoftInputFromWindow();

        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout overlay = new FrameLayout(context);
        overlay.setTag(R.layout.base_flow_loading_layout);
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canCancelOnTouchOutside) {
                    hide();
                }
            }
        });
        AnimUtil.startArgb(overlay, bgStartColor, bgEndColor, 300);

        inflater.inflate(R.layout.base_flow_loading_layout, overlay);
        ((TextView) overlay.findViewById(R.id.base_load_tip_view)).setText(tip);

        overlay.getChildAt(0).startAnimation(startAnimation);

        if (parent instanceof FrameLayout) {
            parent.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        } else if (parent instanceof ConstraintLayout) {
            parent.addView(overlay, new ConstraintLayout.LayoutParams(-1, -1));
        }

        return this;
    }

    public void hide() {
        final View view = parent.findViewWithTag(R.layout.base_flow_loading_layout);
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

}
