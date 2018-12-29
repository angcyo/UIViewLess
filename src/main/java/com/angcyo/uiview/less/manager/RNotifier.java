/************************************************************
 *  * Hyphenate CONFIDENTIAL 
 * __________________ 
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved. 
 *
 * NOTICE: All information contained herein is, and remains 
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material 
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package com.angcyo.uiview.less.manager;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.widget.Toast;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.utils.T_;
import com.angcyo.uiview.less.utils.ThreadExecutor;

import java.util.*;

/**
 * new message notifier class
 * <p>
 * this class is subject to be inherited and implement the relative APIs
 */
public class RNotifier {
    private final static String TAG = "RNotifier";
    protected NotificationManager notificationManager = null;
    protected Context appContext;
    protected String packageName;
    protected long lastNotifiyTime;
    protected AudioManager audioManager;
    protected Vibrator vibrator;
    Ringtone ringtone = null;

    //需要震动
    private boolean needVibrator = true;
    //需要铃声
    private boolean needRingtone = true;

    private RNotifier() {
    }

    public static RNotifier instance() {
        return Holder.instance;
    }

    /**
     * this function can be override
     *
     * @param context
     * @return
     */
    public RNotifier init(Context context) {
        appContext = context.getApplicationContext();

        packageName = appContext.getApplicationInfo().packageName;
        if (Locale.getDefault().getLanguage().equals("zh")) {
        } else {
        }

        //音频管理
        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        //震动管理
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);

