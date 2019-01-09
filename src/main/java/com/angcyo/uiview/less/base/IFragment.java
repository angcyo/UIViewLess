package com.angcyo.uiview.less.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by angcyo on 2018/12/04 23:32
 *
 * @author angcyo
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

    /**
     * Activity 的 onBackPressed 回调.
     *
     * @return true 允许关闭当前的Fragment
     */
    boolean onBackPressed(@NonNull Activity activity);

    //void onFragmentShowInPager();

    //void onFragmentHideInPager();

    /**
     * 是否可以滑动返回
     *
     * @return true 允许
     */
    boolean canSwipeBack();

    /**
     * 当手指在 touchDownView 上点击时, 是否调用隐藏键盘的方法
     */
    boolean hideSoftInputOnTouchDown(@Nullable View touchDownView);
}
