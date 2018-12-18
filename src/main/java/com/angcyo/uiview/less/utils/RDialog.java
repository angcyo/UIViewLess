package com.angcyo.uiview.less.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.*;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.kotlin.ViewExKt;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.ResUtil;
import com.angcyo.uiview.less.widget.ExEditText;
import com.angcyo.uiview.less.widget.group.RSoftInputLayout;
import com.angcyo.uiview.less.widget.pager.TextIndicator;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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

    public static InputBuilder input(@NonNull Context context) {
        return new InputBuilder(context);
    }

    /**
     * 加载中的对话框
     */
    public static void flow(Context context) {
        flow(context, null);
    }

    public static void flow(final Context context, final DialogInterface.OnDismissListener dismissListener) {
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
    }

    public static void hide(final Context context) {
        cancel(dialogMap.remove(context.hashCode()));
    }

    public static void hide() {
        final WeakHashMap<Integer, List<Dialog>> hashMap = new WeakHashMap<>(RDialog.dialogMap);
        RDialog.dialogMap.clear();

        for (WeakHashMap.Entry<Integer, List<Dialog>> entry : hashMap.entrySet()) {
            cancel(entry.getValue());
        }
        hashMap.clear();
    }

    private static void cancel(List<Dialog> dialogs) {
        if (!RUtils.isListEmpty(dialogs)) {
            for (Dialog dialog : dialogs) {
                dialog.cancel();
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
        int dialogGravity = NO_NUM;

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

        public Builder setDialogGravity(int dialogGravity) {
            this.dialogGravity = dialogGravity;
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
        DialogInterface.OnCancelListener onCancelListener;

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

        public Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
            this.onCancelListener = onCancelListener;
            return this;
        }

        //</editor-fold>

        /**
         * 配置window特性, 需要在setContentView之前调用
         */
        private void configWindow(@NonNull Dialog dialog) {
            Window window = dialog.getWindow();

            if (window != null) {

                if (dialog instanceof AlertDialog) {
                } else {
                    //window.requestFeature(Window.FEATURE_NO_TITLE);
                    //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        window.setNavigationBarColor(SkinHelper.getSkin().getThemeColor());
//                    }
                }

                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
        }

        private void configDialog(@NonNull Dialog dialog) {
            dialog.setCancelable(cancelable);

            if (!TextUtils.isEmpty(dialogTitle)) {
                dialog.setTitle(dialogTitle);
            }

            if (onDismissListener != null) {
                dialog.setOnDismissListener(onDismissListener);
            }

            if (onCancelListener != null) {
                dialog.setOnCancelListener(onCancelListener);
            }

            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);


            Window window = dialog.getWindow();
            View decorView;

            configWindow(dialog);

            if (dialog instanceof AlertDialog) {

            } else {
                if (contentView != null) {
                    dialog.setContentView(contentView);
                } else if (layoutId != -1) {
                    dialog.setContentView(layoutId);
                }
            }

            //显示对话框
            dialog.show();

            if (window != null) {
                WindowManager.LayoutParams attributes = window.getAttributes();

                // window的宽高设置
                if (dialogWidth != NO_NUM && dialogHeight != NO_NUM) {
                    window.setLayout(dialogWidth, dialogHeight);
                } else {
                    if (dialogHeight != NO_NUM) {
                        window.setLayout(attributes.width, dialogHeight);
                    }
                    if (dialogWidth != NO_NUM) {
                        window.setLayout(dialogWidth, attributes.height);
                    }
                }

                if (dialogGravity != NO_NUM) {
                    attributes.gravity = dialogGravity;
                    window.setAttributes(attributes);
                }

                decorView = window.getDecorView();
                if (initListener != null) {
                    initListener.onInitDialog(dialog, new RBaseViewHolder(decorView));
                }
            }

        }

        public AlertDialog showAlertDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            if (!TextUtils.isEmpty(dialogMessage)) {
                builder.setMessage(dialogMessage);
            }

            //积极的按钮 DialogInterface.BUTTON_POSITIVE
            if (!TextUtils.isEmpty(positiveButtonText)) {
                builder.setPositiveButton(positiveButtonText, positiveButtonListener);
            }
            //消极的按钮 DialogInterface.BUTTON_NEGATIVE
            if (!TextUtils.isEmpty(negativeButtonText)) {
                builder.setNegativeButton(negativeButtonText, negativeButtonListener);
            }
            //中立的按钮 DialogInterface.BUTTON_NEUTRAL
            if (!TextUtils.isEmpty(neutralButtonText)) {
                builder.setNeutralButton(neutralButtonText, neutralButtonListener);
            }

            if (contentView != null) {
                builder.setView(contentView);
            } else if (layoutId != -1) {
                builder.setView(layoutId);
            }

            AlertDialog alertDialog = builder.create();
            configDialog(alertDialog);

            //alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            //alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            //alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            return alertDialog;
        }

        public BottomSheetDialog showSheetDialog() {
            BottomSheetDialog sheetDialog = new BottomSheetDialog(context);
            configDialog(sheetDialog);
            return sheetDialog;
        }

        public AppCompatDialog showCompatDialog() {
            AppCompatDialog dialog = new AppCompatDialog(context);
            dialog.requestWindowFeature(0);
            configDialog(dialog);
            return dialog;
        }
    }

    public static class InputBuilder {
        @NonNull
        Context context;
        int maxInputLength = 0;
        int inputViewHeight = -1;

        /**
         * 文本框hint文本
         */
        String hintInputString = "请输入...";

        /**
         * 左上角标题提示
         */
        String tipInputString = "";
        /**
         * 缺省的文本框内容
         */
        String defaultInputString = "";

        String saveButtonText = null;

        boolean showSoftInput = false;

        /**
         * 是否允许输入为空
         */
        boolean canInputEmpty = true;

        boolean useCharLengthFilter = false;

        OnInputListener inputListener;

        OnInitListener initListener;

        public InputBuilder setMaxInputLength(int maxInputLength) {
            this.maxInputLength = maxInputLength;
            return this;
        }

        public InputBuilder setInputViewHeight(int inputViewHeight) {
            this.inputViewHeight = inputViewHeight;
            return this;
        }

        public InputBuilder setHintInputString(String hintInputString) {
            this.hintInputString = hintInputString;
            return this;
        }

        public InputBuilder setTipInputString(String tipInputString) {
            this.tipInputString = tipInputString;
            return this;
        }

        public InputBuilder setDefaultInputString(String defaultInputString) {
            this.defaultInputString = defaultInputString;
            return this;
        }

        public InputBuilder setShowSoftInput(boolean showSoftInput) {
            this.showSoftInput = showSoftInput;
            return this;
        }

        public InputBuilder setInputListener(OnInputListener inputListener) {
            this.inputListener = inputListener;
            return this;
        }

        public InputBuilder setInitListener(OnInitListener initListener) {
            this.initListener = initListener;
            return this;
        }

        public InputBuilder setCanInputEmpty(boolean canInputEmpty) {
            this.canInputEmpty = canInputEmpty;
            return this;
        }

        public InputBuilder setUseCharLengthFilter(boolean useCharLengthFilter) {
            this.useCharLengthFilter = useCharLengthFilter;
            return this;
        }

        public InputBuilder setSaveButtonText(String saveButtonText) {
            this.saveButtonText = saveButtonText;
            return this;
        }

        public InputBuilder(@NonNull Context context) {
            this.context = context;
        }

        public void doIt() {
            RDialog.build(context)
                    .setContentLayoutId(R.layout.dialog_base_input_layout)
                    .setCanceledOnTouchOutside(false)
                    .setDialogWidth(-1)
                    .setDialogHeight(-2)
                    .setDialogBgColor(Color.TRANSPARENT)
                    .setDialogGravity(Gravity.BOTTOM)
                    .setInitListener(new OnInitListener() {
                        @Override
                        public void onInitDialog(@NonNull final Dialog dialog, @NonNull final RBaseViewHolder dialogViewHolder) {
                            final ExEditText editView = dialogViewHolder.v(R.id.base_edit_text_view);
                            TextIndicator indicatorView = dialogViewHolder.v(R.id.base_single_text_indicator_view);

                            TextView tipView = dialogViewHolder.v(R.id.base_input_tip_view);
                            if (!TextUtils.isEmpty(tipInputString)) {
                                tipView.setVisibility(View.VISIBLE);
                                tipView.setText(tipInputString);
                            }

                            if (!canInputEmpty) {
                                ViewExKt.onEmptyText(editView, new Function1<Boolean, Unit>() {
                                    @Override
                                    public Unit invoke(Boolean aBoolean) {
                                        dialogViewHolder.enable(R.id.base_save_button, !aBoolean);
                                        return null;
                                    }
                                });
                                dialogViewHolder.enable(R.id.base_save_button, !TextUtils.isEmpty(defaultInputString));
                            }

                            if (useCharLengthFilter) {
                                editView.setUseCharLengthFilter(useCharLengthFilter);
                            }

                            if (maxInputLength >= 0) {
                                editView.setMaxLength(maxInputLength);
                                indicatorView.setVisibility(View.VISIBLE);
                                indicatorView.initIndicator(maxInputLength, editView);
                            }

                            if (inputViewHeight > 0) {
                                UI.setViewHeight(editView, inputViewHeight);
                                editView.setGravity(Gravity.TOP);
                            } else {
                                editView.setGravity(Gravity.CENTER_VERTICAL);
                                editView.setSingleLine(true);
                                editView.setMaxLines(1);
                            }

                            editView.setHint(hintInputString);
                            editView.setInputText(defaultInputString);

                            if (saveButtonText != null) {
                                dialogViewHolder.tv(R.id.base_save_button).setText(saveButtonText);
                            }
                            dialogViewHolder.click(R.id.base_save_button, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    boolean canCancel = true;
                                    if (inputListener != null) {
                                        canCancel = !inputListener.onSaveClick(dialogViewHolder, editView, editView.string());
                                    }

                                    if (canCancel) {
                                        if (inputListener != null) {
                                            inputListener.onInputString(editView.string());
                                        }
                                        dialog.cancel();
                                    }
                                }
                            });

                            if (showSoftInput) {
                                dialogViewHolder.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showSoftInput(editView);
                                    }
                                });
                            }

                            if (initListener != null) {
                                initListener.onInitDialog(dialog, dialogViewHolder);
                            }
                        }
                    })
                    .showCompatDialog();
        }

        private void showSoftInput(View view) {
            RSoftInputLayout.showSoftInput(view);
        }
    }

    public static abstract class OnInitListener {
        public abstract void onInitDialog(@NonNull Dialog dialog, @NonNull RBaseViewHolder dialogViewHolder);
    }

    public static abstract class OnInputListener {
        public boolean onSaveClick(@NonNull RBaseViewHolder dialogViewHolder, @NonNull ExEditText editView, @NonNull String input) {
            return false;
        }

        @NonNull
        public abstract void onInputString(@NonNull String input);
    }
}
