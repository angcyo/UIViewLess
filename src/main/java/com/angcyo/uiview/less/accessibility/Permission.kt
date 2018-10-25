package com.angcyo.uiview.less.accessibility

import android.app.Activity
import android.support.v7.app.AlertDialog
import com.angcyo.uiview.less.utils.RUtils
import com.angcyo.uiview.less.utils.permission.SettingsCompat

/**
 * Created by angcyo on 2018/10/20 19:40
 */
object Permission {
    var isShowAccessibilityDialog = false
    var isShowOverlaysDialog = false

    fun haveAllPermission(activity: Activity): Boolean {
        return haveAccService() && haveDrawOverlays(activity)
    }

    fun haveAccService(): Boolean {
        return BaseAccessibilityService.isServiceEnabled()
    }

    fun haveDrawOverlays(activity: Activity): Boolean {
        return SettingsCompat.canDrawOverlays(activity)
    }

    /**权限通过 返回 true*/
    fun check(activity: Activity): Boolean {
        if (!SettingsCompat.canDrawOverlays(activity)) {
            if (!isShowOverlaysDialog) {
                AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setTitle("权限提示")
                    .setMessage("请打开\"悬浮窗\"权限.")
                    .setOnDismissListener {
                        isShowOverlaysDialog = false
                    }
                    .setPositiveButton("去打开") { dialog, which ->

                        try {
                            SettingsCompat.manageDrawOverlays(activity)
                            ASTip.show("显示悬浮窗")
                        } catch (e: Exception) {
                            //Tip.tip("没有找到对应的程序.")
                            RUtils.openAppDetailView(activity)
                        }
                    }
                    .show()
                isShowOverlaysDialog = true
            }
            return false
        }

        if (!BaseAccessibilityService.isServiceEnabled()) {
            if (!isShowAccessibilityDialog) {
                AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setTitle("权限提示")
                    .setMessage("请打开\"无障碍服务\".")
                    .setOnDismissListener {
                        isShowAccessibilityDialog = false
                    }
                    .setPositiveButton("去打开") { dialog, which ->
                        BaseAccessibilityService.openAccessibilityActivity()
                        ASTip.show()
                    }
                    .show()
                isShowAccessibilityDialog = true
            }
            return false
        }

        return true
    }
}