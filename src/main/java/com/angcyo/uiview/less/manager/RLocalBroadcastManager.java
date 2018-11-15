package com.angcyo.uiview.less.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import com.angcyo.uiview.less.RApplication;

import java.util.Map;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：本地广播管理类
 * 创建人员：Robi
 * 创建时间：2018/03/22 12:00
 * 修改人员：Robi
 * 修改时间：2018/03/22 12:00
 * 修改备注：
 * Version: 1.0.0
 */
public class RLocalBroadcastManager {

    public static final String KEY_EXTRA = "key_extra";
    public static final String KEY_EXTRA_JSON = "key_extra_json";
    ArrayMap<String, BroadcastReceiver> broadcastReceiverArrayMap = new ArrayMap<>();

    private RLocalBroadcastManager() {
    }

    public static LocalBroadcastManager get() {
        return LocalBroadcastManager.getInstance(RApplication.getApp());
    }

    /**
     * 发送广播
     */
    public static RLocalBroadcastManager sendBroadcast(String action) {
        return sendBroadcast(action, null);
    }

    public static RLocalBroadcastManager sendBroadcast(String action, @Nullable Bundle bundle) {
        return sendBroadcast(action, bundle, null);
    }

    public static RLocalBroadcastManager sendBroadcast(String action, @Nullable Bundle bundle, @Nullable String json) {
        Intent intent = new Intent(action);
        if (bundle != null) {
            intent.putExtra(KEY_EXTRA, bundle);
        }
        if (json != null) {
            intent.putExtra(KEY_EXTRA_JSON, json);
        }
        get().sendBroadcast(intent);
        return instance();
    }

    public static RLocalBroadcastManager instance() {
        return Holder.instance;
    }

    /**
     * 注册广播
     */
    public BroadcastReceiver registerBroadcast(@NonNull Object tag, @NonNull final OnBroadcastReceiver receiver, @NonNull String... actions) {
        IntentFilter intentFilter = new IntentFilter();
        for (String a : actions) {
            intentFilter.addAction(a);
        }

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                receiver.onReceive(context, intent, action);
            }
        };

        broadcastReceiverArrayMap.put(tag.toString(), broadcastReceiver);
        get().registerReceiver(broadcastReceiver, intentFilter);
        return broadcastReceiver;
    }

    public BroadcastReceiver registerBroadcast(@NonNull Object tag, @NonNull final ArrayMap<String, BroadcastReceiver> actions) {
        IntentFilter intentFilter = new IntentFilter();
        for (String a : actions.keySet()) {
            intentFilter.addAction(a);
        }

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                actions.get(action).onReceive(context, intent);
            }
        };

        broadcastReceiverArrayMap.put(tag.toString(), broadcastReceiver);
        get().registerReceiver(broadcastReceiver, intentFilter);
        return broadcastReceiver;
    }

    /**
     * 反注册
     */
    public void unregisterBroadcast() {
        for (Map.Entry<String, BroadcastReceiver> entry : broadcastReceiverArrayMap.entrySet()) {
            BroadcastReceiver broadcastReceiver = entry.getValue();
            get().unregisterReceiver(broadcastReceiver);
        }
        broadcastReceiverArrayMap.clear();
    }

    public void unregisterBroadcast(@NonNull Object tag) {
        BroadcastReceiver broadcastReceiver = broadcastReceiverArrayMap.get(tag.toString());
        if (broadcastReceiver != null) {
            get().unregisterReceiver(broadcastReceiver);
            broadcastReceiverArrayMap.remove(tag.toString());
        }
    }

    public interface OnBroadcastReceiver {
        void onReceive(@NonNull Context context, @NonNull Intent intent, @NonNull String action);
    }

    private static class Holder {
        static RLocalBroadcastManager instance = new RLocalBroadcastManager();
    }
}
