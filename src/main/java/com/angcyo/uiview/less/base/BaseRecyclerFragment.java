package com.angcyo.uiview.less.base;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.recycler.RBaseAdapter;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.recycler.RRecyclerView;
import com.angcyo.uiview.less.smart.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView打底的Fragment
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/08
 */
public abstract class BaseRecyclerFragment<T> extends BaseTitleFragment implements OnRefreshListener, OnLoadMoreListener {

    protected SmartRefreshLayout smartRefreshLayout;

    protected RRecyclerView recyclerView;

    protected RBaseAdapter<T> baseAdapter;

    @Override
    protected int getContentLayoutId() {
        return R.layout.base_recycler_fragment_layout;
    }

    @Override
    protected void onInitBaseView(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onInitBaseView(arguments, savedInstanceState);

        smartRefreshLayout = baseViewHolder.v(R.id.base_refresh_layout);
        recyclerView = baseViewHolder.v(R.id.base_recycler_view);

        initRefreshRecyclerView(smartRefreshLayout, recyclerView);
    }

    public void initRefreshRecyclerView(@Nullable SmartRefreshLayout smartRefreshLayout, @Nullable RRecyclerView recyclerView) {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.setOnRefreshListener(this);
            /*设置加载更多监听之后, 会自动开启加载更多*/
            smartRefreshLayout.setOnLoadMoreListener(this);
            //激活加载更多, 关闭加载更多时, 尽量也关闭不满一页时候开启上拉加载功能
            smartRefreshLayout.setEnableLoadMore(false);
            //是否在列表不满一页时候开启上拉加载功能
            smartRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
            //越界滚动
            smartRefreshLayout.setEnableOverScrollDrag(false);

            //是否启用下拉刷新功能
            smartRefreshLayout.setEnableRefresh(true);

            //是否启用列表惯性滑动到底部时自动加载更多, 关闭之后, 需要释放手指, 才能加载更多
            smartRefreshLayout.setEnableAutoLoadMore(false);

            //是否启用嵌套滚动
            //smartRefreshLayout.setEnableNestedScroll(false);
            //是否在刷新完成时滚动列表显示新的内容 1.0.5,
            smartRefreshLayout.setEnableScrollContentWhenRefreshed(true);
            //是否在加载完成时滚动列表显示新的内容
            smartRefreshLayout.setEnableScrollContentWhenLoaded(true);
            //是否下拉Header的时候向下平移列表或者内容, 内容是否跟手
            smartRefreshLayout.setEnableHeaderTranslationContent(true);
            //是否上拉Footer的时候向上平移列表或者内容, 内容是否跟手
            smartRefreshLayout.setEnableFooterTranslationContent(true);

            //是否在全部加载结束之后Footer跟随内容1.0.4
            smartRefreshLayout.setEnableFooterFollowWhenLoadFinished(true);
            //是否启用越界拖动（仿苹果效果）1.0.4
            //smartRefreshLayout.setEnableOverScrollDrag(true);

            //android 原生样式
            smartRefreshLayout.setRefreshHeader(new MaterialHeader(mAttachContext));
            //关闭内容跟随移动, 更像原生样式
            smartRefreshLayout.setEnableHeaderTranslationContent(false);

            //ios的下拉刷新样式
            //smartRefreshLayout.setRefreshHeader(new ClassicsHeader(mAttachContext));
            smartRefreshLayout.setRefreshFooter(new ClassicsFooter(mAttachContext));
        }
        if (recyclerView != null) {
            baseAdapter = onCreateAdapter(new ArrayList<T>());
            recyclerView.setAdapter(baseAdapter);
            //recyclerView.setBackgroundColor(Color.GREEN);
        }
    }

    //<editor-fold desc="事件回调">

    /**
     * 创建适配器
     */
    protected RBaseAdapter<T> onCreateAdapter(List<T> datas) {
        return new RBaseAdapter<T>(mAttachContext, datas) {
            @Override
            protected int getItemLayoutId(int viewType) {
                return android.R.layout.simple_list_item_1;
            }

            @Override
            protected void onBindView(@NonNull RBaseViewHolder holder, int position, T bean) {
                if (holder.itemView instanceof TextView && bean instanceof String) {
                    ((TextView) holder.itemView).setText((String) bean);
                }
            }

            @Override
            protected void onLoadMore() {
                super.onLoadMore();
                baseViewHolder.postDelay(2_000, new Runnable() {
                    @Override
                    public void run() {
                        setNoMore(true);
                    }
                });
            }
        };
    }

    /**
     * 刷新控件, 刷新事件
     */
    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.finishRefresh(2_000);
    }

    /**
     * 刷新控件, 加载更多事件
     */
    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.finishLoadMore(2_000);
    }

    //</editor-fold>

}
