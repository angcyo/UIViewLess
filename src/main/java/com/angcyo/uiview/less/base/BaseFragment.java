package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.angcyo.lib.L;

/**
 * Created by angcyo on 2018/12/03 23:17
 */
public class BaseFragment extends AbsFragment implements IFragment {
    /**
     * Fragment 是否可见
     */
    protected boolean isFragmentVisible = true;

    //<editor-fold desc="生命周期, 系统的方法">

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        switchVisible(!hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        switchVisible(isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFragmentVisible) {
            onFragmentShow(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFragmentVisible) {
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

    protected void switchVisible(boolean visible) {
        if (isFragmentVisible == visible) {
            return;
        }
        isFragmentVisible = visible;
        if (visible) {
            onFragmentHide();
        } else {
            onFragmentShow(null);
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
    //</editor-fold>
}
