package com.angcyo.uiview.less.base;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.widget.ImageTextView;

/**
 * 标题栏 item 创建/控制
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/07
 */
public class TitleItemHelper {

    @NonNull
    public static ImageTextView createItem(@NonNull Context context, String text) {
        return new Builder(context).setText(text).build();
    }

    @NonNull
    public static ImageTextView createItem(@NonNull Context context, @DrawableRes int src) {
        return new Builder(context).setSrc(src).build();
    }

    @NonNull
    public static ImageTextView createItem(@NonNull Context context, int src, String text) {
        return new Builder(context).setText(text).setSrc(src).build();
    }

    public static class Builder {
        Context context;
        /**
         * 可选的显示文本
         */
        String text = null;

        /**
         * 可选的图片资源
         */
        int src = -1;

        /**
         * 文字和图片之间的距离
         */
        int textOffset = -1;

        Drawable bgDrawable;

        int minWidth = -1;
        int itemWidth = -100;

        int textSize = -1;
        int textColor = -1;

        public Builder(@NonNull Context context) {
            this.context = context;
            textOffset = context.getResources()
                    .getDimensionPixelOffset(R.dimen.base_ldpi);
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setSrc(@DrawableRes int src) {
            this.src = src;
            return this;
        }

        public Builder setTextOffsetRes(@DimenRes int textOffset) {
            this.textOffset = context.getResources()
                    .getDimensionPixelOffset(textOffset);
            return this;
        }

        public Builder setTextOffset(int textOffset) {
            this.textOffset = textOffset;
            return this;
        }

        public Builder setBgDrawable(Drawable bgDrawable) {
            this.bgDrawable = bgDrawable;
            return this;
        }

        public Builder setMinWidth(int minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        public Builder setItemWidth(int itemWidth) {
            this.itemWidth = itemWidth;
            return this;
        }

        public Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public ImageTextView build() {
            return build(null);
        }

        public ImageTextView build(ViewGroup viewGroup) {
            ImageTextView view;
            if (viewGroup == null) {
                view = (ImageTextView) LayoutInflater.from(context)
                        .inflate(R.layout.base_title_item_layout,
                                new FrameLayout(context), false);
            } else {
                view = (ImageTextView) LayoutInflater.from(context)
                        .inflate(R.layout.base_title_item_layout,
                                viewGroup, false);
                viewGroup.addView(view);
            }
            if (text != null) {
                view.setShowText(text);
            }
            if (textColor != -1) {
                view.setTextShowColor(textColor);
            }
            if (textSize != -1) {
                view.setShowTextSize(textSize);
            }
            if (textOffset != -1) {
                view.setTextOffset(textOffset);
            }
            if (bgDrawable != null) {
                ViewCompat.setBackground(view, bgDrawable);
            }
            if (src != -1) {
                view.setImageResource(src);
            }
            if (minWidth != -1) {
                view.setMinimumWidth(minWidth);
            }
            if (itemWidth != -100) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = itemWidth;
                view.setLayoutParams(layoutParams);
            }
            return view;
        }
    }
}
