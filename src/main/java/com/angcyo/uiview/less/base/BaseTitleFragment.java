package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.skin.SkinHelper;

/**
 * Email:angcyo@126.com
 * 统一标题管理的Fragment
 *
 * @author angcyo
 * @date 2018/12/07
 */
public class BaseTitleFragment extends BaseFragment {
    @NonNull
    @Override
    protected View createRootView() {
        return super.createRootView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.base_title_fragment_layout;
    }

    @Override
    protected void initBaseView(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.initBaseView(arguments, savedInstanceState);

        baseViewHolder.v(R.id.base_title_bar_layout).setBackgroundColor(SkinHelper.getSkin().getThemeColor());

        baseViewHolder.vg(R.id.base_title_left_layout).addView(TitleItemHelper.createItem(mAttachContext, R.drawable.base_back));
        baseViewHolder.vg(R.id.base_title_left_layout).addView(TitleItemHelper.createItem(mAttachContext, "测试1"));
        baseViewHolder.vg(R.id.base_title_left_layout).addView(TitleItemHelper.createItem(mAttachContext, "测试2"));

        baseViewHolder.vg(R.id.base_title_right_layout).addView(TitleItemHelper.createItem(mAttachContext, R.drawable.base_back, "测试1"));
        baseViewHolder.vg(R.id.base_title_right_layout).addView(TitleItemHelper.createItem(mAttachContext, R.drawable.base_back, "测试2"));
    }
}
