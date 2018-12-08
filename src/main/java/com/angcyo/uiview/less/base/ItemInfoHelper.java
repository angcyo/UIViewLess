package com.angcyo.uiview.less.base;

import android.support.annotation.DrawableRes;
import android.view.View;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.widget.group.ItemInfoLayout;
import com.angcyo.uiview.view.RClickListener;
import org.jetbrains.annotations.Nullable;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/08
 */
public class ItemInfoHelper {

    public static Builder build(View view) {
        return new Builder(view);
    }

    public static Builder build(ItemInfoLayout view) {
        return new Builder(view);
    }

    public static class Builder {
        ItemInfoLayout infoLayout;
        String itemText;
        String itemDarkText;

        int leftRes = -1;
        int leftDrawPadding = 0;

        int rightRes = -1;
        int rightDrawPadding = 0;

        View.OnClickListener listener;

        public Builder(View infoLayout) {
            if (infoLayout instanceof ItemInfoLayout) {
                this.infoLayout = (ItemInfoLayout) infoLayout;
            }
        }

        public Builder(ItemInfoLayout infoLayout) {
            this.infoLayout = infoLayout;
        }

        public Builder setItemText(String itemText) {
            this.itemText = itemText;
            return this;
        }

        public Builder setItemDarkText(String itemDarkText) {
            this.itemDarkText = itemDarkText;
            return this;
        }

        public Builder setLeftRes(int leftRes) {
            this.leftRes = leftRes;
            return this;
        }

        public Builder setLeftDrawPadding(int leftDrawPadding) {
            this.leftDrawPadding = leftDrawPadding;
            return this;
        }

        public Builder setRightRes(int rightRes) {
            this.rightRes = rightRes;
            return this;
        }

        public Builder setRightDrawPadding(int rightDrawPadding) {
            this.rightDrawPadding = rightDrawPadding;
            return this;
        }

        public Builder setClickListener(final View.OnClickListener listener) {
            if (listener != null) {
                this.listener = new RClickListener() {
                    @Override
                    public void onRClick(@Nullable View view) {
                        listener.onClick(view);
                    }
                };
            } else {
                this.listener = null;
            }
            return this;
        }

        public ItemInfoLayout doIt() {
            if (infoLayout != null) {
                infoLayout.setItemText(itemText);
                infoLayout.setItemDarkText(itemDarkText);

                infoLayout.setLeftDrawableRes(leftRes);
                infoLayout.setLeftDrawPadding(leftDrawPadding);
                infoLayout.setRightDrawableRes(rightRes);
                infoLayout.setRightDrawPadding(rightDrawPadding);

                if (infoLayout.hasOnClickListeners() ||
                        listener != null) {
                    infoLayout.setOnClickListener(listener);
                }
            }
            return infoLayout;
        }
    }
}
