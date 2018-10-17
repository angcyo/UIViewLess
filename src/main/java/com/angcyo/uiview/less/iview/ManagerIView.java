package com.angcyo.uiview.less.iview;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/17
 */
public class ManagerIView {
    public static void replaceIView(@NonNull Activity activity, @NonNull BaseIView baseIView) {
        View contentView = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        if (contentView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) contentView;

            /**
             * 为什么要先 addView, 再 removeView?
             * 如果界面之前有EditText, 并且显示了简单, 如果先removeView, 键盘会消失.
             * 但是, 如果先addView(默认需要显示键盘), 在removeView, 键盘状态会保持.
             *
             * */
            baseIView.createView(activity, viewGroup, null);
            (viewGroup).removeViews(0, viewGroup.getChildCount() - 1);
        }
    }
}
