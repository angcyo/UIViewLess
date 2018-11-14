package com.angcyo.uiview.less.accessibility

import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.angcyo.uiview.less.kotlin.nowTime
import java.util.*

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/01/26 08:57
 * 修改人员：Robi
 * 修改时间：2018/01/26 08:57
 * 修改备注：
 * Version: 1.0.0
 */
abstract class AccessibilityInterceptor {
    /**需要收到那个程序的事件*/
    var filterPackageName = ""

    /**当到达目标之后的回调*/
    var onJumpToTarget: (() -> Unit)? = null

    val handler = Handler(Looper.getMainLooper())

    /**等待延迟的任务*/
    var delayRunnable: Runnable? = null

    val filterEventList = ArrayList<FilterEven>()

    companion object {
        var lastAccService: BaseAccessibilityService? = null
        var lastEvent: AccessibilityEvent? = null
    }

    /**过滤包名后的事件*/
    open fun onAccessibilityEvent(accService: BaseAccessibilityService, event: AccessibilityEvent) {
        lastAccService = accService
        lastEvent = AccessibilityEvent.obtain(event)

        filterEvent(accService, event)
    }

    open fun filterEvent(accService: BaseAccessibilityService, event: AccessibilityEvent) {
        var filter = false
        for (bean in filterEventList) {
            if (bean.eventType == event.eventType && bean.className == event.className) {
                val nowTime = nowTime()
                if (nowTime - bean.lastHandlerTime <= bean.delayTime) {
                    filter = true
                } else {
                    bean.lastHandlerTime = nowTime
                }
                break
            }
        }
        if (!filter) {
            onFilterAccessibilityEvent(accService, event)
        }
    }

    /**并发事件过滤后的回调*/
    open fun onFilterAccessibilityEvent(accService: BaseAccessibilityService, event: AccessibilityEvent) {

    }

    open fun onDestroy() {

    }

    /**切换到了非过滤包名的程序*/
    open fun onLeavePackageName(
        accService: BaseAccessibilityService,
        event: AccessibilityEvent,
        toPackageName: String
    ) {
        //L.i("离开 $filterPackageName -> $toPackageName")
    }

    /**每次延迟, 取消之前的任务*/
    open fun delay(delay: Long, action: () -> Unit) {
        delayRunnable?.let {
            handler.removeCallbacks(it)
        }
        delayRunnable = Runnable {
            action.invoke()
        }
        handler.postDelayed(delayRunnable, delay)
    }

    fun sleep(delay: Long, action: () -> Unit) {
        Thread.sleep(delay)
        action.invoke()
    }

    open fun isWindowStateChanged(event: AccessibilityEvent): Boolean {
        return event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
    }

    /**此事件, 会优先于 TYPE_WINDOW_STATE_CHANGED 调用*/
    open fun isWindowContentChanged(event: AccessibilityEvent): Boolean {
        return event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
    }

    fun rootNodeInfo(
        accService: BaseAccessibilityService?,
        event: AccessibilityEvent
    ): AccessibilityNodeInfo? {
        return if (accService?.rootInActiveWindow == null) {
            event.source
        } else {
            accService.rootInActiveWindow
        }
    }

    private fun idString(
        id: String,
        event: AccessibilityEvent
    ): String {
        if (event.packageName == null) {
            return id
        }

        return if (id.contains(event.packageName)) {
            id
        } else {
            "${event.packageName}:id/$id"
        }
    }


    open fun findNodeByText(
        text: String,
        accService: BaseAccessibilityService?,
        event: AccessibilityEvent
    ): List<AccessibilityNodeInfo> {
        val rootNodeInfo = rootNodeInfo(accService, event)
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        rootNodeInfo?.findAccessibilityNodeInfosByText(text)?.let {
            nodes.addAll(it)
        }
        return nodes
    }

    /**
     * id/button1
     *
     * -> button1
     * */
    open fun findNodeById(
        id: String,
        accService: BaseAccessibilityService?,
        event: AccessibilityEvent
    ): List<AccessibilityNodeInfo> {
        val rootNodeInfo = rootNodeInfo(accService, event)

        val idString = idString(id, event)
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        rootNodeInfo?.findAccessibilityNodeInfosByViewId(idString)?.let {
            nodes.addAll(it)
        }
        return nodes
    }

    /**返回 文本 node 在屏幕中的 矩形坐标*/
    open fun findRectByText(
        text: String,
        accService: BaseAccessibilityService,
        event: AccessibilityEvent
    ): Array<Rect> {
        val rootNodeInfo = rootNodeInfo(accService, event)

        val nodes = rootNodeInfo?.findAccessibilityNodeInfosByText(text)
        val rectList = mutableListOf<Rect>()

        nodes?.mapIndexed { _, accessibilityNodeInfo ->
            val rect = Rect()
            accessibilityNodeInfo.getBoundsInScreen(rect)
            rectList.add(rect)
        }
        return rectList.toTypedArray()
    }

    /**
     * id 全路径 "com.xunmeng.pinduoduo:id/ll_tab"
     * 但是 只需要传 ll_tab 就行
     * */
    open fun findRectById(
        id: String,
        accService: BaseAccessibilityService,
        event: AccessibilityEvent
    ): Array<Rect> {
        val rootNodeInfo = rootNodeInfo(accService, event)

        val idString = idString(id, event)

        val nodes = rootNodeInfo?.findAccessibilityNodeInfosByViewId(idString)
        val rectList = mutableListOf<Rect>()

        nodes?.mapIndexed { _, accessibilityNodeInfo ->
            val rect = Rect()
            accessibilityNodeInfo.getBoundsInScreen(rect)
            rectList.add(rect)
        }
        return rectList.toTypedArray()
    }

    /**返回中心点坐标*/
    open fun findPathByText(
        text: String,
        accService: BaseAccessibilityService,
        event: AccessibilityEvent
    ): Array<Path> {
        val rectList = findRectByText(text, accService, event)
        val pathList = mutableListOf<Path>()

        rectList.mapIndexed { _, rect ->
            val path = Path().apply {
                moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
            }
            pathList.add(path)
        }

        return pathList.toTypedArray()
    }

    /**返回文本在底部的path*/
    open fun findBottomRect(
        accService: BaseAccessibilityService,
        rects: Array<Rect>
    ): Rect {
        var targetRect = Rect()
        val point = accService.displaySize()
        rects.map {
            if (it.centerY() > point.y / 2) {
                if (it.centerY() > targetRect.centerY()) {
                    targetRect = it
                }
            }
        }
        return targetRect
    }

    /**从顶部查询*/
    open fun findTopRect(
        accService: BaseAccessibilityService,
        rects: Array<Rect>
    ): Rect {
        val point = accService.displaySize()
        var targetRect = Rect(0, 0, point.x, point.y)
        rects.map {
            if (it.centerY() < point.y / 2) {
                if (it.centerY() < targetRect.centerY()) {
                    targetRect = it
                }
            }
        }
        return targetRect
    }

    /**相同事件, 过滤短时间内的并发回调*/
    data class FilterEven(
        val eventType: Int,
        val className: String,
        val delayTime: Int /*毫秒*/,
        var lastHandlerTime: Long = 0L /*用来标识最后处理的时间*/
    )
}