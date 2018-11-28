package com.angcyo.uiview.less.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

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

    public static AlertDialog.Builder builder(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        return builder;
    }
}
