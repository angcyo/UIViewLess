package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.angcyo.lib.L;

/**
 * Created by angcyo on 2018/12/03 23:17
 */
public class BaseFragment extends AbsFragment implements IFragment {

    protected IFragment iLastFragment;

    /**
     * Fragment 是否对用户可见, 不管是GONE, 还是被覆盖, 都是不可见
     */
    protected boolean isFragmentVisible = true;

    //<editor-fold desc="生命周期, 系统的方法">

    @Override
    protected void onVisibleChanged(boolean visible) {
        super.onVisibleChanged(visible);
        switchVisible(visible);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    //</editor-fold>

    //<editor-fold desc="自定义, 可以重写 的方法">

    protected void switchVisible(boolean visible /*是否可见*/) {
        if (isFragmentVisible == visible) {
            //已经是可见状态, 或者不可见状态
            return;
        }
        isFragmentVisible = visible;
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
