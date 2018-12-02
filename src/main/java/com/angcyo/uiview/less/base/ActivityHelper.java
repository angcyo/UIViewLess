package com.angcyo.uiview.less.base;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.angcyo.uiview.less.kotlin.ExKt;
import org.jetbrains.annotations.NotNull;

/**
 * Created by angcyo on 2018/12/02 19:21
 */
public class ActivityHelper {

    /**
     * 设置状态栏背景颜色
     */
    public static void setStatusBarColor(@NotNull Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }

    /**
     * 设置状态栏背景
     */
    public static void setStatusBarDrawable(@NotNull final Activity activity, final Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int identifier = activity.getResources().getIdentifier("statusBarBackground", "id", "android");
            View statusBarView = activity.getWindow().findViewById(identifier);
            if (statusBarView != null) {
                ViewCompat.setBackground(statusBarView, drawable);
            } else {
                activity.getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        activity.getWindow().getDecorView().removeOnLayoutChangeListener(this);
                        setStatusBarDrawable(activity, drawable);
                    }
                });
            }
        }
    }

    /**
     * 是否是白色状态栏. 如果是, 那么系统的状态栏字体会是灰色
     */
    public static void lightStatusBar(@NotNull Activity activity, boolean light) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int systemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            if (light) {
                if (ExKt.have(systemUiVisibility, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                    return;
                }
                activity.getWindow()
                        .getDecorView()
                        .setSystemUiVisibility(
                                systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                if (!ExKt.have(systemUiVisibility, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                    return;
                }
                activity.getWindow()
                        .getDecorView()
                        .setSystemUiVisibility(
                                systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    public static void enableLayoutFullScreen(@NotNull Activity activity, boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public static boolean isLayoutFullScreen(@NotNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            return ExKt.have(window.getDecorView().getSystemUiVisibility(), View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            return false;
        }
    }

    /**
     * 请在 {@link Activity#setContentView(View)} 之前调用
     * 低版本系统, 可能需要在 {@link Activity#onCreate(Bundle)} 之前调用
     */
    public static void setNoTitleNoActionBar(@NotNull Activity activity) {
        activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
        android.app.ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}
