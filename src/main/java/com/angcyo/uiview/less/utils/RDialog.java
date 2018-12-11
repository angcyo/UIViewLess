package com.angcyo.uiview.less.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(cancelable);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        builder.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    //DialogInterface.BUTTON_POSITIVE

    public static Builder builder(Context context) {
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

        public AlertDialog showAlertDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(cancelable);

            if (!TextUtils.isEmpty(dialogTitle)) {
                builder.setTitle(dialogTitle);
            }
            if (!TextUtils.isEmpty(dialogMessage)) {
                builder.setMessage(dialogMessage);
            }

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    L.i(dialog);
                }
            });

            //积极的按钮 DialogInterface.BUTTON_POSITIVE
            builder.setPositiveButton("setPositiveButton", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    L.i(which);
                }
            });
            //消极的按钮 DialogInterface.BUTTON_NEGATIVE
            builder.setNegativeButton("setNegativeButton", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    L.i(which);
                }
            });
            //中立的按钮 DialogInterface.BUTTON_NEUTRAL
            builder.setNeutralButton("setNeutralButton", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    L.i(which);
                }
            });

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
                if (initListener != null) {
                    initListener.onInitDialog(alertDialog, new RBaseViewHolder(decorView));
                }
            }
            return alertDialog;
        }

    }

    public static abstract class OnInitListener {
        public void onInitDialog(@NonNull AlertDialog dialog, @NonNull RBaseViewHolder dialogViewHolder) {

        }
    }
}
