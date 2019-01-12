package com.angcyo.uiview.less.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.RApplication;
import com.angcyo.uiview.less.RCrashHandler;
import com.angcyo.uiview.less.resources.ResUtil;
import com.angcyo.uiview.less.utils.utilcode.utils.AppUtils;
import kotlin.jvm.functions.Function2;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by angcyo on 2016-11-05.
 */

public class Root {
    public static String APP_FOLDER = "_app_log_folder";

    static {
        RApplication app = RApplication.getApp();
        if (app != null) {
            APP_FOLDER = app.getPackageName();
        }
    }

    public static String device_info(Context activity) {
        StringBuilder builder = new StringBuilder();
        builder.append(AppUtils.getAppVersionName(RApplication.getApp())).append(" by ");
        builder.append(ResUtil.getThemeString(RApplication.getApp(), "build_time")).append(" on ");
        builder.append(ResUtil.getThemeString(RApplication.getApp(), "os_name")).append("\n");

        builder.append(RUtils.getScreenWidth(activity)).append("×").append(RUtils.getScreenHeight(activity));
        builder.append(" ");
        builder.append(" ch:");
        builder.append(RUtils.getContentViewHeight(activity));
        builder.append(" ");
        builder.append(" dh:");
        builder.append(RUtils.getDecorViewHeight(activity));
        builder.append(" ");

        builder.append(ScreenUtil.getDensityDpi()).append(" ");
        builder.append(ScreenUtil.density()).append(" ");

        builder.append(Build.VERSION.RELEASE).append("/");
        builder.append(Build.VERSION.SDK_INT).append(" ");

        if (activity != null) {
            builder.append(RUtils.getStatusBarHeight(activity)).append("/");
            builder.append(RUtils.getNavBarHeight(activity)).append("");
        }

        builder.append("\n");
        builder.append("v:").append(Build.MANUFACTURER).append(" ");
        builder.append("m:").append(Build.MODEL).append(" ");
        builder.append("d:").append(Build.DEVICE).append(" ");
        builder.append("h:").append(Build.HARDWARE).append(" ");
        builder.append(ResUtil.getThemeString(RApplication.getApp(), "user_name"));

        return builder.toString();
    }

    public static String getAppExternalFolder() {
        return getAppExternalFolder("");
    }

    public static String externalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String sd() {
        return externalStorageDirectory();
    }

    /**
     * @param type The type of storage directory to return. Should be one of
     *             {@link Environment#DIRECTORY_MUSIC}, {@link Environment#DIRECTORY_PODCASTS},
     *             {@link Environment#DIRECTORY_RINGTONES}, {@link Environment#DIRECTORY_ALARMS},
     *             {@link Environment#DIRECTORY_NOTIFICATIONS}, {@link Environment#DIRECTORY_PICTURES},
     *             {@link Environment#DIRECTORY_MOVIES}, {@link Environment#DIRECTORY_DOWNLOADS},
     *             {@link Environment#DIRECTORY_DCIM}, or {@link Environment#DIRECTORY_DOCUMENTS}. May not be null.
     */
    public static String getExternalStoragePublicDirectory(String type) {
        return Environment.getExternalStoragePublicDirectory(type).getAbsolutePath();
    }

