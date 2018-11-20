package com.angcyo.uiview.less.utils;

import com.angcyo.uiview.less.RCrashHandler;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 将日志写入文件, 10MB后, 自动重命名保存新文件
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/20
 */
public class RLogFile {

    /**
     * 默认在 sd卡/包名/log/ 文件夹下
     */
    public static String DEFAULT_LOG_FILE_NAME = "runtime.log";
    public static long MIN_SIZE = 500 * 1024 * 1024;//500mb

    public static void log(String data) {
        clearOldLog();
        RUtils.saveToSDCard(DEFAULT_LOG_FILE_NAME, data);
    }

    /*如果磁盘空间不足, 清理之前的路径*/
    private static void clearOldLog() {
        try {
            //52768120832 39702720512  52.77吉字节 39.70吉字节

            //SD卡剩余空间大小
            long externalMemorySize = RCrashHandler.getAvailableExternalMemorySize();
            //SD卡总大小
            //long totalExternalMemorySize = RCrashHandler.getTotalExternalMemorySize();
            if (externalMemorySize <= MIN_SIZE) {
                //不足500MB

                String saveFolder = Root.getAppExternalFolder(RUtils.DEFAULT_LOG_FOLDER_NAME);
                File folder = new File(saveFolder);

                File[] files = folder.listFiles();
                if (files != null && files.length > 3) {
                    List<File> fileList = Arrays.asList(files);
                    Collections.sort(fileList);

                    for (File f : fileList) {
                        try {
                            f.delete();
                            clearOldLog();
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