        return this;
    }

    /**
     * vibrate and  play tone
     */
    public void vibrateAndPlayTone() {
        if (System.currentTimeMillis() - lastNotifiyTime < 1000) {
            // received new messages within 2 seconds, skip play ringtone
            return;
        }

        try {
            lastNotifiyTime = System.currentTimeMillis();

            // check if in silent mode
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                L.e(TAG, "in slient mode now");
                return;
            }

            if (needVibrator) {
                //开始震动
                long[] pattern = new long[]{0, 180, 80, 120};
                vibrator.vibrate(pattern, -1);
            }

            //播放铃声
            if (ringtone == null) {
                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
                if (ringtone == null) {
                    L.d(TAG, "cant find ringtone at:" + notificationUri.getPath());
                    return;
                }
            }

            if (needRingtone && !ringtone.isPlaying()) {
                String vendor = Build.MANUFACTURER;

                ringtone.play();
                // for samsung S3, we meet a bug that the phone will
                // continue ringtone without stop
                // so add below special handler to stop it after 3s if
                // needed
                if (vendor != null && vendor.toLowerCase().contains("samsung")) {
                    ThreadExecutor.instance().onThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000);
                                if (ringtone.isPlaying()) {
                                    ringtone.stop();
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Builder build(@NonNull Context context) {
        return new Builder(context);
    }

    public RNotifier setNeedVibrator(boolean needVibrator) {
        this.needVibrator = needVibrator;
        return this;
    }

    public RNotifier setNeedRingtone(boolean needRingtone) {
        this.needRingtone = needRingtone;
        return this;
    }

    private static class Holder {
        static RNotifier instance = new RNotifier();
    }

    int notificationId = 0;
    String lowChannelId = "NotificationChannel_NOTIFIER_LOW";
    String highChannelId = "NotificationChannel_NOTIFIER_HIGH";

    /**
     * 创建通知通道
     */
    private void initChannel() {
        this.notificationId = new Random(System.currentTimeMillis()).nextInt(10_000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            lowChannelId = String.valueOf(notificationId);
            NotificationChannel channelProgress = new NotificationChannel(lowChannelId,
                    "NotificationChannel_NOTIFIER_LOW",
                    NotificationManager.IMPORTANCE_LOW);

            channelProgress.enableLights(false);
            channelProgress.enableVibration(false);
            channelProgress.setSound(null, null);

            highChannelId = String.valueOf(notificationId + 200);
            NotificationChannel channelFinish = new NotificationChannel(highChannelId,
                    "NotificationChannel_NOTIFIER_HIGH",
                    NotificationManager.IMPORTANCE_HIGH);

            channelFinish.enableLights(true);
            channelFinish.enableVibration(true);
            channelFinish.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);

            List<NotificationChannel> channelList = new ArrayList<>();
            channelList.add(channelProgress);
            channelList.add(channelFinish);
            notificationManager.createNotificationChannels(channelList);
        }
    }

    /**
     * 取消通知
     */
    public static void cancel(@NonNull Context context, int notifyID) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.cancel(notifyID);
    }

    public static PendingIntent getActivityPendingIntent(Context context, int requestCode,
                                                         @NonNull Intent intent) {
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT, null);
    }

    public static PendingIntent getMainPendingIntent(Context context, int requestCode) {
        return getActivityPendingIntent(context, requestCode,
                context.getPackageManager().getLaunchIntentForPackage(context.getPackageName())
        );
    }

    /**
     * 2018-12-29
     * 通知构造类
     */
    public static class Builder {
        static String CHANNEL_ID = null;
        Context context;

        String channelId = "NotificationChannel_Notify";
        String channelName = channelId;
        NotificationManager nm;

        int notifyId;

        public Builder(@NonNull Context context) {
            this.context = context;
            notifyId = (int) (System.currentTimeMillis() % 1_000_000_000);
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        /**
         * 可选
         */
        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        /**
         * 可选
         */
        public Builder setChannelName(String channelName) {
            this.channelName = channelName;
            return this;
        }

        /**
         * 创建通知通道
         */
        private void initChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (CHANNEL_ID == null || !TextUtils.equals(CHANNEL_ID, channelId)) {
                    NotificationChannel channel = new NotificationChannel(channelId,
                            channelName,
                            NotificationManager.IMPORTANCE_HIGH);

                    //允许这个渠道下的通知显示角标
                    channel.setShowBadge(true);
                    channel.enableLights(true);
                    channel.enableVibration(true);
                    channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                    nm.createNotificationChannel(channel);

                    if (CHANNEL_ID == null) {
                        CHANNEL_ID = channelId;
                    }
                }
            }
        }

        /**
         * https://www.jianshu.com/p/6aec3656e274
         */
        public NotificationCompat.Builder build() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
            builder//设置通知标题
                    .setContentTitle(contentTitle)
                    //设置通知内容
                    .setContentText(contentText)
                    .setTicker(contentText)
                    //设置通知左侧的小图标
                    .setSmallIcon(smallIcon)
                    //设置通知右侧的大图标
                    .setLargeIcon(largeIcon)
                    .setOngoing(ongoing)
                    //未读消息的数量。https://mp.weixin.qq.com/s/Ez-G_9hzUCOjU8rRnsW8SA
                    .setNumber(0)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setShowWhen(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setContentInfo("Info")
                    //设置点击通知时的响应事件
                    .setContentIntent(clickIntent)
                    //设置点击通知后自动删除通知
                    .setAutoCancel(autoCancel)
                    //设置删除通知时的响应事件
                    .setDeleteIntent(deleteIntent)
            //.setStyle()
            //.setCustomContentView()
            //.setCustomBigContentView()
            //.setCustomBigContentView()

            ;
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);

            if (progress >= 0) {
                builder.setProgress(progressMax, progress, progressIndeterminate);
            }

            if (!actions.isEmpty()) {
                for (NotificationCompat.Action action : actions) {
                    builder.addAction(action);
                }
            }
            return builder;
        }

        CharSequence contentTitle;
        CharSequence contentText;

        /**
         * 必须要的的参数
         */
        @DrawableRes
        int smallIcon = R.drawable.base_info;
        Bitmap largeIcon;

        boolean ongoing = false;
        boolean autoCancel = true;

        ArrayList<NotificationCompat.Action> actions = new ArrayList<>();

        public Builder setNotifyId(int notifyId) {
            this.notifyId = notifyId;
            return this;
        }

        public Builder setContentTitle(CharSequence contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder setContentText(CharSequence contentText) {
            this.contentText = contentText;
            return this;
        }

        public Builder setSmallIcon(@DrawableRes int smallIcon) {
            this.smallIcon = smallIcon;
            return this;
        }

        public Builder setLargeIcon(@DrawableRes int largeIcon) {
            setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    largeIcon));
            return this;
        }

        public Builder setLargeIcon(Bitmap largeIcon) {
            this.largeIcon = largeIcon;
            return this;
        }

        public Builder setOngoing(boolean ongoing) {
            this.ongoing = ongoing;
            return this;
        }

        public Builder setAutoCancel(boolean autoCancel) {
            this.autoCancel = autoCancel;
            return this;
        }

        int progressMax = 100;
        /**
         * 大于-1, 激活进度显示
         */
        int progress = -1;
        boolean progressIndeterminate = false;

        public Builder setProgressMax(int progressMax) {
            this.progressMax = progressMax;
            return this;
        }

        public Builder setProgress(int progress) {
            this.progress = progress;
            return this;
        }

        public Builder setProgressIndeterminate(boolean progressIndeterminate) {
            this.progressIndeterminate = progressIndeterminate;
            return this;
        }

        public Builder addAction(int icon, CharSequence title, PendingIntent intent) {
            this.actions.add(new NotificationCompat.Action(icon, title, intent));
            return this;
        }

        public Builder addAction(NotificationCompat.Action action) {
            this.actions.add(action);
            return this;
        }

        PendingIntent clickIntent;
        PendingIntent deleteIntent;

        public Builder setClickDoMain() {
            clickIntent = getMainPendingIntent(context, notifyId);
            return this;
        }

        public Builder setClickIntent(PendingIntent clickIntent) {
            this.clickIntent = clickIntent;
            return this;
        }

        public Builder setDeleteIntent(PendingIntent deleteIntent) {
            this.deleteIntent = deleteIntent;
            return this;
        }

        public int doIt() {
            if (context == null) {
                return -1;
            }

            initChannel();

            Notification notify = build()
                    .build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = nm.getNotificationChannel(channelId);
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.getId());
                    context.startActivity(intent);
                    T_.error("请手动将通知打开");
                    return notifyId;
                }
            }

            nm.notify(channelName, notifyId, notify);

            L.w("显示通知:" + notifyId);
            return notifyId;
        }
    }
}