    /**
     * 获取录屏路径
     */
    public static String getScreenshotsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Screenshots";
    }

    public static void ensureFolder(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getAppExternalFolder(String folder) {
        if (folder == null) {
            folder = "";
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                File.separator + Root.APP_FOLDER + File.separator + folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * /data/user/0/com.angcyo.mainhost/files
     */
    public static String getAppInternalFolder(String folder) {
        if (folder == null) {
            folder = "";
        }
        File file = new File(RApplication.getApp().getFilesDir()/*getCacheDir()*/, folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * /data/user/0/com.angcyo.mainhost/app_dddddddddddd
     */
    public static String getAppInternalDir(String folder) {
        if (folder == null) {
            folder = "";
        }
        File file = RApplication.getApp().getDir(folder, Context.MODE_PRIVATE);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    private static Properties loadProperties(Function2<Properties, String, Void> function) {
        Reader reader = null;

        try {
            File file = new File(getAppExternalFolder("prop") + File.separator + "account.prop");
            if (file.exists() && file.isDirectory()) {
                file.delete();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            reader = new FileReader(file.getAbsolutePath());

            Properties pro = new Properties();
            pro.load(reader);
            if (function != null) {
                function.invoke(pro, file.getAbsolutePath());
            }
            return pro;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 写入键值对到properties
     */
    public static void storeProperties(final String key, final String value) {
        L.d("root storeProperties key : " + key + " value : " + value);
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        loadProperties(new Function2<Properties, String, Void>() {
            @Override
            public Void invoke(Properties properties, String path) {
                for (Enumeration e = properties.propertyNames(); e.hasMoreElements(); ) {
                    String s = (String) e.nextElement(); // 遍历所有元素
                    if (s.equals(key)) {
                        properties.setProperty(key, value);
                    } else {
                        properties.setProperty(s, properties.getProperty(s));
                    }
                }
                properties.setProperty(key, value);
                Writer writer = null;
                try {
                    writer = new FileWriter(path);
                    properties.store(writer, new Date().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                return null;
            }
        });
    }

    public static String loadProperties(String key) {
        L.d("root before loadProperties key : " + key);
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        String value = null;
        try {
            value = loadProperties(new Function2<Properties, String, Void>() {
                @Override
                public Void invoke(Properties properties, String writer) {
                    return null;
                }
            }).getProperty(key);
        } catch (Exception e) {
            e.printStackTrace();
            value = "";
        }
        L.d("root loadProperties key : " + key + " value : " + value);

        if (TextUtils.isEmpty(value)) {
            return "";
        } else {
            return value;
        }
    }

    /**
     * 创建时间文件名
     */
    public static String createTimeFileName() {
        return createTimeFileName("yyyy-MM-dd_HH-mm-ss-SSS");
    }

    public static String createTimeFileName(String format) {
        String dataTime = RCrashHandler.getDataTime(format);
        return dataTime;
    }

    /**
     * 创建随机文本名
     *
     * @param suffix 后缀
     */
    public static String createFileName(String suffix) {
        if (suffix == null) {
            suffix = "";
        }
        return UUID.randomUUID().toString() + suffix;
    }

    public static String createFileName() {
        return createFileName("");
    }

    /**
     * 在SD APP_FOLDER根目录下, 随机创建一个文件名路径
     */
    public static String createFilePath() {
        return createExternalFilePath();
    }

    public static String createExternalFilePath() {
        return createExternalFilePath("");
    }

    public static String createExternalFilePath(String folder) {
        return createExternalFilePath(folder, createFileName());
    }

    /**
     * 在SD卡的程序指定根目录下, 创建文件路径
     */
    public static String createExternalFilePath(String folder, String fileName) {
        return getAppExternalFolder(folder) + File.separator + fileName;
    }

    /**
     * 伪造一个自己的Imei,
     * 在内部和外部都创建一个相同id的文件, 防止重装后重新分配.
     * <p>
     * 重装后, 没有sd卡权限, 所以请在权限申请后调用此方法.
     * 无SD卡权限时, UUID 将使用内存缓存
     * <p>
     * 038d4833-5fa1-47a0-8c43-3ef3b8a9d103
     */
    public static String initImei() {
        //需要返回的uuid
        String uuid = null;
        //sd卡中存在的uuid
        String sdUuid = null;
        //内部存在的uuid
        String innerUuid = null;

        String imeiPath = getAppInternalDir("card");
        String sdUUIDPath = getAppExternalFolder(".card");

        File imeiFile = new File(imeiPath);
        File sdUUIDPathFile = new File(sdUUIDPath);

        try {
            imeiFile.mkdirs();
            sdUUIDPathFile.mkdirs();

            if (getFolderFileCount(sdUUIDPath) > 0) {
                sdUuid = sdUUIDPathFile.listFiles()[0].getName();
            }

            if (getFolderFileCount(imeiPath) > 0) {
                innerUuid = imeiFile.listFiles()[0].getName();
            }

            if (TextUtils.isEmpty(sdUuid)) {
                //外部存在中没有uuid, 有可能是没有权限, 或者手动删了
                if (TextUtils.isEmpty(innerUuid)) {
                    //干净的首次安装
                } else {
                    //内部存储有
                    uuid = innerUuid;
                    createFile(sdUUIDPath + File.separator + uuid);
                }
            } else {
                //外部存储有uuid
                uuid = sdUuid;
                clearFolder(imeiFile);
                createFile(imeiPath + File.separator + uuid);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            L.e("IMEI 初始化失败:" + e.getMessage());
        }

        if (TextUtils.isEmpty(uuid)) {
            //创建一个新的imei
            uuid = UUID.randomUUID().toString();
            createFile(imeiPath + File.separator + uuid);
            createFile(sdUUIDPath + File.separator + uuid);
        }

        return uuid;
    }

    private static void clearFolder(File folder) {
        try {
            if (folder != null && folder.isDirectory()) {
                for (File f : folder.listFiles()) {
                    f.delete();
                }
            }
        } catch (Exception e) {
            //L.e("IMEI 初始化失败:" + e.getMessage());
        }
    }

    private static void createFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            //L.e("IMEI 初始化失败:" + e.getMessage());
        }
    }

    /**
     * 返回文件夹中, 文件的数量
     */
    public static int getFolderFileCount(@Nullable String folder) {
        if (TextUtils.isEmpty(folder)) {
            return 0;
        }
        File folderFile = new File(folder);
        if (!folderFile.isDirectory()) {
            return 0;
        }
        String[] strings = folderFile.list();
        if (strings == null) {
            return 0;
        }
        return strings.length;
    }
}
