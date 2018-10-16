package com.angcyo.uiview.less.utils;

import android.app.Application;
import android.content.Context;
import com.angcyo.uiview.less.RApplication;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 16/12/08
 *     desc  : Utils初始化相关
 * </pre>
 * https://github.com/Blankj/AndroidUtilCode
 */
public class Utils {

    static Application mApplication;

    public static void init(Application application) {
        mApplication = application;
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (mApplication == null) {
            return RApplication.getApp();
        }
        return mApplication;
    }
}