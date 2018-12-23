package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.iview.AffectUI;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.recycler.RRecyclerView;
import com.angcyo.uiview.less.recycler.adapter.RBaseAdapter;
import com.angcyo.uiview.less.smart.MaterialHeader;
import com.angcyo.uiview.less.widget.RSmartRefreshLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
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
public abstract class BaseRecyclerFragment<T> extends BaseTitleFragment
        implements OnRefreshListener, OnLoadMoreListener, RBaseAdapter.OnAdapterLoadMoreListener<T> {

    protected RSmartRefreshLayout smartRefreshLayout;

    protected RRecyclerView recyclerView;

    protected RBaseAdapter<T> baseAdapter;

    @Override
    protected int getContentLayoutId() {
        return R.layout.base_recycler_fragment_layout;
    }

    @Override
    protected void onInitBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onInitBaseView(viewHolder, arguments, savedInstanceState);

        smartRefreshLayout = baseViewHolder.v(R.id.base_refresh_layout);
        recyclerView = baseViewHolder.v(R.id.base_recycler_view);

        initRefreshRecyclerView(smartRefreshLayout, recyclerView);
    }

    public void initRefreshRecyclerView(@Nullable SmartRefreshLayout smartRefreshLayout, @Nullable RRecyclerView recyclerView) {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.setOnRefreshListener(this);
            /*设置加载更多监听之后, 会自动开启加载更多*/
            smartRefreshLayout.setOnLoadMoreListener(this);
            // √ 激活加载更多, 关闭加载更多时, 尽量也关闭不满一页时候开启上拉加载功能
            smartRefreshLayout.setEnableLoadMore(false);
            // √ 是否在列表不满一页时候开启上拉加载功能
            smartRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
            // √ 是否启用越界拖动（仿苹果效果）1.0.4
            smartRefreshLayout.setEnableOverScrollDrag(false);

            // √ 是否启用下拉刷新功能
            smartRefreshLayout.setEnableRefresh(true);

            // √ 是否启用列表惯性滑动到底部时自动加载更多, 关闭之后, 需要释放手指, 才能加载更多
            smartRefreshLayout.setEnableAutoLoadMore(false);

            //是否启用嵌套滚动, 默认智能控制
            //smartRefreshLayout.setEnableNestedScroll(false);
            // √ 是否启用越界回弹, 关闭后, 快速下滑列表不会触发刷新事件回调
            smartRefreshLayout.setEnableOverScrollBounce(false);

            //是否在刷新完成时滚动列表显示新的内容 1.0.5,
            smartRefreshLayout.setEnableScrollContentWhenRefreshed(true);
            // √ 是否在加载完成时滚动列表显示新的内容, RecyclerView会自动滚动 Footer的高度
            smartRefreshLayout.setEnableScrollContentWhenLoaded(true);
            // √ 是否下拉Header的时候向下平移列表或者内容, 内容是否跟手
            smartRefreshLayout.setEnableHeaderTranslationContent(true);
            // √ 是否上拉Footer的时候向上平移列表或者内容, 内容是否跟手
            smartRefreshLayout.setEnableFooterTranslationContent(true);

            //是否在全部加载结束之后Footer跟随内容1.0.4
            smartRefreshLayout.setEnableFooterFollowWhenLoadFinished(true);

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

            if (baseAdapter != null) {
                baseAdapter.setOnLoadMoreListener(this);
            }
            //recyclerView.setBackgroundColor(Color.GREEN);
        }
    }

    /**
     * 禁掉下拉刷新效果
     */
    public void disableRefreshAffect() {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.setEnableRefresh(false);
            smartRefreshLayout.setEnableLoadMore(false);
            smartRefreshLayout.setEnableOverScrollDrag(false);
            smartRefreshLayout.setEnablePureScrollMode(false);
        }
    }

    /**
     * 启用纯下拉刷新效果
     */
    public void enableRefreshAffect() {
        if (smartRefreshLayout != null) {
            //激活越界滚动
            smartRefreshLayout.setEnableOverScrollDrag(true);
            //纯滚动模式, 需要激活越界滚动才有效
            smartRefreshLayout.setEnablePureScrollMode(true);

            smartRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
            smartRefreshLayout.setEnableLoadMore(true);
            smartRefreshLayout.setEnableFooterTranslationContent(true);
            smartRefreshLayout.setEnableHeaderTranslationContent(true);
        }
    }

    /**
     * 重置刷新控件和Adapter状态
     */
    public void resetUIStatus() {
        if (smartRefreshLayout != null) {
            if (smartRefreshLayout.isEnableRefresh()) {
                smartRefreshLayout.finishRefresh();
            }
            if (smartRefreshLayout.isEnableLoadMore()) {
                smartRefreshLayout.finishLoadMore();
            }
        }
        if (baseAdapter != null) {
            if (baseAdapter.isEnableLoadMore()) {
                baseAdapter.setLoadMoreEnd();
            }
        }
    }

    /**
     * 如果没有数据展示, 才切换到错误情感图
     */
    public void switchToError() {
        if (baseAdapter != null) {
            if (baseAdapter.getAllDataCount() <= 0) {
                switchAffectUI(AffectUI.AFFECT_ERROR);
            }
        }
    }

    /**
     * 显示内容
     */
    public void switchToContent() {
        switchAffectUI(AffectUI.AFFECT_CONTENT);
    }
    //<editor-fold desc="事件回调">

    /**
     * 创建适配器
     */
    protected RBaseAdapter<T> onCreateAdapter(@Nullable List<T> datas) {
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
            }
        };
    }

    @Override
    public void onAffectChange(@NonNull AffectUI affectUI, int fromAffect, int toAffect, @Nullable View fromView, @NonNull View toView) {
        super.onAffectChange(affectUI, fromAffect, toAffect, fromView, toView);
        if (toAffect == AffectUI.AFFECT_ERROR) {
            //显示额外的错误信息
            Object extraObj = affectUI.getExtraObj();
            if (extraObj != null) {
                if (extraObj instanceof String) {
                    baseViewHolder.tv(R.id.base_error_tip_view).setText((CharSequence) extraObj);
                } else if (extraObj instanceof Number) {

                } else {
                    baseViewHolder.tv(R.id.base_error_tip_view).setText(extraObj.toString());
                }
            }

            baseViewHolder.click(R.id.base_retry_button, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchAffectUI(AffectUI.AFFECT_LOADING);
                }
            });
        } else if (toAffect == AffectUI.AFFECT_LOADING) {

            if (!isFragmentHide()) {
                //切换到加载情感图, 调用刷新数据接口
                baseViewHolder.post(new Runnable() {
                    @Override
                    public void run() {
                        onBaseRefresh(null);
                    }
                });
            }
        }
    }

    @Override
    public void onFragmentFirstShow(@Nullable Bundle bundle) {
        super.onFragmentFirstShow(bundle);
        baseViewHolder.post(new Runnable() {
            @Override
            public void run() {
                onBaseRefresh(null);
            }
        });
    }

    /**
     * 刷新控件, 刷新事件
     */
    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        onBaseRefresh(refreshLayout);
    }

    /**
     * 刷新控件, 加载更多事件
     */
    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        onBaseLoadMore(refreshLayout, null);
    }

    /**
     * 适配器, 加载更多事件
     */
    @Override
    public void onAdapterLodeMore(@NonNull final RBaseAdapter<T> adapter) {
        onBaseLoadMore(null, adapter);
    }

    /**
     * 分出来的刷新回调
     */
    public void onBaseRefresh(@Nullable RefreshLayout refreshLayout) {
        if (refreshLayout != null) {
            refreshLayout.finishRefresh(2_000);
        }
    }

    /**
     * 分出来的加载更多回调
     */
    public void onBaseLoadMore(@Nullable RefreshLayout refreshLayout,
                               @Nullable final RBaseAdapter<T> adapter) {

        if (refreshLayout != null) {
            refreshLayout.finishLoadMore(2_000);
        }

        if (adapter != null) {
            baseViewHolder.postDelay(2_000, new Runnable() {
                @Override
                public void run() {
                    adapter.setNoMore(true);
                }
            });
        }
    }

    //</editor-fold>

}
