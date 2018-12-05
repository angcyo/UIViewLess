package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by angcyo on 2018/12/04 23:32
 */
public interface IFragment {

    /**
     * 当Fragment可见时回调
     */
    void onFragmentShow(@Nullable Bundle bundle);

    /**
     * 当Fragment不可见时回调
     */
    void onFragmentHide();

    /**
     * 调用系统的 transaction.hide(hideFragment) , Fragment 的 getView 可见性为 GONE
     * <p>
     * 当 Fragment 上面, 显示了另一个 Fragment时, getView 可见性为 VISIBLE, 但是此时 Fragment的状态也是 hide
     */
    boolean isFragmentHide();

    /**
     * 用来在Fragment 里面 嵌套 Fragment 的显示隐藏状态传递
     */
    void setLastFragment(@Nullable IFragment iFragment);

    @Nullable
    IFragment getLastFragment();

    void setFragmentInViewPager(boolean inViewPager);

    boolean isFragmentInViewPager();

    //void onFragmentShowInPager();

    //void onFragmentHideInPager();
}
