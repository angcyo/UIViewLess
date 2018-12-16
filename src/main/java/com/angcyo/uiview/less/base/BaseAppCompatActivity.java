package com.angcyo.uiview.less.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.base.helper.ActivityHelper;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.picture.RPicture;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/13
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    protected RBaseViewHolder viewHolder;

    protected RxPermissions mRxPermissions;

    //<editor-fold desc="生命周期, 系统的方法">

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (enableLayoutFull()) {
            ActivityHelper.enableLayoutFullScreen(this, true);
            ActivityHelper.setNoTitleNoActionBar(this);
        }

        super.onCreate(savedInstanceState);
        viewHolder = new RBaseViewHolder(getWindow().getDecorView());

        //系统Fragment操作日志输出
        //FragmentManager.enableDebugLogging(BuildConfig.DEBUG);

        L.v("taskId:" + getTaskId());
        //ActivityHelper.setStatusBarColor(this, Color.YELLOW);
        //ActivityHelper.setStatusBarDrawable(this, getDrawableCompat(R.drawable.base_nav_shadow));
    }

    @Override
    public boolean isImmersive() {
        return enableLayoutFull();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Debug.addDebugTextView(this);
        }
    }

    @Override
    public void onBackPressed() {
        int fragmentParentLayoutId = getFragmentParentLayoutId();

        if (fragmentParentLayoutId != -1) {
            if (FragmentHelper.build(getSupportFragmentManager())
                    .parentLayoutId(fragmentParentLayoutId)
                    .defaultExitAnim()
                    .back(this)) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Fragment所在的ViewGroup id
     */
    @IdRes
    protected int getFragmentParentLayoutId() {
        return -1;
    }

    //</editor-fold>

    public void moveTaskToBack() {
        moveTaskToBack(true);
    }


    /**
     * 激活沉浸式, 5.0以下不支持.
     */
    protected boolean enableLayoutFull() {
        return true;
    }

    /**
     * 请求忽略电池优化, 在后台存活机会会加大
     */
    public void ignoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    //some device doesn't has activity to handle this intent
                    //so add try catch
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
        }
    }

    //<editor-fold desc="权限相关方法">

    protected void checkPermissions() {
        checkPermissionsResult(needPermissions(), new Action1<String>() {
            @Override
            public void call(String string) {
//                onPermissionDenied(string);
                if (string.contains("0")) {
                    //有权限被拒绝
                    onPermissionDenied(string);
                } else {
                    //所有权限通过
                    onLoadViewAfterPermission(getIntent());
                }
            }
        });
    }

    public void checkPermissionsResult(String[] permissions, final Action1<String> onResult) {
        if (mRxPermissions == null) {
            mRxPermissions = new RxPermissions(this);
        }
        mRxPermissions.requestEach(permissions)
                .map(new Func1<Permission, String>() {
                    @Override
                    public String call(Permission permission) {
                        if (permission.granted) {
                            return permission.name + "1";
                        }
                        return permission.name + "0";
                    }
                })
                .scan(new Func2<String, String, String>() {
                    @Override
                    public String call(String s, String s2) {
                        return s + ":" + s2;
                    }
                })
                .last()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        L.e("\n" + BaseAppCompatActivity.this.getClass().getSimpleName() + " 权限状态-->\n"
                                + s.replaceAll("1", " 允许").replaceAll("0", " 拒绝").replaceAll(":", "\n"));
                        onResult.call(s);
                    }
                });
//                .subscribe(new Action1<Permission>() {
//                    @Override
//                    public void call(Permission permission) {
//                        if (permission.granted) {
//                            T.show(UILayoutActivity.this, "权限允许");
//                        } else {
//                            notifyAppDetailView();
//                            T.show(UILayoutActivity.this, "权限被拒绝");
//                        }
//                    }
//                });
    }

    public void checkPermissions(String[] permissions, final Action1<Boolean> onResult) {
        if (this.isDestroyed()) {
            return;
        }

        checkPermissionsResult(permissions, new Action1<String>() {
            @Override
            public void call(String s) {
                if (s.contains("0")) {
                    /*有权限被拒绝*/
                    onResult.call(false);
                } else {
                    /*所欲权限通过*/
                    onResult.call(true);
                }
            }
        });
    }

    /**
     * 权限通过后回调
     */
    protected void onLoadViewAfterPermission(Intent intent) {

    }

    /**
     * 权限拒绝后回调
     */
    protected void onPermissionDenied(String permission) {
//        startIView(new PermissionDeniedUIView(
//                        permission.replaceAll("1", "").replaceAll("0", "")),
//                false);
////        finishSelf();
//        //notifyAppDetailView();
////        T_.show("必要的权限被拒绝!");
    }

    protected String[] needPermissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.ACCESS_WIFI_STATE
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.CAMERA,
//                Manifest.permission.READ_CONTACTS
        };
    }

    //</editor-fold>

    public Drawable getDrawableCompat(@DrawableRes int res) {
        return ContextCompat.getDrawable(this, res);
    }

    public Fragment showFragment(@NonNull Fragment fragment, int parentLayout) {
        return showFragment(fragment, parentLayout, false);
    }

    public Fragment showFragment(@NonNull Fragment fragment, int parentLayout, boolean stateLoss) {
        return showFragment(fragment, null, parentLayout, stateLoss);
    }

    public Fragment showFragment(@NonNull Fragment fragment, @Nullable Fragment hideFragment, int parentLayout, boolean stateLoss) {
        return FragmentHelper.showFragment(getSupportFragmentManager(), fragment, hideFragment, parentLayout, stateLoss);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment lastFragment = FragmentHelper.getLastFragment(getSupportFragmentManager(), getFragmentParentLayoutId(), 0);
        if (lastFragment != null) {
            lastFragment.onActivityResult(requestCode, resultCode, data);
        }
        RPicture.onActivityResult(requestCode, resultCode, data);
    }
}
