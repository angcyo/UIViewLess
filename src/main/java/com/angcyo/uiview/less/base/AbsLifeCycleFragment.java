package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.angcyo.lib.L;

/**
 * Created by angcyo on 2018/12/03 23:17
 * <p>
 * 生命周期的封装, 只需要关注 {@link #onFragmentShow(Bundle)} 和 {@link #onFragmentHide()}
 */
public abstract class AbsLifeCycleFragment extends AbsFragment implements IFragment {

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
            isFragmentVisible = false;
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
    }

    //<editor-fold desc="自定义, 可以重写 的方法">

    protected void switchVisible(boolean oldHidden, boolean oldUserVisibleHint, boolean visible /*是否可见*/) {
        if (isInViewPager) {
            if (!isAdded() && visible) {
                //需要可见状态, 但是Fragment又没有add.
                return;
            }
            if (oldUserVisibleHint == visible) {
                return;
            }
        } else {
            if (isFragmentVisible == visible) {
                //已经是可见状态, 或者不可见状态
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

    @Override
    public void onFragmentShow(@Nullable Bundle bundle) {
        L.i(this.getClass().getSimpleName() + " " + bundle);
    }

    @Override
    public void onFragmentHide() {
        L.i(this.getClass().getSimpleName());
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

    //</editor-fold>

    //<editor-fold desc="界面操作">

    public Fragment showFragment(@NonNull Fragment fragment, int parentLayout) {
        return showFragment(fragment, parentLayout, false);
    }

    public Fragment showFragment(@NonNull Fragment fragment, int parentLayout, boolean stateLoss) {
        return showFragment(fragment, null, parentLayout, stateLoss);
    }

    public Fragment showFragment(@NonNull Fragment fragment, @Nullable Fragment hideFragment, int parentLayout, boolean stateLoss) {
        return FragmentHelper.showFragment(getChildFragmentManager(), fragment, hideFragment, parentLayout, stateLoss);
    }
    //</editor-fold>
}
