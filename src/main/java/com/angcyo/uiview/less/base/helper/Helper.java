package com.angcyo.uiview.less.base.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/15
 */
public class Helper {
    public static ActivityHelper.Builder activity(@NonNull Context context) {
        return ActivityHelper.build(context);
    }

    public static FragmentHelper.Builder fragment(@NonNull FragmentManager fragmentManager) {
        return FragmentHelper.build(fragmentManager);
    }

    public static ItemInfoHelper.Builder itemInfo(@NonNull View view) {
        return ItemInfoHelper.build(view);
    }

    public static TitleItemHelper.Builder titleItem(@NonNull Context context) {
        return TitleItemHelper.build(context);
    }

    public static ViewGroupHelper view(View parentView) {
        return ViewGroupHelper.build(parentView);
    }
}
