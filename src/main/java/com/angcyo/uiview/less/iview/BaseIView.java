package com.angcyo.uiview.less.iview;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/16
 */
public abstract class BaseIView {
    protected View rootView;

    public View createView(@NonNull Context context, @LayoutRes int id, @Nullable ViewGroup parent) {
        rootView = LayoutInflater.from(context).inflate(id, parent, false);

        return rootView;
    }
}
