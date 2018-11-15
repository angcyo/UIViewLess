package com.angcyo.uiview.less.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * 定时任务管理
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/15
 */
public class RAlarmManager {

    /**
     * @param time   确定的一个时间, 毫秒
     * @param intent 需要执行的操作
     */
    public static void setAt(@NonNull Context context, long time, @NonNull PendingIntent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, time, intent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, time, intent);
        }
    }

    /**
     * @param time 延迟多少毫秒之后触发
     */
    public static void setDelay(@NonNull Context context, long time, @NonNull PendingIntent intent) {
        setAt(context, System.currentTimeMillis() + time, intent);
    }

    /**
     * 取消定时
     */
    public static void cancel(@NonNull Context context, @NonNull PendingIntent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(intent);
    }
}
