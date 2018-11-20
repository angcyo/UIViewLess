package com.angcyo.uiview.less.manager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.kotlin.ExKt;
import com.angcyo.uiview.less.utils.RUtils;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/15
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_ALARM = "com.angcyo.alarm";

    public static Intent getIntent(@NonNull Context context, Class<?> cls) {
        return getIntent(context, cls, ACTION_ALARM);
    }

    public static Intent getIntent(@NonNull Context context, Class<?> cls,
                                   String action /*自定义的广播, 需要在xml里面注册*/) {
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        return intent;
    }

    public static PendingIntent getPendingIntent(@NonNull Context context, Class<?> cls) {
        Intent intent = getIntent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public static PendingIntent getPendingIntent(@NonNull Context context, Class<?> cls,
                                                 String action /*自定义的广播, 需要在xml里面注册*/) {
        Intent intent = getIntent(context, cls, action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            L.i("收到广播:" + action);
            //将受到的广播写入文件, 用于记录
            RUtils.saveToSDCard("broadcast.log", action);
            if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(action)) {
                //重新计算闹铃时间，并调第一步的方法设置闹铃时间及闹铃间隔时间
            } else if (ACTION_ALARM.equalsIgnoreCase(action)) {
                //自定义的闹钟, 进程会被拉活

                //runApp(context);
            }
            if (context != null && action != null) {
                onReceive(context, intent, action);
            }
        }
    }

    protected void onReceive(@NonNull Context context, @NonNull Intent intent, @NonNull String action) {

    }

    protected void runApp(Context context) {
        ExKt.runMain(context);
    }
}

//系统操作	action
//监听网络变化	android.net.conn.CONNECTIVITY_CHANGE
//关闭或打开飞行模式	Intent.ACTION_AIRPLANE_MODE_CHANGED
//充电时或电量发生变化	Intent.ACTION_BATTERY_CHANGED
//电池电量低	Intent.ACTION_BATTERY_LOW
//电池电量充足（即从电量低变化到饱满时会发出广播	Intent.ACTION_BATTERY_OKAY
//系统启动完成后(仅广播一次)	Intent.ACTION_BOOT_COMPLETED
//按下照相时的拍照按键(硬件按键)时	Intent.ACTION_CAMERA_BUTTON
//屏幕锁屏	Intent.ACTION_CLOSE_SYSTEM_DIALOGS
//设备当前设置被改变时(界面语言、设备方向等)	Intent.ACTION_CONFIGURATION_CHANGED
//插入耳机时	Intent.ACTION_HEADSET_PLUG
//未正确移除SD卡但已取出来时(正确移除方法:设置--SD卡和设备内存--卸载SD卡)	Intent.ACTION_MEDIA_BAD_REMOVAL
//插入外部储存装置（如SD卡）	Intent.ACTION_MEDIA_CHECKING
//成功安装APK	Intent.ACTION_PACKAGE_ADDED
//成功删除APK	Intent.ACTION_PACKAGE_REMOVED
//重启设备	Intent.ACTION_REBOOT
//屏幕被关闭	Intent.ACTION_SCREEN_OFF
//屏幕被打开	Intent.ACTION_SCREEN_ON
//关闭系统时	Intent.ACTION_SHUTDOWN
//重启设备	Intent.ACTION_REBOOT
//
//作者：Carson_Ho
//链接：https://www.jianshu.com/p/ca3d87a4cdf3
//來源：简书
//简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
