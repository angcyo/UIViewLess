package com.angcyo.uiview.less.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.*;

/**
 * 主线程调度器
 * Created by robi on 2016-06-02 20:47.
 */
public class ThreadExecutor {
    static private ThreadExecutor instance;
    private final Executor mCallbackPoster;
    private final Executor mCallbackPosterDelay;
    private final ExecutorService mExecutorService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int delayTime = 0;

    private ThreadExecutor() {
        mCallbackPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
        mCallbackPosterDelay = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.postDelayed(command, delayTime);
            }
        };

        mExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    public static ThreadExecutor instance() {
        return instance == null ? instance = new ThreadExecutor() : instance;
    }

    public void onMain(Runnable runnable) {
        onMain(0, runnable);
    }

    public void onThread(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

    public void onMain(int delayTime, Runnable runnable) {
        this.delayTime = delayTime;
        if (delayTime > 0) {
            mCallbackPosterDelay.execute(runnable);
        } else {
            if (RUtils.isMainThread()) {
                if (runnable != null) {
                    runnable.run();
                }
            } else {
                mCallbackPoster.execute(runnable);
            }
        }
    }
}
