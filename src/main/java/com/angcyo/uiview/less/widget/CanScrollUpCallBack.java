package com.angcyo.uiview.less.widget;

import android.support.v7.widget.RecyclerView;

public interface CanScrollUpCallBack {
    /**
     * 顶部是否还可以滚动
     */
    boolean canChildScrollUp();

    /**
     * 用来执行fling操作的view
     */
    RecyclerView getRecyclerView();
}