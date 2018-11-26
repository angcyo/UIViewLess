package com.angcyo.uiview.less.manager;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.math.MathUtils;
import android.util.Log;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.accessibility.ExKt;
import com.angcyo.uiview.less.utils.Reflect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Screenshot.java
 * Description :
 * <p>
 * Created by MixtureDD on 2017/6/21 19:16.
 * Copyright © 2017 MixtureDD. All rights reserved.
 */

public class Screenshot {
    public static final String TAG = Screenshot.class.getName();
    private String nameImage = null;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;
    public static MediaProjectionManager mMediaProjectionManager = null;

    private ImageReader mImageReader;

    private int windowWidth;
    private int windowHeight;
    private int mScreenDensity;
    private Context application;
    private OnCaptureListener captureListener;

    /**
     * 保存到文件
     */
    private boolean saveToFile = false;

    /**
     * 连续捕捉
     */
    private boolean alwaysCapture = false;

    /**
     * 自动捕捉,如果为false,
     */
    private boolean autoCapture = true;

    private long captureDelay = 60;

    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int compressQuality = 80;

    private final Handler handler = new Handler();

    /**
     * 1: 创建对象, 设置回调监听
     */
    public static Screenshot capture(@NonNull Context context, @NonNull OnCaptureListener listener) {
        return new Screenshot(context.getApplicationContext()).setCaptureListener(listener);
    }

    public Screenshot setSaveToFile(boolean save) {
        saveToFile = save;
        return this;
    }

    /**
     * 是否循环截图
     */
    public Screenshot setAlwaysCapture(boolean alwaysCapture) {
        this.alwaysCapture = alwaysCapture;
        return this;
    }

    public Screenshot setCaptureListener(OnCaptureListener captureListener) {
        this.captureListener = captureListener;
        return this;
    }

    public Screenshot setCaptureDelay(long captureDelay) {
        this.captureDelay = captureDelay;
        return this;
    }

