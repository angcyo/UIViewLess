package com.angcyo.uiview.less.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2018/11/14
 */
open class GestureCallback : AccessibilityService.GestureResultCallback() {
    override fun onCancelled(gestureDescription: GestureDescription?) {
        super.onCancelled(gestureDescription)
        onEnd(gestureDescription)
    }

    override fun onCompleted(gestureDescription: GestureDescription?) {
        super.onCompleted(gestureDescription)
        onEnd(gestureDescription)
    }

    open fun onEnd(gestureDescription: GestureDescription?) {

    }
}