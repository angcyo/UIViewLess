package com.angcyo.uiview.less.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.ResUtil;
import com.angcyo.uiview.less.skin.SkinHelper;
import com.angcyo.uiview.view.RClickListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/09
 */
public class RSheetDialog {

    public static Builder build(@NonNull Context context) {
        return new Builder(context);
    }

    public static class Builder {

        Context context;

        boolean showCancelButton = true;

        ItemConfig itemConfig;

        String dialogTitle;

        View.OnClickListener cancelButtonClickListener;

        protected ArrayList<ItemInfo> mItemInfos = new ArrayList<>();

        BottomSheetDialog dialog;

        public Builder(@NonNull Context context) {
            this.context = context;
            dialog = new BottomSheetDialog(context);
        }

        public Builder addItem(String text, View.OnClickListener clickListener) {
            addItem(new ItemInfo(text, clickListener));
            return this;
        }

        public Builder addItem(String text, @DrawableRes int leftRes, View.OnClickListener clickListener) {
            addItem(new ItemInfo(text, leftRes, clickListener));
            return this;
        }

        public Builder addItem(ItemInfo itemInfo) {
            mItemInfos.add(itemInfo);
            //int size = mItemInfos.size();
            //addItemInner(size, size - 1, itemInfo);
            return this;
        }

        public Builder setShowCancelButton(boolean showCancelButton) {
            this.showCancelButton = showCancelButton;
            return this;
        }

        public Builder setItemConfig(ItemConfig itemConfig) {
            this.itemConfig = itemConfig;
            return this;
        }

        public Builder setDialogTitle(String dialogTitle) {
            this.dialogTitle = dialogTitle;
            return this;
        }

        public Builder setCancelButtonClickListener(View.OnClickListener cancelButtonClickListener) {
            this.cancelButtonClickListener = cancelButtonClickListener;
            return this;
        }

        public BottomSheetDialog build() {
            View rootView = LayoutInflater.from(context).inflate(R.layout.base_sheet_dialog_layout, new FrameLayout(context), false);
            RBaseViewHolder viewHolder = new RBaseViewHolder(rootView);
            LinearLayout itemContentLayout = viewHolder.v(R.id.item_content_layout);

            TextView mCancelView = viewHolder.v(R.id.cancel_view);
            TextView mTitleView = viewHolder.v(R.id.base_title_view);

            if (TextUtils.isEmpty(dialogTitle)) {
                mTitleView.setVisibility(View.GONE);
            } else {
                mTitleView.setVisibility(View.VISIBLE);
                mTitleView.setText(dialogTitle);
            }

            mCancelView.setOnClickListener(new RClickListener() {
                @Override
                public void onRClick(@Nullable View view) {
                    dialog.dismiss();
                    if (cancelButtonClickListener != null) {
                        cancelButtonClickListener.onClick(view);
                    }
                }
            });
            mCancelView.setTextColor(getColor(R.color.base_text_color));
            mCancelView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            mCancelView.setPadding(getDimensionPixelOffset(R.dimen.base_xxhdpi), 0, 0, 0);

            itemContentLayout.setBackgroundColor(Color.WHITE);
            mCancelView.setTextColor(Color.BLACK /*SkinHelper.getSkin().getThemeSubColor()*/);

            inflateItem(itemContentLayout);

            viewHolder.visible(R.id.cancel_control_layout, showCancelButton);
            itemContentLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
            viewHolder.v(R.id.line).setVisibility(View.GONE);

            if (itemConfig != null) {
                itemConfig.onLoadContent(viewHolder);
            }

            dialog.setContentView(rootView);
            return dialog;
        }

        public BottomSheetDialog show() {
            build();
            dialog.show();
            return dialog;
        }

        private int getDimensionPixelOffset(int base_xxhdpi) {
            return ResUtil.getDimen(base_xxhdpi);
        }

        private void inflateItem(ViewGroup itemContentLayout) {
            int size = mItemInfos.size();
            for (int i = 0; i < size; i++) {
                ItemInfo info = mItemInfos.get(i);
                addItemInner(itemContentLayout, size, i, info);
            }
        }

        private void addItemInner(ViewGroup itemContentLayout,
                                  int size, int i, final ItemInfo info) {
            View itemView = createItem(context, info);

            if (itemView instanceof TextView) {
                TextView textView = (TextView) itemView;
                textView.setBackgroundResource(R.drawable.base_bg_selector);
                textView.setTextColor(getColor(R.color.base_text_color));
            }

            itemView.setOnClickListener(new RClickListener(1000) {
                @Override
                public void onRClick(final View view) {
                    if (info.autoCloseDialog) {
                        dialog.dismiss();
                    }
                    if (info.mClickListener != null) {
                        info.mClickListener.onClick(view);
                    }
                }
            });

            if (itemConfig != null) {
                itemConfig.onCreateItem(itemView, info, i);
            }

            itemContentLayout.addView(itemView,
                    new ViewGroup.LayoutParams(-1,
                            context.getResources().

                                    getDimensionPixelSize(R.dimen.base_item_size)));
        }

        private int getColor(int base_text_color) {
            return ResUtil.getColor(base_text_color);
        }

    }

    public static View createItem(@NonNull Context context, @NonNull final ItemInfo info) {
        TextView textView = new TextView(context);
        textView.setText(info.mItemText);
        textView.setTextColor(SkinHelper.getSkin().getThemeSubColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SkinHelper.getSkin().getMainTextSize());
        //textView.setGravity(Gravity.CENTER);

        int offset = ResUtil.getDimen(R.dimen.base_xxhdpi);
        if (info.leftRes != 0) {
            textView.setCompoundDrawablePadding(offset);
            textView.setCompoundDrawablesWithIntrinsicBounds(info.leftRes, 0, 0, 0);
        }
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        textView.setPadding(offset, 0, 0, 0);

        return textView;
    }

    public interface ItemConfig {
        void onCreateItem(@NonNull View itemView, @NonNull ItemInfo itemInfo, int position);

        void onLoadContent(@NonNull RBaseViewHolder viewHolder);
    }


    public static class ItemInfo {
        public String mItemText;
        public View.OnClickListener mClickListener;
        public boolean autoCloseDialog = true;
        @DrawableRes
        public int leftRes = 0;

        public ItemInfo(String itemText, View.OnClickListener clickListener) {
            mItemText = itemText;
            mClickListener = clickListener;
        }

        public ItemInfo(String itemText, int leftRes, View.OnClickListener clickListener) {
            mItemText = itemText;
            this.leftRes = leftRes;
            mClickListener = clickListener;
        }

        public ItemInfo(String itemText, View.OnClickListener clickListener, boolean autoCloseDialog) {
            mItemText = itemText;
            mClickListener = clickListener;
            this.autoCloseDialog = autoCloseDialog;
        }

        public ItemInfo setItemText(String itemText) {
            mItemText = itemText;
            return this;
        }

        public ItemInfo setClickListener(View.OnClickListener clickListener) {
            mClickListener = clickListener;
            return this;
        }

        public ItemInfo setAutoCloseDialog(boolean autoCloseDialog) {
            this.autoCloseDialog = autoCloseDialog;
            return this;
        }

        public ItemInfo setLeftRes(@DrawableRes int leftRes) {
            this.leftRes = leftRes;
            return this;
        }
    }
}