    public Screenshot setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
        return this;
    }

    public Screenshot setCompressQuality(int compressQuality) {
        this.compressQuality = MathUtils.clamp(compressQuality, 0, 100);
        return this;
    }

    /**
     * 关闭自动截图, 需要手动调用 {@link #startToShot()} 才会截图
     */
    public Screenshot setAutoCapture(boolean autoCapture) {
        this.autoCapture = autoCapture;
        return this;
    }

    private Screenshot(Context application) {
        this.application = application;
        createVirtualEnvironment();
    }

    /**
     * 手动触发截图
     */
    public void startToShot() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startCapture();

                if (autoCapture) {
                    if (alwaysCapture && mVirtualDisplay != null) {
                        handler.postDelayed(this, captureDelay);
                    } else {
                        destroy();
                    }
                } else {
                    //手动模式
                }
            }
        }, captureDelay);
    }

    /**
     * 2:触发截屏权限, 捕捉屏幕
     */
    public void startCapture(@NonNull Activity activity, int requestCode) {
        activity.startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), requestCode);
    }

    /**
     * 3:开始处理, 权限允许之后, 才能截图
     * end.
     */

    public void onActivityResult(int resultCode, Intent data) {
        onActivityResult(resultCode, data, null);
    }

    public MediaProjection onActivityResult(int resultCode, Intent data, Runnable onSucceed) {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        Log.i(TAG, "mMediaProjection defined");
        if (mMediaProjection != null) {
            mMediaProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    super.onStop();
                    Log.i(TAG, "MediaProjection Stop");
                }
            }, null);
            setUpVirtualDisplay();
            if (autoCapture) {
                startToShot();
            }

            if (onSucceed != null) {
                onSucceed.run();
            }
        }

        return mMediaProjection;
    }

    private void setUpVirtualDisplay() {
        Log.i(TAG, "Setting up a VirtualDisplay: " +
                windowWidth + "x" + windowHeight +
                " (" + mScreenDensity + ")");
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                //Log.v(TAG, "onImageAvailable");
            }
        }, null);

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                windowWidth, windowHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void createVirtualEnvironment() {
        Point realSize = ExKt.displayRealSize(application);
        windowWidth = realSize.x;
        windowHeight = realSize.y;
        mScreenDensity = (int) ExKt.density(application);
        mMediaProjectionManager = (MediaProjectionManager) application.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void startCapture() {
        if (mImageReader == null) {
            return;
        }
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        //Toast.makeText(application, "正在保存截图", Toast.LENGTH_SHORT).show();
        if (bitmap != null && saveToFile) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-hhmmss");
            String strDate = "Screenshot_" + dateFormat.format(new java.util.Date());
            String pathImage = Environment.getExternalStorageDirectory().getPath() + "/Pictures/Screenshots/";
            nameImage = pathImage + strDate + ".png";
            Log.i(TAG, "image name is : " + nameImage);

            Log.i(TAG, "bitmap  create success ");
            try {
                File fileFolder = new File(pathImage);
                if (!fileFolder.exists()) {
                    fileFolder.mkdirs();
                }
                File file = new File(nameImage);
                if (!file.exists()) {
                    Log.e(TAG, "file create success ");
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file);
                if (out != null) {
                    bitmap.compress(compressFormat, compressQuality, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(file);
                    media.setData(contentUri);
                    application.sendBroadcast(media);
                    Log.i(TAG, "screen image saved");
                    //Toast.makeText(application, "截图保存成功", Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
        if (captureListener != null && bitmap != null) {
            captureListener.onCapture(bitmap, nameImage);
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        Log.i(TAG, "onDestroy()");
        tearDownMediaProjection();
    }

    private void tearDownMediaProjection() {
        stopCapture();
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "mMediaProjection undefined");
    }

    /**
     * 停止捕捉
     */
    public void stopCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG, "virtual display stopped");
    }

    /**
     * 返回屏幕是否亮屏状态
     */
    public static boolean isScreenOn(@NonNull Context context) {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean screenOn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            screenOn = pm.isInteractive();
        } else {
            screenOn = pm.isScreenOn();
        }

        return screenOn;
    }

    /**
     * 唤醒手机屏幕并解锁, 点亮屏幕,解锁手机
     */
    public static void wakeUpAndUnlock(@NonNull Context context) {
        wakeUpAndUnlock(context, true, null);
    }

    public static void wakeUpAndUnlock(@NonNull Context context,
                                       boolean wakeLock /*亮屏(并解锁) or 灭屏*/,
                                       final Runnable succeededAction) {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean screenOn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            screenOn = pm.isInteractive();
        } else {
            screenOn = pm.isScreenOn();
        }

        if (wakeLock) {
            if (!screenOn) {
                // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
                PowerManager.WakeLock wl = pm.newWakeLock(
                        /*PowerManager.FULL_WAKE_LOCK |*/
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.SCREEN_DIM_WAKE_LOCK /*PowerManager.SCREEN_BRIGHT_WAKE_LOCK*/,
                        context.getPackageName() + ":bright");
                wl.acquire(10000); // 点亮屏幕
                wl.release(); // 释放
            } else {
                //屏幕已经是亮的
                if (succeededAction != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(succeededAction);
                }
            }
            // 屏幕解锁
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);

            if (keyguardManager.isKeyguardLocked()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context instanceof Activity) {
                    //在未设置密码的情况下, 可以解锁
                    keyguardManager.requestDismissKeyguard((Activity) context,
                            new KeyguardManager.KeyguardDismissCallback() {
                                @Override
                                public void onDismissError() {
                                    super.onDismissError();
                                    //如果设备未锁屏的情况下, 调用此方法.会回调错误
                                    L.i("onDismissError");
                                }

                                @Override
                                public void onDismissSucceeded() {
                                    super.onDismissSucceeded();
                                    //输入密码解锁成功
                                    L.i("onDismissSucceeded");
                                    if (succeededAction != null) {
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        handler.post(succeededAction);
                                    }
                                }

                                @Override
                                public void onDismissCancelled() {
                                    super.onDismissCancelled();
                                    //弹出密码输入框之后, 取消了会回调
                                    L.i("onDismissCancelled");
                                }
                            });
                } else {
                    KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
                    // 屏幕锁定
                    keyguardLock.reenableKeyguard();
                    keyguardLock.disableKeyguard(); // 解锁

                    L.i("reenableKeyguard -> disableKeyguard");

                    if (succeededAction != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(succeededAction);
                    }
                }
            }
        } else {
            if (screenOn) {
//                PowerManager.WakeLock wl = pm.newWakeLock(
//                        PowerManager.PARTIAL_WAKE_LOCK,
//                        context.getPackageName() + ":bright");
//                wl.acquire();
//                wl.release();
                //android.permission.DEVICE_POWER
                Reflect.invokeMethod(pm, "goToSleep", SystemClock.uptimeMillis(), 0, 0);
            }
        }
    }

    public interface OnCaptureListener {
        /**
         * 主线程回调
         */
        void onCapture(@NonNull Bitmap bitmap, @Nullable String filePath);
    }
}
