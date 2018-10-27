package com.angcyo.uiview.less.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.WindowManager
import com.angcyo.lib.L

/**
 * Created by angcyo on 2018/10/24 21:06
 */

/**
 * 相当于按返回键
 * */
public fun AccessibilityService.back() {
    //api 16
    this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
}

public fun AccessibilityService.home() {
    //api 16
    this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
}

public fun AccessibilityService.recents() {
    //api 16
    this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
}

/**锁屏*/
public fun AccessibilityService.lockScreen() {
    //api 28
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
    }
}

/**屏幕截图*/
public fun AccessibilityService.takeScreenShot() {
    //api 28
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
    }
}

public fun AccessibilityService.move(path: Path, callback: AccessibilityService.GestureResultCallback? = null) {
    val pathsList = mutableListOf<Path>()
    pathsList.add(path)

    val paths = pathsList.toTypedArray()
    val startTImeList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()

    val DEFAULT_START_TIME = 20L
    val DEFAULT_DURATION = 500L
    paths.mapIndexed { index, _ ->
        startTImeList.add((index + 1) * DEFAULT_START_TIME + index * DEFAULT_DURATION)
        durationList.add(DEFAULT_DURATION)
    }

    touch(paths, startTImeList.toLongArray(), durationList.toLongArray(), callback)
}

/**
 * 通过 touch 坐标, 触发点击事件
 * */
public fun AccessibilityService.touch(vararg path: Path) {
    val pathsList = mutableListOf<Path>()
    pathsList.addAll(path)

    val paths = pathsList.toTypedArray()
    val startTImeList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()

    val DEFAULT_START_TIME = 20L
    val DEFAULT_DURATION = 60L
    paths.mapIndexed { index, _ ->
        startTImeList.add((index + 1) * DEFAULT_START_TIME + index * DEFAULT_DURATION)
        durationList.add(DEFAULT_DURATION)
    }

    touch(paths, startTImeList.toLongArray(), durationList.toLongArray())
}

public fun AccessibilityService.touch(callback: AccessibilityService.GestureResultCallback, vararg path: Path) {
    val pathsList = mutableListOf<Path>()
    pathsList.addAll(path)

    val paths = pathsList.toTypedArray()
    val startTImeList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()

    val DEFAULT_START_TIME = 700L
    val DEFAULT_DURATION = 300L
    paths.mapIndexed { index, _ ->
        startTImeList.add((index + 1) * DEFAULT_START_TIME + index * DEFAULT_DURATION)
        durationList.add(DEFAULT_DURATION)
    }

    touch(paths, startTImeList.toLongArray(), durationList.toLongArray(), callback)
}

/**
 * @param paths 需要点击的位置
 * @param touchInterval 每次点击 间隔时长
 * @param touchDuration 每次点击持续时长
 *
 * */
public fun AccessibilityService.touch(paths: Array<Path>, touchInterval: Long, touchDuration: Long) {
    if (paths.isEmpty()) {
        return
    }

    val intervalList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()
    paths.mapIndexed { index, path ->
        intervalList.add(touchInterval * (index + 1) + touchDuration * index)
        durationList.add(touchDuration)
    }

    touch(paths, intervalList.toLongArray(), durationList.toLongArray())
}

/**
 * 执行手势
 * */
public fun AccessibilityService.touch(
    paths: Array<Path>,
    startTime: LongArray,
    duration: LongArray,
    callback: AccessibilityService.GestureResultCallback? = null
) {
    if (paths.isEmpty()) {
        return
    }

    //api 24
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        GestureDescription.Builder().apply {
            paths.mapIndexed { index, path ->
                this.addStroke(
                    GestureDescription.StrokeDescription(
                        path,
                        startTime[index],
                        duration[index]
                    )
                )
            }
            this@touch.dispatchGesture(
                this.build(),
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        L.e("$gestureDescription")
                        callback?.onCompleted(gestureDescription)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        L.e("$gestureDescription")
                        callback?.onCancelled(gestureDescription)
                    }
                },
                null
            )
        }
    }
}

/**不包含导航栏的高度*/
public fun Context.displaySize(): Point {
    val wm: WindowManager = this.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    wm.defaultDisplay.getSize(point)
    return point
}

/**包含导航栏的高度*/
public fun Context.displayRealSize(): Point {
    val wm: WindowManager = this.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    wm.defaultDisplay.getRealSize(point)
    return point
}

public fun Context.displayRealRect(): Rect {
    val wm: WindowManager = this.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val rect = Rect()
    wm.defaultDisplay.getRectSize(rect)
    return rect
}

public fun Rect.toPath(): Path {
    return Path().apply {
        moveTo(this@toPath.centerX().toFloat(), this@toPath.centerY().toFloat())
    }
}