package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;

/**
 * Created by angcyo on 2018/12/03 23:17
 * <p>
 * 生命周期的封装, 只需要关注 {@link #onFragmentShow(Bundle)} 和 {@link #onFragmentHide()}
 */
public abstract class BaseFragment extends AbsLifeCycleFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        final Bundle arguments = getArguments();

        baseViewHolder.post(new Runnable() {
            @Override
            public void run() {
                onPostCreateView(container, arguments, savedInstanceState);
            }
        });
        return view;
    }

    /**
     * 此方法会在onCreateView之后回调
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 模拟Activity 的 onPostCreate啊
     */
    protected void onPostCreateView(@Nullable ViewGroup container, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {

    }

    @Override
    protected void initBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.initBaseView(viewHolder, arguments, savedInstanceState);
        if (interceptRootTouchEvent()) {
            viewHolder.itemView.setClickable(true);
        }
    }

    /**
     * 拦截RootView的事件, 防止事件穿透到底下的Fragment
     */
    protected boolean interceptRootTouchEvent() {
        return true;
    }
}
