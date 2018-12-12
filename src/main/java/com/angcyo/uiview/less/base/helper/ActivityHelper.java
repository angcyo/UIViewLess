package com.angcyo.uiview.less.base.helper;

import android.app.Activity;
import android.arch.core.util.Function;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.kotlin.ExKt;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by angcyo on 2018/12/02 19:21
 */
public class ActivityHelper {

    public static final String KEY_EXTRA = "key_extra";

    /**
     * 设置状态栏背景颜色
     */
    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }

    /**
     * 设置状态栏背景
     */
    public static void setStatusBarDrawable(final Activity activity, final Drawable drawable) {
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int identifier = activity.getResources().getIdentifier("statusBarBackground", "id", "android");
            View statusBarView = activity.getWindow().findViewById(identifier);
            if (statusBarView != null) {
                ViewCompat.setBackground(statusBarView, drawable);
            } else {
                activity.getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        activity.getWindow().getDecorView().removeOnLayoutChangeListener(this);
                        setStatusBarDrawable(activity, drawable);
                    }
                });
            }
        }
    }

    /**
     * 是否是白色状态栏. 如果是, 那么系统的状态栏字体会是灰色
     */
    public static void lightStatusBar(Activity activity, boolean light) {
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int systemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            if (light) {
                if (ExKt.have(systemUiVisibility, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                    return;
                }
                activity.getWindow()
                        .getDecorView()
                        .setSystemUiVisibility(
                                systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                if (!ExKt.have(systemUiVisibility, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                    return;
                }
                activity.getWindow()
                        .getDecorView()
                        .setSystemUiVisibility(
                                systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    /**
     * 激活布局到状态栏中, 只要 WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS 属性, 就可以实现.
     * <p>
     * View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 属性主要用来做检查判断.
     */
    public static void enableLayoutFullScreen(Activity activity, boolean enable) {
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public static boolean isLayoutFullScreen(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            return ExKt.have(window.getDecorView().getSystemUiVisibility(), View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            return false;
        }
    }

    /**
     * 请在 {@link Activity#setContentView(View)} 之前调用
     * 低版本系统, 可能需要在 {@link Activity#onCreate(Bundle)} 之前调用
     */
    public static void setNoTitleNoActionBar(Activity activity) {
        if (activity == null) {
            return;
        }
        activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
        android.app.ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Deprecated
    public static <T extends Activity> void startActivity(@NonNull Context context, Class<T> cls, @Nullable Bundle bundle) {
        build(context)
                .setClass(cls)
                .setBundle(bundle)
                .start();
    }

    @Deprecated
    public static void startActivity(@NonNull Context context, @NonNull Intent intent) {
        build(context)
                .setIntent(intent)
                .start();
    }

    /**
     * 从fragmentManager中, 恢复Fragment. 如果没有, 则创建新对象, 请在super.onCreate()之后调用
     */
    public static List<Fragment> restore(@NonNull Context context,
                                         @NonNull FragmentManager fragmentManager,
                                         Class<? extends Fragment>... cls) {
        List<Fragment> fragments = new ArrayList<>();
        StringBuilder builder = new StringBuilder("恢复Fragment:");
        for (Class f : cls) {
            builder.append("\n");
            String tag = f.getSimpleName();
            Fragment fragmentByTag = fragmentManager.findFragmentByTag(tag);
            if (fragmentByTag == null) {
                fragmentByTag = Fragment.instantiate(context, f.getName());
                builder.append("创建:");
            } else {
                builder.append("恢复:");
            }
            fragments.add(fragmentByTag);

            builder.append(tag);
            builder.append("->");
            FragmentHelper.logFragment(fragmentByTag, builder);
        }
        L.w(builder.toString());
        return fragments;
    }

    /**
     * 获取启动的时, 设置的参数
     */
    public static Bundle getBundle(@NonNull Intent intent) {
        return intent.getBundleExtra(KEY_EXTRA);
    }

    public static Builder build(@NonNull Context context) {
        return new Builder(context);
    }

    public static class Builder {
        Context context;
        Intent intent;
        Bundle bundle;
        int enterAnim = -1;
        int exitAnim = -1;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * 用cls, 启动Activity
         */
        public Builder setClass(@NonNull Class<? extends Activity> cls) {
            intent = new Intent(context, cls);
            configIntent();
            return this;
        }

        /**
         * 用Intent, 启动Activity
         */
        public Builder setIntent(@NonNull Intent intent) {
            this.intent = intent;
            configIntent();
            return this;
        }

        /**
         * 用包名, 启动Activity
         */
        public Builder setPackageName(@NonNull String packageName) {
            intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            configIntent();
            return this;
        }

        private void configIntent() {
            if (context instanceof Activity) {

            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            if (bundle != null) {
                intent.putExtra(KEY_EXTRA, bundle);
            }
        }

        /**
         * 设置传输的参数
         */
        public Builder setBundle(Bundle bundle) {
            this.bundle = bundle;
            return this;
        }

        /**
         * 扩展设置
         */
        public Builder extra(@NonNull Function<Bundle, Void> function) {
            this.bundle = new Bundle();
            function.apply(bundle);
            return this;
        }

        public Builder enterAnim(@AnimRes int enterAnim) {
            this.enterAnim = enterAnim;
            return this;
        }

        public Builder exitAnim(@AnimRes int exitAnim) {
            this.exitAnim = exitAnim;
            return this;
        }

        /**
         * 用来启动Activity
         */
        public Intent start() {
            if (intent == null) {
                L.e("必要的参数不合法,请检查参数:" + "\n1->intent:" + intent + " ×");
            } else {
                context.startActivity(intent);

                if (context instanceof Activity) {
                    if (enterAnim != -1 || exitAnim != -1) {
                        ((Activity) context).overridePendingTransition(enterAnim, exitAnim);
                    }
                }
            }
            return intent;
        }

        /**
         * 可以在Fragment中, 关闭Activity , 或者 Remove  Fragment
         */
        public void back() {

        }
    }
}