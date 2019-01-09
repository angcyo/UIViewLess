package com.angcyo.uiview.less.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.base.helper.ActivityHelper;

/**
 * Created by angcyo on 2018/12/03 23:17
 * <p>
 * 生命周期的封装, 只需要关注 {@link #onFragmentShow(Bundle)} 和 {@link #onFragmentHide()}
 *
 * @author angcyo
 */
public abstract class AbsLifeCycleFragment extends AbsFragment implements IFragment {

    /**
     * 保存可见性, 用来恢复状态.
     */
    public static final String KEY_FRAGMENT_VISIBLE = "key_fragment_visible";

    protected IFragment iLastFragment;

    /**
     * Fragment 是否对用户可见, 不管是GONE, 还是被覆盖, 都是不可见
     */
    protected boolean isFragmentVisible = true;

    /**
     * Fragment 是否在 ViewPager中
     * <p>
     * ViewPager中, 所有的onFragmentShow方法, 都将在 Adapter中回调
     */
    protected boolean isInViewPager = false;

    /**
     * 是否显示过第一次
     */
    protected boolean firstShowEnd = false;

    //<editor-fold desc="生命周期, 系统的方法">

    @Override
    protected void onVisibleChanged(boolean oldHidden, boolean oldUserVisibleHint, boolean visible) {
        super.onVisibleChanged(oldHidden, oldUserVisibleHint, visible);
        switchVisible(oldHidden, oldUserVisibleHint, visible);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFragmentHide()) {
            onFragmentShow(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isFragmentHide()) {
            onFragmentHide();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container instanceof ViewPager) {
            isInViewPager = true;
            //ViewPager中, 默认是隐藏状态
            if (savedInstanceState == null) {
                isFragmentVisible = false;
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    //</editor-fold>

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //状态恢复
            isFragmentVisible = savedInstanceState.getBoolean(KEY_FRAGMENT_VISIBLE, isFragmentVisible);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*数据恢复*/
        if (savedInstanceState != null) {
            Bundle arguments = savedInstanceState.getBundle(ActivityHelper.KEY_EXTRA);
            if (arguments != null) {
                setArguments(arguments);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        /*数据保存*/
        Bundle arguments = getArguments();
        if (arguments != null) {
            outState.putBundle(ActivityHelper.KEY_EXTRA, arguments);
        }
        outState.putBoolean(KEY_FRAGMENT_VISIBLE, isFragmentVisible);
    }

    //<editor-fold desc="自定义, 可以重写 的方法">

    protected void switchVisible(boolean oldHidden, boolean oldUserVisibleHint, boolean visible /*是否可见*/) {
        if (isInViewPager) {
            if (!isAdded() && visible) {
                //需要可见状态, 但是Fragment又没有add.
                return;
            }
            if (oldUserVisibleHint == visible) {
                if (visible) {
                    onFragmentReShow();
                }
                return;
            }
        } else {
            if (isFragmentVisible == visible) {
                //已经是可见状态, 或者不可见状态
                if (visible) {
                    onFragmentReShow();
                }
                return;
            }
            isFragmentVisible = visible;
        }

        if (visible) {
            onFragmentShow(null);
        } else {
            onFragmentHide();
        }
        if (iLastFragment instanceof Fragment) {
            ((Fragment) iLastFragment).setUserVisibleHint(visible);
        }
    }

    public void onFragmentReShow() {
        L.i(this.getClass().getSimpleName() +
                " view:" + (getView() == null ? "×" : "√") +
                " viewHolder:" + (baseViewHolder == null ? "×" : "√"));
    }


    @Override
    public void onFragmentShow(@Nullable Bundle bundle) {
        L.i(this.getClass().getSimpleName() +
                " view:" + (getView() == null ? "×" : "√") +
                " viewHolder:" + (baseViewHolder == null ? "×" : "√") +
                " bundle:" + (bundle == null ? "×" : "√" +
                " firstShowEnd:" + (firstShowEnd ? "√" : "×")));
        if (getView() != null) {
            if (firstShowEnd) {
                onFragmentNotFirstShow(bundle);
            } else {
                firstShowEnd = true;
                onFragmentFirstShow(bundle);
            }
        }
    }

    /**
     * 从 onFragmentShow 分出来的周期事件
     */
    public void onFragmentFirstShow(@Nullable Bundle bundle) {

    }

    public void onFragmentNotFirstShow(@Nullable Bundle bundle) {

    }

    @Override
    public void onFragmentHide() {
        L.i(this.getClass().getSimpleName() +
                " view:" + (getView() == null ? "×" : "√") +
                " viewHolder:" + (baseViewHolder == null ? "×" : "√"));
    }

    @Override
    public boolean isFragmentHide() {
        if (isInViewPager) {
            if (isAdded()) {
                return !getUserVisibleHint();
            } else {
                return true;
            }
        }

        //boolean isVisible = getUserVisibleHint() && !isHidden();
        //return !isVisible;
        return !isFragmentVisible;
    }

    @Override
    public void setLastFragment(@Nullable IFragment iFragment) {
        iLastFragment = iFragment;
    }

    @Nullable
    @Override
    public IFragment getLastFragment() {
        return iLastFragment;
    }

    @Override
    public void setFragmentInViewPager(boolean inViewPager) {
        isInViewPager = inViewPager;
        if (isInViewPager) {
            isFragmentVisible = false;
        }
    }

    @Override
    public boolean isFragmentInViewPager() {
        return isInViewPager;
    }

    /**
     * 需要显示的标题
     */
    public String getFragmentTitle() {
        return this.getClass().getSimpleName();
    }

    /**
     * 可以关闭当前界面.
     */
    @Override
    public boolean onBackPressed(@NonNull Activity activity) {
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (iLastFragment != null) {
            if (iLastFragment instanceof Fragment) {
                ((Fragment) iLastFragment).onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean canSwipeBack() {
        return getView() != null;
    }

    @Override
    public boolean hideSoftInputOnTouchDown(@Nullable View touchDownView) {
        return false;
    }

    //</editor-fold>

    //<editor-fold desc="界面操作">

    //</editor-fold>
}
