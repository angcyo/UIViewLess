package com.angcyo.uiview.less.utils;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import com.angcyo.lib.L;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/17
 */
public class DecorUtil {

    /**
     * 请勿在dialog中使用
     * <p>
     * 主题的 android:windowTranslucentStatus 属性, 会影响 contentView 的 padding top.
     * <p>
     * 如果设置了 View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN , 那么 contentView 的 padding top 都是 0
     */
    public static void demo(@NonNull final Window window) {
        final View decorView = window.getDecorView();
        int measuredHeight = decorView.getMeasuredHeight();
        if (measuredHeight <= 0) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decorView.getViewTreeObserver().removeOnPreDrawListener(this);

                    demo(window);
                    return true;
                }
            });
        } else {
            Rect outRect = new Rect();
            decorView.getWindowVisibleDisplayFrame(outRect);
            L.w("可视区域:" + outRect);

            L.w("屏幕高度:" + measuredHeight);
            if (decorView instanceof ViewGroup) {
                int childCount = ((ViewGroup) decorView).getChildCount();
                if (childCount > 0) {
                    View contentView = ((ViewGroup) decorView).getChildAt(0);
                    L.w("内容高度:" + contentView.getMeasuredHeight() + " p:" + contentView.getPaddingTop());
                }
                if (childCount > 1) {
                    View childView = ((ViewGroup) decorView).getChildAt(1);
                    if (isStatusBar(decorView, childView)) {
                        L.w("状态栏高度:" + childView.getMeasuredHeight());
                    } else if (isNavigationBar(decorView, childView)) {
                        L.w("导航栏高度:" + childView.getMeasuredHeight());
                    } else {
                        L.w("未知:" + childView);
                    }
                }
                if (childCount > 2) {
                    View childView = ((ViewGroup) decorView).getChildAt(2);
                    if (isStatusBar(decorView, childView)) {
                        L.w("状态栏高度:" + childView.getMeasuredHeight());
                    } else if (isNavigationBar(decorView, childView)) {
                        L.w("导航栏高度:" + childView.getMeasuredHeight());
                    } else {
                        L.w("未知:" + childView);
                    }
                }
            }
        }
    }

    private static boolean isStatusBar(@NonNull View decorView, @NonNull View childView) {
        if (childView.getTop() == 0 &&
                childView.getMeasuredWidth() == decorView.getMeasuredWidth() &&
                childView.getBottom() < decorView.getBottom()
                ) {
            return true;
        }
        return false;
    }

    private static boolean isNavigationBar(@NonNull View decorView, @NonNull View childView) {
        if (childView.getTop() > decorView.getTop() &&
                childView.getMeasuredWidth() == decorView.getMeasuredWidth() &&
                childView.getBottom() == decorView.getBottom()
                ) {
            return true;
        }
        return false;
    }
}
