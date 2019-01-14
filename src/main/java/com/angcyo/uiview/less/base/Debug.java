package com.angcyo.uiview.less.base;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.angcyo.uiview.less.BuildConfig;

/**
 * Created by angcyo on 2018/12/02 15:38
 */
public class Debug {
    public static void addDebugTextView(Activity activity) {
        if (BuildConfig.DEBUG && activity != null) {
            //添加一个TextView,用来提示当前的Activity类
            View decorView = activity.getWindow().getDecorView();
            View rootView = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
            final String tag = "addDebugTextView";
            View debugTextView = decorView.findViewWithTag(tag);
            if (debugTextView != null) {
                return;
            }
            if (decorView instanceof FrameLayout) {
                TextView textView = new TextView(activity);
                textView.setTag(tag);
                textView.setTextSize(9);
                textView.setTextColor(Color.WHITE);
                float dp2 = 1 * activity.getResources().getDisplayMetrics().density;
                int padding = (int) dp2 * 4;
                textView.setPadding(padding, padding, padding, padding);
                textView.setShadowLayer(dp2 * 2, dp2, dp2, Color.BLACK);

                textView.setText(activity.getClass().getSimpleName());

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
                layoutParams.gravity = Gravity.BOTTOM;
                if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                        && decorView.getBottom() > rootView.getBottom()) {
                    //显示了导航栏
                    Resources resources = activity.getResources();
                    int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                    int navBarHeight = 0;
                    if (resourceId > 0) {
                        navBarHeight = resources.getDimensionPixelSize(resourceId);
                    }
                    layoutParams.bottomMargin = navBarHeight;
                }
                ((ViewGroup) decorView).addView(textView, layoutParams);
            }
        }
    }
}
