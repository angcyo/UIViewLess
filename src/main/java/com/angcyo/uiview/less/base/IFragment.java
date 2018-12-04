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

    //void onFragmentShowInPager();

    //void onFragmentHideInPager();
}
