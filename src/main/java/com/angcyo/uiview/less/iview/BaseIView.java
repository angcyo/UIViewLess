package com.angcyo.uiview.less.iview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/16
 */
public abstract class BaseIView {
    protected View rootView;
    protected RBaseViewHolder baseViewHolder;

    /**
     * 1.创建根布局
     *
     * @param context 上下文
     * @param id      根布局id
     * @param parent  是否需要attach到parent
     * @param state   初始化的状态参数
     */
    public View createView(@NonNull Context context, @LayoutRes int id, @Nullable ViewGroup parent, @Nullable Bundle state) {
        rootView = LayoutInflater.from(context).inflate(id, parent, false);
        if (parent != null) {
            parent.addView(rootView);
        }
        baseViewHolder = new RBaseViewHolder(rootView);

        initIView(state);
        return rootView;
    }

    /**
     * 1.1 初始化IView
     *
     * @param state {@link #createView(Context, int, ViewGroup, Bundle)}
     */
    protected void initIView(@Nullable Bundle state) {

    }
}
