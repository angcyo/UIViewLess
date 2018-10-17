package com.angcyo.uiview.less.iview;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import com.angcyo.uiview.less.resources.AnimUtil;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/17
 */
public class ManagerIView {

    /**
     * 默认透明过渡动画
     */
    public static void replaceIView(@NonNull Activity activity, @NonNull BaseIView baseIView) {
        replaceIView(activity, baseIView,
                AnimUtil.createAlphaEnterAnim(0.5f), AnimUtil.createAlphaExitAnim(0f));
    }

    public static void replaceIView(@NonNull Activity activity, @NonNull BaseIView baseIView,
                                    @Nullable Animation startAnimation, @Nullable Animation finishAnimation) {
        View contentView = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        if (contentView instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) contentView;

            /**
             * 为什么要先 addView, 再 removeView?
             * 如果界面之前有EditText, 并且显示了简单, 如果先removeView, 键盘会消失.
             * 但是, 如果先addView(默认需要显示键盘), 在removeView, 键盘状态会保持.
             *
             * */
            View rootView = baseIView.createView(activity, viewGroup, null);
            if (startAnimation != null) {
                rootView.startAnimation(startAnimation);
            }

            final int childCount = viewGroup.getChildCount();//已经包含了之前创建的view
            if (finishAnimation != null && childCount >= 2) {
                //至少有2个child view, 才能执行 finishAnimation
                View finishView = viewGroup.getChildAt(childCount - 2);
                finishAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        (viewGroup).removeViews(0, childCount - 1);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                finishView.startAnimation(finishAnimation);
            } else {
                (viewGroup).removeViews(0, childCount - 1);
            }
        }
    }
}
