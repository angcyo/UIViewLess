package com.angcyo.uiview.less.widget.pager;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2016/12/17 11:07
 * 修改人员：Robi
 * 修改时间：2016/12/17 11:07
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class ImageAdapter extends PagerAdapter {

    private int width = -1, height = -1;

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        RelativeLayout layout = new RelativeLayout(container.getContext());
        ImageView imageView = new ImageView(container.getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        layout.addView(imageView, new ViewGroup.LayoutParams(-1, -1));
        initImageView(layout, imageView, position);
        container.addView(layout, new ViewGroup.LayoutParams(width, height));
        return layout;
    }

    protected abstract void initImageView(RelativeLayout rootLayout, ImageView imageView, int position);

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}

