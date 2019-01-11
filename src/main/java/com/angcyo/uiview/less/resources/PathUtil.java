package com.angcyo.uiview.less.resources;

import android.graphics.Path;
import android.support.annotation.NonNull;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/01/11
 */
public class PathUtil {
    /**
     * 在 Path 中, 添加一个 `勾号` `√`的path
     */
    public static void addTickPath(@NonNull Path path,
                                   int startX, int startY,
                                   int width, int height) {
        path.moveTo(startX, startY + height / 2);
        path.lineTo(startX + width / 3, startY + height);
        path.lineTo(startX + width, startY);
    }

    /**
     * add `×` path
     */
    public static void addCrossPath(@NonNull Path path,
                                    int startX, int startY,
                                    int width, int height) {

        // \
        path.moveTo(startX, startY);
        path.lineTo(startX + width, startY + height);

        // /

        //从右上角往左下角绘制
        path.moveTo(startX + width, startY);
        path.lineTo(startX, startY + height);

        //从左下角往右上角绘制
        //path.moveTo(startX, startY + height);
        //path.lineTo(startX + width, startY);
    }
}
