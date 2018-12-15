package com.angcyo.uiview.less.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.*;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.ResUtil;
import com.angcyo.uiview.less.widget.group.RSoftInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import static com.angcyo.uiview.less.base.helper.TitleItemHelper.NO_NUM;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/20
 */
public class RDialog {

    static WeakHashMap<Integer, List<Dialog>> dialogMap = new WeakHashMap<>();
    static Handler mainHandle = new Handler(Looper.getMainLooper());

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

    /**
     * 加载中的对话框
     */
    public static void flow(Context context) {
        flow(context, null);
    }

    public static void flow(final Context context, final DialogInterface.OnDismissListener dismissListener) {
        mainHandle.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = build(context)
                        .setCancelable(false)
                        .setDialogWidth((int) ResUtil.dpToPx(56))
                        .setDimAmount(0f)
                        .setAnimStyleResId(R.style.WindowNoAnim)
                        .setDialogBgColor(Color.TRANSPARENT)
                        .setContentLayoutId(R.layout.base_dialog_flow_loading_layout)
                        .setOnDismissListener(dismissListener)
                        .showAlertDialog();
                List<Dialog> dialogs = dialogMap.get(context.hashCode());
                if (dialogs == null) {
                    dialogs = new ArrayList<>();
                    dialogMap.put(context.hashCode(), dialogs);
                }
                dialogs.add(alertDialog);

//                if (alertDialog.getWindow() != null) {
//                    RSoftInputLayout.hideSoftInput(alertDialog.getWindow().getDecorView());
//                }
            }
        });
    }

    public static void hide(final Context context) {
        mainHandle.post(new Runnable() {
            @Override
            public void run() {
                dismiss(dialogMap.get(context.hashCode()));
                dialogMap.remove(context.hashCode());
            }
        });
    }

    public static void hide() {
        mainHandle.post(new Runnable() {
            @Override
            public void run() {
                for (WeakHashMap.Entry<Integer, List<Dialog>> entry : dialogMap.entrySet()) {
                    dismiss(entry.getValue());
                }
                dialogMap.clear();
            }
        });
    }

    private static void dismiss(List<Dialog> dialogs) {
        if (!RUtils.isListEmpty(dialogs)) {
            for (Dialog dialog : dialogs) {
                dialog.dismiss();
            }
        }
    }

    public static Builder build(Context context) {
        return new Builder(context);
    }

    public static class Builder {

        Context context;

        @LayoutRes
        int layoutId = -1;

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

        int dialogWidth = NO_NUM;
        int dialogHeight = NO_NUM;

        /**
         * 对话框变暗指数, [0,1]
         * 0表示, 不变暗
         * 1表示, 全暗
         * NO_NUM, 默认
         */
        float amount = NO_NUM;

        /**
         * window动画资源
         */
        @StyleRes
        int animStyleResId = NO_NUM;

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
            if (!cancelable) {
                setCanceledOnTouchOutside(false);
            }
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

        //<editor-fold desc="window的配置">

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


        public Builder setDialogWidth(int dialogWidth) {
            this.dialogWidth = dialogWidth;
            return this;
        }

        public Builder setDialogHeight(int dialogHeight) {
            this.dialogHeight = dialogHeight;
            return this;
        }

        public Builder setDimAmount(@FloatRange(from = 0f, to = 1f) float amount) {
            this.amount = amount;
            return this;
        }

        public Builder setAnimStyleResId(@StyleRes int animStyleResId) {
            this.animStyleResId = animStyleResId;
            return this;
        }

        //</editor-fold>

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

            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);

            Window window = alertDialog.getWindow();
            View decorView;

            if (window != null) {
                //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                if (dialogBgDrawable != null) {
                    window.setBackgroundDrawable(dialogBgDrawable);
                }

                if (amount != NO_NUM) {
                    window.setDimAmount(amount);
                }

                if (animStyleResId != NO_NUM) {
                    window.setWindowAnimations(animStyleResId);
                }
            }

            //alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            //alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            //alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);

            alertDialog.show();

            if (window != null) {

                // window的宽高设置
                if (dialogWidth != NO_NUM && dialogHeight != NO_NUM) {
                    window.setLayout(dialogWidth, dialogHeight);
                } else {
                    if (dialogHeight != NO_NUM) {
                        window.setLayout(window.getAttributes().width, dialogHeight);
                    }
                    if (dialogWidth != NO_NUM) {
                        window.setLayout(dialogWidth, window.getAttributes().height);
                    }
                }

                decorView = window.getDecorView();
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
