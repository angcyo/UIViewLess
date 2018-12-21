package com.angcyo.uiview.less.utils;

import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/20
 */
public class RPopup {

    /**
     * 强制显示Menu 图标, 请在show(), 方法之前调用
     */
    public static void showMenuIco(PopupMenu popup) {
        try {
            Field mPopUpField = popup.getClass().getDeclaredField("mPopup");
            mPopUpField.setAccessible(true);
            MenuPopupHelper mPopup = (MenuPopupHelper) mPopUpField.get(popup);
            Method setForceShowIcon = mPopup.getClass().getDeclaredMethod("setForceShowIcon", Boolean.class);
            setForceShowIcon.invoke(mPopup, true);
        } catch (Exception e) {

        }
    }
}
