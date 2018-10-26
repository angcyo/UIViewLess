package com.angcyo.uiview.less.iview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;


/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/16
 */
public abstract class BaseIView {
    public static final int STATUS_INIT = 0x01;
    public static final int STATUS_CREATE = 0x02;
    public static final int STATUS_LOAD = 0x04;
    public static final int STATUS_SHOW = 0x08;
    public static final int STATUS_HIDE = 0x10;
    public static final int STATUS_UNLOAD = 0x20;

    protected View rootView;
    protected RBaseViewHolder baseViewHolder;
    protected Context context;
    protected ViewGroup parent;
    protected int iViewStatus = STATUS_INIT;

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

    protected void setViewStatus(int status) {
        iViewStatus = status;
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
        this.parent = parent;
        setViewStatus(STATUS_CREATE);

        int layoutId = getLayoutId();
        if (layoutId == -1) {
            rootView = createRootView();
        } else {
            rootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }

        baseViewHolder = new RBaseViewHolder(rootView);

        //将 BaseIView 对象和 View 关联.
        rootView.setTag(R.id.tag_base_iview, this);

        if (rootView.getLayoutParams() == null) {
            rootView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        }

        if (parent != null) {
            parent.addView(rootView);
        }

        initIView(state);

        if (parent != null) {
            onIViewLoad(state);
            onIViewShow(state);
        }
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
        setViewStatus(STATUS_LOAD);
    }

    /**
     * 3. 生命周期
     */
    public void onIViewShow(@Nullable Bundle state) {
        setViewStatus(STATUS_SHOW);
    }

    /**
     * 4. 生命周期
     */
    public void onIViewHide(@Nullable Bundle state) {
        setViewStatus(STATUS_HIDE);
    }

    /**
     * 5. 生命周期
     */
    public void onIViewUnLoad() {
        setViewStatus(STATUS_UNLOAD);
    }

    /**
     * 界面已经装载了
     */
    public boolean isIViewLoad() {
        return iViewStatus == STATUS_LOAD || iViewStatus == STATUS_SHOW;
    }

    public void show(@NonNull ViewGroup parent, @Nullable final Bundle state, @Nullable Animation animation, final @Nullable Runnable endAction) {
        if (iViewStatus == STATUS_INIT || rootView == null) {
            throw new IllegalArgumentException("请调用先createView().");
        }
        if (rootView.getParent() != null) {
            Log.d("BaseIView", "已经在布局中.");
            return;
        }

        if (rootView.getLayoutParams() == null) {
            rootView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        }
        this.parent = parent;
        parent.addView(rootView);
        onIViewLoad(state);

        final Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                onIViewShow(state);
                if (endAction != null) {
                    endAction.run();
                }
            }
        };

        if (animation == null) {
            endRunnable.run();
        } else {
            animation.setAnimationListener(new AnimationEnd() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    endRunnable.run();
                }
            });
        }
    }

    public void show() {
        show(parent, null);
    }

    public void show(@NonNull ViewGroup parent, @Nullable Animation animation) {
        show(parent, null, animation, null);
    }

    public void remove(@Nullable Animation animation, @Nullable final Runnable endAction) {
        if (rootView != null) {
            final ViewParent parent = rootView.getParent();
            if (parent instanceof ViewGroup) {
                final Runnable removeRunnable = new Runnable() {
                    @Override
                    public void run() {
                        onIViewHide(null);
                        onIViewUnLoad();
                        ((ViewGroup) parent).removeView(rootView);
                        if (endAction != null) {
                            endAction.run();
                        }
                    }
                };

                if (animation == null) {
                    removeRunnable.run();
                } else {
                    animation.setAnimationListener(new AnimationEnd() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            removeRunnable.run();
                        }
                    });
                }
            }
        }
    }

    public void remove() {
        remove(null);
    }

    public void remove(Animation animation) {
        remove(animation, null);
    }

    public static abstract class AnimationEnd implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
