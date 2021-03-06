package com.angcyo.uiview.less.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.angcyo.lib.L;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用程序缓存管理, 用来清理, 计算缓存大小
 * Created by angcyo on 2017/09/30 0030.
 */

public class RCacheManager {

    /**
     * 缓存目录列表
     */
    List<String> cachePaths = new ArrayList<>();

    private RCacheManager() {
    }

    public static RCacheManager instance() {
        return Holder.instance;
    }

    public static Observable<Boolean> clearCacheFolder(final String... paths) {
        return Observable
                .create(new SyncOnSubscribe<Integer, Boolean>() {
                    @Override
                    protected Integer generateState() {
                        return 1;
                    }

                    @Override
                    protected Integer next(Integer state, Observer<? super Boolean> observer) {
                        if (state > 0) {
                            for (String path : paths) {
                                deleteFolderFile(path, false);
                            }
                            observer.onNext(true);
                            observer.onCompleted();
                        }
                        return -1;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     */
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     *
     * @param filePath       filePath
     * @param deleteThisPath deleteThisPath
     */
    private static boolean deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            return deleteFolderFile(file, deleteThisPath);
        }
        return false;
    }

    private static boolean deleteFolderFile(File file, boolean deleteThisPath) {
        try {
            if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (File file1 : files) {
                    deleteFolderFile(file1.getAbsolutePath(), true);
                }
            }
            if (deleteThisPath) {
                if (!file.isDirectory()) {
                    file.delete();
                } else {
                    if (file.listFiles().length == 0) {
                        file.delete();
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取整个Cache文件夹大小
     */
    public long getAllCacheFolderSize() {
        try {
            long size = 0;
            for (String path : cachePaths) {
                size += getFolderSize(new File(path));
            }
            return size;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 打印log
     */
    public void log(Context context) {
        for (String path : cachePaths) {
            L.e(path + " -> " + Formatter.formatFileSize(context, getFolderSize(new File(path))));
        }
    }

    /**
     * 添加缓存目录, 需要用来统计大小的目录
     */
    public void addCachePath(String... paths) {
        for (String path : paths) {
            if (cachePaths.contains(path)) {
                continue;
            }
            File cache = new File(path);
            if (!cache.exists()) {
                cache.mkdirs();
            }
            cachePaths.add(path);
        }
    }

    /**
     * 添加SD根目录下的缓存目录
     */
    public void addSDCachePath(String... paths) {
        for (String path : paths) {
            addCachePath(Environment.getExternalStorageDirectory()
                    .getAbsoluteFile().getAbsolutePath() + File.separator + path);
        }
    }

    /**
     * 清理整个cache文件夹的文件
     */
    public Observable<Boolean> clearCacheFolder() {
        return Observable
                .create(new SyncOnSubscribe<Integer, Boolean>() {
                    @Override
                    protected Integer generateState() {
                        return 1;
                    }

                    @Override
                    protected Integer next(Integer state, Observer<? super Boolean> observer) {
                        if (state > 0) {
                            for (String path : cachePaths) {
                                deleteFolderFile(path, false);
                            }
                            observer.onNext(true);
                            observer.onCompleted();
                        }
                        return -1;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static class Holder {
        static RCacheManager instance = new RCacheManager();
    }
}
