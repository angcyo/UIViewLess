package com.angcyo.uiview.less.base.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.widget.ImageTextView;
import com.angcyo.uiview.view.RClickListener;
import org.jetbrains.annotations.Nullable;

/**
 * 标题栏 item 创建/控制
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/07
 */
public class TitleItemHelper {

    /**
     * 定义为不存在值
     */
    public static final int NO_NUM = -0xff;

    public static Builder build(@NonNull Context context) {
        return new Builder(context);
    }

    @NonNull
    public static ImageTextView createItem(@NonNull Context context, String text, View.OnClickListener listener) {
        return new Builder(context).setText(text).setClickListener(listener).build();
    }

    @NonNull
    public static ImageTextView createItem(@NonNull Context context, @DrawableRes int src, View.OnClickListener listener) {
        return new Builder(context).setSrc(src).setClickListener(listener).build();
    }

    @NonNull
    public static ImageTextView createItem(@NonNull Context context, int src, String text, View.OnClickListener listener) {
        return new Builder(context).setText(text).setSrc(src).setClickListener(listener).build();
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
        int itemWidth = NO_NUM;

        int textSize = -1;
        int textColor = -1;

        View.OnClickListener listener;

        int leftMargin;
        int rightMargin;

        /**
         * 视图id, 可以用来findViewById
         */
        int viewId = -1;

        int visibility = View.VISIBLE;

        Object tag = null;

        /**
         * AddView中的Index
         */
        int viewIndex = -1;

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

        public Builder setClickListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setLeftMargin(int leftMargin) {
            this.leftMargin = leftMargin;
            return this;
        }

        public Builder setRightMargin(int rightMargin) {
            this.rightMargin = rightMargin;
            return this;
        }

        public Builder setViewId(int viewId) {
            this.viewId = viewId;
            return this;
        }

        public Builder setVisibility(int visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder setTag(Object tag) {
            this.tag = tag;
            return this;
        }

        public Builder setViewIndex(int viewIndex) {
            this.viewIndex = viewIndex;
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
                viewGroup.addView(view, viewIndex);
            }
            if (viewId != -1) {
                view.setId(viewId);
            }
            view.setVisibility(visibility);
            if (tag != null) {
                view.setTag(tag);
            }
            if (listener != null) {
                view.setOnClickListener(new RClickListener() {
                    @Override
                    public void onRClick(@Nullable View view) {
                        if (listener != null) {
                            listener.onClick(view);
                        }
                    }
                });
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
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (itemWidth != NO_NUM) {
                layoutParams.width = itemWidth;
            }
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin = leftMargin;
                ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = rightMargin;
            }
            view.setLayoutParams(layoutParams);
            return view;
        }
    }
}
