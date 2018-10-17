package com.angcyo.uiview.less.iview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.angcyo.uiview.less.R;
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
    protected Context context;

    public static BaseIView from(@Nullable View view) {
        if (view == null) {
            return null;
        }
        Object tag = view.getTag(R.id.tag_base_iview);
        if (tag instanceof BaseIView) {
            return (BaseIView) tag;
        }
        return null;
    }

    /**
     * @return 根布局id
     */
    protected int getLayoutId() {
        return -1;
    }

    /**
     * @return 用代码的方式, 创建布局
     */
    protected View createRootView() {
        return new View(context);
    }

    public Activity getActivity() {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    /**
     * 1.创建根布局
     *
     * @param context 上下文
     * @param parent  是否需要attach到parent
     * @param state   初始化的状态参数
     */
    public View createView(@NonNull Context context, @Nullable ViewGroup parent, @Nullable Bundle state) {
        this.context = context;

        int layoutId = getLayoutId();
        if (layoutId == -1) {
            rootView = createRootView();
        } else {
            rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }

        //将 BaseIView 对象和 View 关联.
        rootView.setTag(R.id.tag_base_iview, this);

        if (parent != null) {
            parent.addView(rootView, new ViewGroup.LayoutParams(-1, -1));
        }
        baseViewHolder = new RBaseViewHolder(rootView);

        initIView(state);

        onIViewLoad(state);
        return rootView;
    }

    /**
     * 1.1 初始化IView
     *
     * @param state {@link #createView(Context, ViewGroup, Bundle)}
     */
    protected void initIView(@Nullable Bundle state) {

    }

    /**
     * 2. 生命周期
     */
    public void onIViewLoad(@Nullable Bundle state) {

    }

    /**
     * 3. 生命周期
     */
    public void onIViewShow(@Nullable Bundle state) {

    }

    /**
     * 4. 生命周期
     */
    public void onIViewHide(@Nullable Bundle state) {

    }

    /**
     * 5. 生命周期
     */
    public void onIViewUnLoad() {

    }
}
