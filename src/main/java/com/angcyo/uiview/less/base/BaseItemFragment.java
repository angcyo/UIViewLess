package com.angcyo.uiview.less.base;

import android.support.annotation.Nullable;
import android.view.View;
import com.angcyo.uiview.less.recycler.RRecyclerView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/08
 */
public abstract class BaseItemFragment<T> extends BaseRecyclerFragment<T> {
    @Override
    public void initRefreshRecyclerView(@Nullable SmartRefreshLayout smartRefreshLayout, @Nullable RRecyclerView recyclerView) {
        super.initRefreshRecyclerView(smartRefreshLayout, recyclerView);
        if (smartRefreshLayout != null) {
            //激活越界滚动
            smartRefreshLayout.setEnableOverScrollDrag(true);
            //纯滚动模式, 需要激活越界滚动才有效
            smartRefreshLayout.setEnablePureScrollMode(true);
        }
        if (recyclerView != null) {
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }
}
