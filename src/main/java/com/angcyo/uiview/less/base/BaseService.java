package com.angcyo.uiview.less.base;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/14
 */
public class BaseService extends Service {

    protected int FOREGROUND_NOTIFICATION_ID = this.hashCode();
    protected NotificationManager mNM;
    protected final IBinder mBinder = new LocalBinder();

    public static void start(Context context, Class<? extends BaseService> cls) {
        Intent intent = new Intent(context, cls);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            // Pre-O behavior.
            context.startService(intent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Android 8.0 5秒内调用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification());
        }
    }

    /**
     * xxx 正在运行
     *
     * @see android.app.job.JobScheduler
     * @see androidx.work.WorkManager
     */
    protected Notification buildForegroundNotification() {
        String channelId = this.getClass().getSimpleName();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId,
                    channelId,
                    NotificationManager.IMPORTANCE_NONE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNM.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        //已下设置都不会生效
//        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        builder.setContentTitle("服务运行于前台")
//                .setContentText("service被设为前台进程")
//                .setTicker("service正在后台运行...")
//                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .setWhen(System.currentTimeMillis())
//                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNM.cancel(FOREGROUND_NOTIFICATION_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void showNotification() {
//        // In this sample, we'll use the same text for the ticker and the expanded notification
//        CharSequence text = getText(R.string.local_service_started);
//
//        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, LocalServiceActivities.Controller.class), 0);
//
//        // Set the info for the views that show in the notification panel.
//        Notification notification = new Notification.Builder(this)
//                .setSmallIcon(R.drawable.stat_sample)  // the status icon
//                .setTicker(text)  // the status text
//                .setWhen(System.currentTimeMillis())  // the time stamp
//                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
//                .setContentText(text)  // the contents of the entry
//                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
//                .build();
//
//        // Send the notification.
//        mNM.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }

    public class LocalBinder extends Binder {
        BaseService getService() {
            return BaseService.this;
        }
    }
}
