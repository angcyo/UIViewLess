package com.angcyo.uiview.less.base;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.View;
import com.angcyo.uiview.less.recycler.RExItemDecoration;
import com.angcyo.uiview.less.recycler.RRecyclerView;
import com.angcyo.uiview.less.recycler.adapter.RBaseAdapter;
import com.angcyo.uiview.less.recycler.item.*;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/08
 */
public abstract class BaseExItemFragment extends BaseRecyclerFragment<IExStringDataType> {
    @Override
    protected RBaseAdapter<IExStringDataType> onCreateAdapter(@Nullable List<IExStringDataType> datas) {
        RExItemAdapter<String, IExStringDataType> adapter = new RExItemAdapter<String, IExStringDataType>(mAttachContext,
                new RExItemFactory<String, IExStringDataType>() {

                    @Override
                    public void registerItems(@NonNull ArrayList<RExItem<String, IExStringDataType>> allRegItems) {
                        BaseExItemFragment.this.registerItems(allRegItems);
                    }

                    @Nullable
                    @Override
                    public String getItemTypeFromData(@Nullable IExStringDataType data, int position) {
                        if (data == null) {
                            return null;
                        }
                        return data.getItemDataType();
                    }

                    @Override
                    public void onItemFactoryInit() {
                        super.onItemFactoryInit();
                    }

                    @Override
                    public void onCreateItemHolder(@NonNull RExItemHolder<IExStringDataType> itemHolder) {
                        super.onCreateItemHolder(itemHolder);
                        BaseExItemFragment.this.onCreateItemHolder(itemHolder);
                    }
                }) {

            @Override
            public void loadMoreEnd(List datas, int currentPage, int pageSize) {
                super.loadMoreEnd(datas, currentPage, pageSize);
                //BaseExItemFragment.this.loadMoreEnd(datas, currentPage, pageSize);
            }

            @Override
            public void onFirstPageSetData(List<IExStringDataType> datas) {
                //super.onFirstPageSetData(datas);
                BaseExItemFragment.this.onFirstPageSetData(datas);
            }

            @Override
            public void onOtherPageSetData(List<IExStringDataType> datas) {
                //super.onOtherPageSetData(datas);
                BaseExItemFragment.this.onOtherPageSetData(datas);
            }
        };

        //必须
        adapter.initItemFactory();
        return adapter;
    }

    /**
     * 实例化ItemHolder之后回调
     */
    public void onCreateItemHolder(@NonNull RExItemHolder<IExStringDataType> itemHolder) {

    }

//    public void loadMoreEnd(List<IExStringDataType> datas, int currentPage, int pageSize) {
//    }

    public void onFirstPageSetData(List<IExStringDataType> datas) {
        if (baseAdapter != null) {
            baseAdapter.resetData(datas);
        }
    }

    public void onOtherPageSetData(List<IExStringDataType> datas) {
        if (baseAdapter != null) {
            baseAdapter.appendData(datas);
        }
    }

    /**
     * 强转后返回
     */
    @Nullable
    public RExItemAdapter<String, IExStringDataType> getExItemAdapter() {
        if (baseAdapter instanceof RExItemAdapter) {
            return (RExItemAdapter<String, IExStringDataType>) baseAdapter;
        }
        return null;
    }

    public RExItemHolder<IExStringDataType> getItemHolderByPosition(int position) {
        RExItemAdapter<String, IExStringDataType> exItemAdapter = getExItemAdapter();
        if (exItemAdapter != null) {
            return exItemAdapter.getItemHolderByPosition(position);
        }
        return null;
    }

    /**
     * 注册处理类 {@link com.angcyo.uiview.less.recycler.item.RExItemHolder}
     */
    public abstract void registerItems(@NonNull ArrayList<RExItem<String, IExStringDataType>> allRegItems);

    @Override
    public void initRefreshRecyclerView(@Nullable SmartRefreshLayout smartRefreshLayout, @Nullable RRecyclerView recyclerView) {
        super.initRefreshRecyclerView(smartRefreshLayout, recyclerView);
        if (smartRefreshLayout != null) {

        }
        if (recyclerView != null) {
            final RExItemDecoration itemDecoration = new RExItemDecoration();
            itemDecoration.setItemDecorationCallback(new RExItemDecoration.SingleItemCallback() {

                @Override
                public void getItemOffsets2(Rect outRect, int position, int edge) {
                    RExItemHolder<IExStringDataType> itemHolder = getItemHolderByPosition(position);
                    if (itemHolder != null) {
                        itemHolder.getItemOffsets(itemDecoration, outRect, position, edge);
                    }
                }

                @Override
                public void draw(Canvas canvas, TextPaint paint, View itemView, Rect offsetRect, int itemCount, int position) {
                    RExItemHolder<IExStringDataType> itemHolder = getItemHolderByPosition(position);
                    if (itemHolder != null) {
                        itemHolder.draw(itemDecoration, canvas, paint, itemView, offsetRect, itemCount, position);
                    }
                }
            });
            recyclerView.addItemDecoration(itemDecoration);
        }
    }
}
