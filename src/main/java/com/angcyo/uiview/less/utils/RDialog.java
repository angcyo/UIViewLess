package com.angcyo.uiview.less.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.ResUtil;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/20
 */
public class RDialog {

    public static void tip(Context context,
                           String message) {
        show(context, true, null, message, "我知道了");
    }

    public static void tip(Context context,
                           String message,
                           String okText) {
        show(context, true, null, message, okText);
    }

    public static void tip(Context context,
                           String title,
                           String message,
                           String okText) {
        show(context, true, title, message, okText);
    }

    public static void show(Context context,
                            boolean cancelable,
                            String title,
                            String message,
                            String okText) {
        build(context)
                .setCancelable(cancelable)
                .setDialogTitle(title)
                .setDialogMessage(message)
                .setPositiveButtonText(okText)
                .showAlertDialog();
    }

    //DialogInterface.BUTTON_POSITIVE

    public static Builder build(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        Context context;


        @LayoutRes
        int layoutId;

        /**
         * 优先使用 contentView, 其次再使用 layoutId
         */
        View contentView;

        /**
         * 是否可以cancel
         */
        boolean cancelable = true;

        boolean canceledOnTouchOutside = true;

        String dialogTitle = "";
        String dialogMessage = "";

        OnInitListener initListener;

        Drawable dialogBgDrawable;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        public Builder setContentView(@NonNull View view) {
            contentView = view;
            return this;
        }

        public Builder setContentLayoutId(@LayoutRes int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
            this.canceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        public Builder setDialogTitle(String dialogTitle) {
            this.dialogTitle = dialogTitle;
            return this;
        }

        public Builder setDialogMessage(String dialogMessage) {
            this.dialogMessage = dialogMessage;
            return this;
        }

        public Builder setInitListener(OnInitListener initListener) {
            this.initListener = initListener;
            return this;
        }

        public Builder setDialogBgDrawable(@NonNull Drawable dialogBgDrawable) {
            this.dialogBgDrawable = dialogBgDrawable;
            return this;
        }

        public Builder setDialogBgColor(@ColorInt int color) {
            return setDialogBgDrawable(new ColorDrawable(color));
        }

        public Builder setDialogBgResource(@DrawableRes int drawable) {
            return setDialogBgDrawable(ResUtil.getDrawable(drawable));
        }

        //<editor-fold desc="系统默认3个按钮设置">

        /**
         * 系统默认3个按钮设置
         */
        CharSequence positiveButtonText;
        CharSequence negativeButtonText;
        CharSequence neutralButtonText;

        DialogInterface.OnClickListener positiveButtonListener;
        DialogInterface.OnClickListener negativeButtonListener;
        DialogInterface.OnClickListener neutralButtonListener;

        DialogInterface.OnDismissListener onDismissListener;

        public Builder setPositiveButtonText(CharSequence positiveButtonText) {
            this.positiveButtonText = positiveButtonText;
            return this;
        }

        public Builder setNegativeButtonText(CharSequence negativeButtonText) {
            this.negativeButtonText = negativeButtonText;
            return this;
        }

        public Builder setNeutralButtonText(CharSequence neutralButtonText) {
            this.neutralButtonText = neutralButtonText;
            return this;
        }

        public Builder setPositiveButtonListener(DialogInterface.OnClickListener positiveButtonListener) {
            this.positiveButtonListener = positiveButtonListener;
            return this;
        }

        public Builder setNegativeButtonListener(DialogInterface.OnClickListener negativeButtonListener) {
            this.negativeButtonListener = negativeButtonListener;
            return this;
        }

        public Builder setNeutralButtonListener(DialogInterface.OnClickListener neutralButtonListener) {
            this.neutralButtonListener = neutralButtonListener;
            return this;
        }

        public Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

        //</editor-fold>

        public AlertDialog showAlertDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(cancelable);

            if (!TextUtils.isEmpty(dialogTitle)) {
                builder.setTitle(dialogTitle);
            }
            if (!TextUtils.isEmpty(dialogMessage)) {
                builder.setMessage(dialogMessage);
            }

            if (onDismissListener != null) {
                builder.setOnDismissListener(onDismissListener);
            }

            //积极的按钮 DialogInterface.BUTTON_POSITIVE
            if (!TextUtils.isEmpty(positiveButtonText)) {
                builder.setPositiveButton(positiveButtonText, positiveButtonListener);
            }
            //消极的按钮 DialogInterface.BUTTON_NEGATIVE
            if (!TextUtils.isEmpty(negativeButtonText)) {
                builder.setPositiveButton(negativeButtonText, negativeButtonListener);
            }
            //中立的按钮 DialogInterface.BUTTON_NEUTRAL
            if (!TextUtils.isEmpty(neutralButtonText)) {
                builder.setPositiveButton(neutralButtonText, neutralButtonListener);
            }

            if (contentView != null) {
                builder.setView(contentView);
            } else if (layoutId != -1) {
                builder.setView(layoutId);
            }

            AlertDialog alertDialog = builder.show();
            alertDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);

            Window window = alertDialog.getWindow();
            if (window != null) {
                //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                View decorView = window.getDecorView();

                if (dialogBgDrawable != null) {
                    window.setBackgroundDrawable(dialogBgDrawable);
                }

                if (initListener != null) {
                    initListener.onInitDialog(alertDialog, new RBaseViewHolder(decorView));
                }
            }

            //alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            //alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            //alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            return alertDialog;
        }

    }

    public static abstract class OnInitListener {
        public void onInitDialog(@NonNull AlertDialog dialog, @NonNull RBaseViewHolder dialogViewHolder) {

        }
    }
}
