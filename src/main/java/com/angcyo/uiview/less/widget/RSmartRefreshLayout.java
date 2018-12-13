package com.angcyo.uiview.less.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.angcyo.uiview.less.smart.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/13
 */
public class RSmartRefreshLayout extends SmartRefreshLayout {

    public RSmartRefreshLayout(Context context) {
        super(context);
        initLayout(context);
    }

    public RSmartRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    public RSmartRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    private void initLayout(Context context) {
        // √ 是否在列表不满一页时候开启上拉加载功能
        setEnableLoadMoreWhenContentNotFull(false);
        // √ 是否启用越界拖动（仿苹果效果）1.0.4
        setEnableOverScrollDrag(false);

        // √ 是否启用列表惯性滑动到底部时自动加载更多, 关闭之后, 需要释放手指, 才能加载更多
        setEnableAutoLoadMore(false);

        //是否启用嵌套滚动, 默认智能控制
        //setEnableNestedScroll(false);
        // √ 是否启用越界回弹, 关闭后, 快速下滑列表不会触发刷新事件回调
        setEnableOverScrollBounce(false);

        //是否在刷新完成时滚动列表显示新的内容 1.0.5,
        setEnableScrollContentWhenRefreshed(true);
        // √ 是否在加载完成时滚动列表显示新的内容, RecyclerView会自动滚动 Footer的高度
        setEnableScrollContentWhenLoaded(true);

        //是否在全部加载结束之后Footer跟随内容1.0.4
        setEnableFooterFollowWhenLoadFinished(true);

        // √ 是否下拉Header的时候向下平移列表或者内容, 内容是否跟手
        setEnableHeaderTranslationContent(true);
        // √ 是否上拉Footer的时候向上平移列表或者内容, 内容是否跟手
        setEnableFooterTranslationContent(true);

        /*
         * 重点：设置 srlEnableNestedScrolling 为 false 才可以兼容 BottomSheet
         * */
        //setEnableNestedScroll(false);

        //android 原生样式
        setRefreshHeader(new MaterialHeader(context));
        //关闭内容跟随移动, 更像原生样式
        setEnableHeaderTranslationContent(false);

        //ios的下拉刷新样式
        //setRefreshHeader(new ClassicsHeader(mAttachContext));
        setRefreshFooter(new ClassicsFooter(context));
    }
}
