package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.angcyo.http.HttpSubscriber;
import com.angcyo.http.NonetException;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.widget.group.RSoftInputLayout;
import rx.Subscription;
import rx.observers.SafeSubscriber;
import rx.subscriptions.CompositeSubscription;

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

    @NonNull
    public FragmentManager parentFragmentManager() {
        if (getParentFragment() == null) {
            return requireFragmentManager();
        } else {
            return getParentFragment().requireFragmentManager();
        }
    }

    /**
     * 隐藏键盘
     */
    public void hideSoftInput() {
        View fragmentRootView = getView();
        if (fragmentRootView != null) {
            View focus = fragmentRootView.findFocus();
            if (focus instanceof EditText) {
                RSoftInputLayout.hideSoftInput(focus);
            } else if (focus != null) {
                RSoftInputLayout.hideSoftInput(focus);
            }
        }
    }

    //<editor-fold desc="网络请求管理">

    @Override
    public void onDestroy() {
        super.onDestroy();
        onCancelSubscriptions();
    }

    protected CompositeSubscription mSubscriptions;

    public void onCancelSubscriptions() {
        if (mSubscriptions != null) {
            mSubscriptions.clear();
        }
    }

    public void addSubscription(Subscription subscription) {
        addSubscription(subscription, false);
    }

    public void addSubscription(Subscription subscription, boolean checkToken) {
        addSubscription(mSubscriptions, subscription, checkToken, new Runnable() {
            @Override
            public void run() {
                onCancelSubscriptions();
            }
        });
    }

    public static void addSubscription(CompositeSubscription subscriptions, Subscription subscription, boolean checkToken, Runnable onCancel) {
        if (subscription == null) {
            return;
        }
        if (subscriptions != null) {
            subscriptions.add(subscription);
        }
        if (NetworkStateReceiver.getNetType().value() < 2) {
            //2G网络以下, 取消网络请求
            if (onCancel != null) {
                onCancel.run();
            }
            try {
                if (subscription instanceof SafeSubscriber) {
                    if (((SafeSubscriber) subscription).getActual() instanceof HttpSubscriber) {
                        ((SafeSubscriber) subscription).getActual().onError(new NonetException());
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    //</editor-fold">

}
