package com.angcyo.uiview.less.base;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.angcyo.lib.L;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/14
 */
public abstract class BaseService extends Service implements Handler.Callback {

    protected int FOREGROUND_NOTIFICATION_ID = this.hashCode();
    protected NotificationManager mNM;
    protected final IBinder mBinder = new LocalBinder();

    public static final String KEY_COMMAND = "key_command";

    protected HandlerThread handlerThread;
    protected Handler handler;
    protected Handler mainHandler;

    public static void start(Context context, Class<? extends BaseService> cls) {
        start(context, cls, -1);
    }

    public static void start(Context context, Class<? extends BaseService> cls, int command) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(KEY_COMMAND, command);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //在 API 28 上, 需要 android.permission.FOREGROUND_SERVICE 权限
            context.startForegroundService(intent);
        } else {
            // Pre-O behavior.
            context.startService(intent);
        }
    }

    public static void start(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //在 API 28 上, 需要 android.permission.FOREGROUND_SERVICE 权限
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
        handlerThread = new HandlerThread(this.getClass().getSimpleName());
        handlerThread.start();

        handler = new Handler(handlerThread.getLooper(), this);
        mainHandler = new Handler(getMainLooper());

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
//        build.setContentTitle("服务运行于前台")
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
        handlerThread.quitSafely();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = -1;
        if (intent != null) {
            command = intent.getIntExtra(KEY_COMMAND, command);
        }
        L.i("onStartCommand-> " + " command:" + command + " flags:" + flags + " startId:" + startId + "\n" + intent);
        if (intent != null) {
            onHandCommand(command, intent);
        }
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

    protected void onHandCommand(int command, @NonNull Intent intent) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg == null) {
            return false;
        } else {
            return onHandleMessage(msg);
        }
    }

    public boolean onHandleMessage(@NonNull Message msg) {
        return false;
    }

    public void postDelayThread(long delay /*毫秒*/, Runnable run) {
        if (handler != null) {
            handler.postDelayed(run, delay);
        }
    }

    public void removeDelayThread(Runnable run) {
        if (handler != null) {
            handler.removeCallbacks(run);
        }
    }

    public void postDelayMain(long delay /*毫秒*/, Runnable run) {
        if (mainHandler != null) {
            mainHandler.postDelayed(run, delay);
        }
    }

    public void removeDelayMain(Runnable run) {
        if (mainHandler != null) {
            mainHandler.removeCallbacks(run);
        }
    }

    public class LocalBinder extends Binder {
        BaseService getService() {
            return BaseService.this;
        }
    }
}
