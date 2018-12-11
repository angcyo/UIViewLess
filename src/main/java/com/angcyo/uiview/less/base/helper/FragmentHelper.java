package com.angcyo.uiview.less.base.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.BuildConfig;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.base.IFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/05
 */
public class FragmentHelper {
    public static final String TAG = "FragmentHelper";

    @Deprecated
    public static Fragment showFragment(@NonNull FragmentManager fragmentManager,
                                        @NonNull Fragment fragment, int parentLayout) {
        return showFragment(fragmentManager, fragment, parentLayout, false);
    }

    @Deprecated
    public static Fragment showFragment(@NonNull FragmentManager fragmentManager,
                                        @NonNull Fragment fragment, int parentLayout, boolean stateLoss) {
        return showFragment(fragmentManager, fragment, null, parentLayout, stateLoss);
    }

    @Deprecated
    public static Fragment showFragment(@NonNull final FragmentManager fragmentManager,
                                        @NonNull Fragment fragment, @Nullable Fragment hideFragment,
                                        int parentLayout, boolean stateLoss) {
        return build(fragmentManager)
                .showFragment(fragment)
                .hideFragment(hideFragment)
                .parentLayoutId(parentLayout)
                .allowStateLoss(stateLoss)
                .doIt();
    }

    public static Builder build(@NonNull FragmentManager fragmentManager) {
        return new Builder(fragmentManager);
    }

    public static void logFragments(@NonNull FragmentManager fragmentManager) {
        StringBuilder builder = new StringBuilder();
        List<Fragment> fragments = fragmentManager.getFragments();
        Fragment primaryNavigationFragment = fragmentManager.getPrimaryNavigationFragment();

        builder.append("\n");
        if (primaryNavigationFragment != null) {
            builder.append("PrimaryFragment->");
            logFragment(primaryNavigationFragment, builder);
            builder.append("\n");
        }

        for (int i = 0; i < fragments.size(); i++) {
            Fragment f = fragments.get(i);
            builder.append(i);
            builder.append("->");
            logFragment(f, builder);

            Fragment parentFragment = f.getParentFragment();
            if (parentFragment != null) {
                builder.append("\n   parent:");
                logFragment(parentFragment, builder);
            }
            builder.append("\n");
        }

        L.w(TAG, builder.toString());
    }

    public static void logFragment(@Nullable Fragment fragment, StringBuilder builder) {
        if (fragment != null) {
            builder.append(Integer.toHexString(getFragmentContainerId(fragment)).toUpperCase());
            builder.append(" ");
            builder.append(fragment);

            builder.append(" isAdd:");
            builder.append(fragment.isAdded() ? "√" : "×");
            builder.append(" isHidden:");
            builder.append(fragment.isHidden() ? "√" : "×");
            builder.append(" userVisible:");
            builder.append(fragment.getUserVisibleHint() ? "√" : "×");

            View view = fragment.getView();
            if (view != null) {
                builder.append(" view:");
                builder.append(view);
                builder.append(" visible:");
                int visibility = view.getVisibility();
                String string;
                switch (visibility) {
                    case View.INVISIBLE:
                        string = "INVISIBLE";
                        break;
                    case View.GONE:
                        string = "GONE";
                        break;
                    default:
                        string = "VISIBLE";
                        break;
                }
                builder.append(string);
            } else {
                builder.append(" view:×");
            }

            if (fragment instanceof IFragment) {
                builder.append(" 可见:");
                builder.append(!((IFragment) fragment).isFragmentHide());
            }
        }
    }

    /**
     * 通过反射, 获取Fragment所在视图的Id
     */
    public static int getFragmentContainerId(@NonNull Fragment fragment) {
        int viewId = -1;

        View fragmentView = fragment.getView();
        if (fragmentView == null) {

        } else if (fragmentView.getParent() instanceof View) {
            viewId = ((View) fragmentView.getParent()).getId();
        }

        if (viewId == View.NO_ID) {
            try {
                Field field = Fragment.class.getDeclaredField("mContainerId");
                field.setAccessible(true);
                viewId = (int) field.get(fragment);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return viewId;
    }

    /**
     * 拿到Fragment所在的ViewGroup
     */
    public static ViewGroup getFragmentViewGroup(@NonNull FragmentManager fragmentManager,
                                                 @IdRes int fragmentContainerId) {
        List<Fragment> fragments = fragmentManager.getFragments();

        ViewGroup targetViewGroup = null;
        viewGroup:
        for (Fragment f : fragments) {
            if (getFragmentContainerId(f) ==
                    fragmentContainerId) {

                View view = f.getView();
                if (view != null && view.getParent() instanceof ViewGroup) {
                    targetViewGroup = (ViewGroup) view.getParent();
                    break viewGroup;
                }
            }
        }
        return targetViewGroup;
    }

    /**
     * 从指定的ViewGroup中, 获取排好序的Fragment
     */
    public static List<Fragment> getFragmentList(@NonNull FragmentManager fragmentManager,
                                                 @IdRes int fragmentContainerId) {
        List<Fragment> fragments = fragmentManager.getFragments();
        List<Fragment> fragmentsResult = new ArrayList<>();
        ViewGroup targetViewGroup = getFragmentViewGroup(fragmentManager, fragmentContainerId);

        if (targetViewGroup != null) {
            for (int i = 0; i < targetViewGroup.getChildCount(); i++) {
                for (Fragment f : fragments) {
                    if (f.getView() == targetViewGroup.getChildAt(i)) {
                        fragmentsResult.add(f);
                    }
                }
            }
        }

        return fragmentsResult;
    }

    /**
     * 获取顶层视图, 对应的Fragment
     *
     * @param lastIndex 倒数第几个视图, 从0开始
     */
    public static Fragment getLastFragment(@NonNull FragmentManager fragmentManager,
                                           @IdRes int fragmentContainerId,
                                           int lastIndex /*倒数第几个, 从0开始*/) {
        List<Fragment> fragments = fragmentManager.getFragments();

        //拿到目标Fragment需要添加到的ViewGroup
        ViewGroup targetViewGroup = getFragmentViewGroup(fragmentManager, fragmentContainerId);

        //拿到当前最顶层显示的Fragment
        Fragment lastFragment = null;
        if (targetViewGroup != null) {
            lastFragment:
            for (int i = targetViewGroup.getChildCount() - 1 - lastIndex; i >= 0; i--) {
                View childAt = targetViewGroup.getChildAt(i);

                for (int j = fragments.size() - 1; j >= 0; j--) {
                    Fragment f = fragments.get(j);
                    if (f.getView() == childAt) {
                        lastFragment = f;
                        break lastFragment;
                    }
                }

                break lastFragment;
            }
        }
        return lastFragment;
    }

    public static List<Fragment> getBeforeFragment(@NonNull FragmentManager fragmentManager,
                                                   Fragment excludeFragment,
                                                   @IdRes int fragmentContainerId,
                                                   int beforeIndex /*从excludeFragment的前面第几个开始*/) {
        List<Fragment> fragments = getFragmentList(fragmentManager, fragmentContainerId);
        List<Fragment> beforeFragments = new ArrayList<>();
        List<Fragment> result = new ArrayList<>();

        for (Fragment f : fragments) {
            if (f == excludeFragment
                /*f.getView() == resultFragment.getView()*/) {
                continue;
            }
            if (getFragmentContainerId(f) ==
                    fragmentContainerId) {
                beforeFragments.add(f);
            }
        }
        beforeFragments.add(excludeFragment);

        for (int i = 0; i < beforeFragments.size() - beforeIndex; i++) {
            result.add(beforeFragments.get(i));
        }
        return result;
    }

    public static class Builder {
        FragmentManager fragmentManager;
        /**
         * 需要隐藏的Fragment
         */
        Fragment hideFragment;

        /**
         * 需要显示的Fragment, 如果没有add, 会替换成add操作
         */
        Fragment showFragment;

        /**
         * showFragment 所在的Parent
         */
        Fragment parentFragment;

        /**
         * add...
         * 0->Fragment1
         * 1->Fragment2
         * 2->Fragment3
         * 3->Fragment4
         * <p>
         * 此时
         * add Fragment5 时
         * <p>
         * 如果 hideBeforeIndex =1
         * 那么 Fragment4 Fragment3 Fragment2 Fragment1 都会执行hide方法
         * <p>
         * 如果 hideBeforeIndex =2
         * 那么 Fragment3 Fragment2 Fragment1 都会执行hide方法
         * <p>
         * 这个值需要 >=1 才会生效
         */
        int hideBeforeIndex = -1;

        /**
         * commit()  or  commitAllowingStateLoss()
         */
        boolean allowStateLoss = false;

        boolean commitNow = false;

        /**
         * 父视图在xml中声明的Id
         */
        @IdRes
        int parentLayoutId = -1;

        String tag = null;

        /**
         * 是否优先使用已经保存过的Fragment, 比如恢复模式下, 就需要设置为true
         */
        boolean isFromCreate = true;

        /**
         * Fragment的参数
         */
        Bundle args;

        int enterAnim = -1;
        int exitAnim = -1;

        public Builder(@NonNull FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
        }

        public Builder hideFragment(Fragment hideFragment) {
            this.hideFragment = hideFragment;
            return this;
        }

        public Builder showFragment(Fragment showFragment) {
            this.showFragment = showFragment;
            return this;
        }

        public Builder showFragment(Context context, Class<? extends Fragment> showFragment) {
            this.showFragment = Fragment.instantiate(context, showFragment.getName());
            //关闭从恢复模式获取Fragment
            isFromCreate = false;
            return this;
        }

        public Builder parentFragment(Fragment parentFragment) {
            this.parentFragment = parentFragment;
            return this;
        }

        public Builder hideBeforeIndex(@IntRange(from = 1, to = Integer.MAX_VALUE) int index) {
            this.hideBeforeIndex = index;
            return this;
        }

        public Builder allowStateLoss(boolean allowStateLoss) {
            this.allowStateLoss = allowStateLoss;
            return this;
        }

        public Builder parentLayoutId(int parentLayoutId) {
            this.parentLayoutId = parentLayoutId;
            return this;
        }

        /**
         * 自动从Fragment中获取parentLayoutId
         */
        public Builder parentLayoutId(@NonNull Fragment fragment) {
            return parentLayoutId(getFragmentContainerId(fragment));
        }

        public Builder commitNow(boolean commitNow) {
            this.commitNow = commitNow;
            return this;
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setFromCreate(boolean fromCreate) {
            isFromCreate = fromCreate;
            return this;
        }

        public Builder setArgs(Bundle args) {
            this.args = args;
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

        public Builder defaultExitAnim() {
            this.enterAnim = R.anim.base_alpha_exit;
            this.exitAnim = R.anim.base_tran_to_bottom;
            return this;
        }

        public Builder defaultEnterAnim() {
            this.exitAnim = R.anim.base_alpha_exit;
            this.enterAnim = R.anim.base_tran_to_top;
            return this;
        }

        private void parent(Fragment lastFragment) {
            if (lastFragment == null || parentFragment == null) {
                return;
            }
            if (parentFragment instanceof IFragment &&
                    lastFragment instanceof IFragment) {
                //父参数, 传递给 子Fragment
                ((IFragment) lastFragment).setFragmentInViewPager(((IFragment) parentFragment).isFragmentInViewPager());
                ((IFragment) parentFragment).setLastFragment((IFragment) lastFragment);
                if (!parentFragment.getUserVisibleHint()) {
                    lastFragment.setUserVisibleHint(false);
                }
            }
        }

        private void animation(FragmentTransaction fragmentTransaction) {
            if (enterAnim != -1 || exitAnim != -1) {
                fragmentTransaction.setCustomAnimations(enterAnim, exitAnim,
                        enterAnim, exitAnim);
            }
        }

        private FragmentTransaction fragmentTransaction;

        private void configTransaction() {
            if (fragmentTransaction == null) {
                fragmentTransaction = fragmentManager.beginTransaction();
                //动画设置
                animation(fragmentTransaction);
            }
        }

        /**
         * 用来在Activity里面按下返回键
         *
         * @return true 可以关闭Activity, false 不可以关闭Activity
         */
        public boolean back(@Nullable Activity activity) {
            if (fragmentManager == null ||
                    parentLayoutId == -1 ||
                    activity == null) {
                L.e("必要的参数不合法,请检查参数:"
                        + "\n1->fragmentManager:" + fragmentManager + (fragmentManager == null ? " ×" : " √")
                        + "\n2->parentLayoutId:" + parentLayoutId + (parentLayoutId == -1 ? " ×" : " √")
                        + "\n3->activity:" + activity + (activity == null ? " ×" : " √")
                );
                return false;
            }

            List<Fragment> fragments = getFragmentList(fragmentManager, parentLayoutId);
            int size = fragments.size();

            boolean canBack = false;
            boolean needCommit = false;

            if (size <= 0) {
                //当前parentLayoutId中,没有Fragment
                canBack = true;
            } else if (size == 1) {
                Fragment fragment = fragments.get(0);
                if (fragment instanceof IFragment) {
                    canBack = ((IFragment) fragment).onBackPressed(activity);
                } else {
                    canBack = true;
                }
            } else {
                Fragment lastFragment = fragments.get(size - 1);
                if (lastFragment instanceof IFragment) {
                    canBack = ((IFragment) lastFragment).onBackPressed(activity);

                    if (canBack) {
                        needCommit = true;
                        canBack = false;

                        configTransaction();

                        //移除最顶上的Fragment
                        fragmentTransaction.remove(lastFragment);

                        Fragment preFragment = fragments.get(size - 2);

                        View view = preFragment.getView();
                        if (view != null) {
                            if (view.getVisibility() == View.GONE) {
                                configTransaction();

                                //显示次顶上的Fragment
                                fragmentTransaction.show(preFragment);
                            } else {
                                if (preFragment instanceof IFragment) {
                                    preFragment.setUserVisibleHint(true);
                                } else {
                                    //不支持
                                }
                            }
                        }
                    }
                } else {
                    canBack = true;
                }
            }

            //日志输出
            logInner(fragmentTransaction, needCommit);

            //提交事务
            if (needCommit) {
                commitInner(fragmentTransaction);
            }

            return canBack;
        }

        /**
         * 用来显示Fragment
         * <p>
         * 如果已经Add, 那么就是 showFragment
         * 否则就是 addFragment
         */
        @Nullable
        public Fragment doIt() {
            if (fragmentManager == null ||
                    showFragment == null ||
                    parentLayoutId == -1) {
                L.e("必要的参数不合法,请检查参数:"
                        + "\n1->fragmentManager:" + fragmentManager + (fragmentManager == null ? " ×" : " √")
                        + "\n2->showFragment:" + showFragment + (showFragment == null ? " ×" : " √")
                        + "\n3->parentLayoutId:" + parentLayoutId + (parentLayoutId == -1 ? " ×" : " √"));
                return showFragment;
            }

            Fragment resultFragment = showFragment;
            String fragmentTag;
            if (tag == null) {
                fragmentTag = showFragment.getClass().getSimpleName();
            } else {
                fragmentTag = tag;
            }

            //如果是恢复模式, 可以拿到系统恢复的对象
            Fragment fragmentByTag;
            if (isFromCreate) {
                fragmentByTag = fragmentManager.findFragmentByTag(fragmentTag);
            } else {
                fragmentByTag = showFragment;
            }

            if (fragmentByTag != null) {
                resultFragment = fragmentByTag;
            }

            if (args != null) {
                resultFragment.setArguments(args);
            }

            boolean needCommit = true;
            boolean isFragmentAdded = resultFragment.isAdded();
            boolean isFragmentHide = false;
            int fragmentViewVisibility = View.VISIBLE;

            if (resultFragment instanceof IFragment) {
                isFragmentHide = ((IFragment) resultFragment).isFragmentHide();
            } else {
                isFragmentHide = resultFragment.isHidden() || !resultFragment.getUserVisibleHint();
            }

            //需要显示的Fragment所在的view的id
            int fragmentContainerId = parentLayoutId;

            //显示或者添加Fragment
            if (isFragmentAdded) {
                fragmentContainerId = getFragmentContainerId(resultFragment);

                View fragmentView = resultFragment.getView();
                if (fragmentView != null) {
                    fragmentViewVisibility = fragmentView.getVisibility();
                }

                //已经存在
                if (isFragmentHide && fragmentViewVisibility == View.GONE) {
                    configTransaction();
                    fragmentTransaction.show(resultFragment);
                } else {
                    needCommit = false;
                    try {
                        resultFragment.setUserVisibleHint(true);

                        if (fragmentView != null) {
                            if (fragmentViewVisibility != View.VISIBLE) {
                                fragmentView.setVisibility(View.VISIBLE);
                            }
                            fragmentView.bringToFront();
                        } else {
                            L.e("警告:" + resultFragment + " 没有视图.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //不存在
                configTransaction();
                fragmentTransaction.add(parentLayoutId, resultFragment, fragmentTag);
            }

            //设置lastFragment
            parent(resultFragment);

            //隐藏需要隐藏的Fragment
            if (hideFragment != null) {
                configTransaction();
                fragmentTransaction.hide(hideFragment);
                needCommit = true;
            }

            //隐藏之前的Fragment
            if (hideBeforeIndex > 0) {
                /*Fragment的顺序和add的顺序保持一致, 无法修改*/
                List<Fragment> beforeFragments = getBeforeFragment(fragmentManager, resultFragment, fragmentContainerId, hideBeforeIndex);

                for (int i = 0; i < beforeFragments.size(); i++) {
                    configTransaction();
                    fragmentTransaction.hide(beforeFragments.get(i));
                    needCommit = true;
                }
            }

            //不 hide 的Fragment, 也需要执行不可见的生命周期
            if (hideBeforeIndex > 1) {
                //如果不隐藏之前的Fragment, 那么onHiddenChanged不会触发.
                //此时界面对用户不可见,需要手动调用setUserVisibleHint方法
                Fragment lastFragment = getLastFragment(fragmentManager, fragmentContainerId, isFragmentAdded ? 1 : 0);

                if (lastFragment != null) {
                    lastFragment.setUserVisibleHint(false);
                }
            }

            //日志输出
            logInner(fragmentTransaction, needCommit);

            //提交事务
            if (needCommit) {
                commitInner(fragmentTransaction);
            }
            return resultFragment;
        }

        private void logInner(FragmentTransaction transaction, boolean needCommit) {
            if (BuildConfig.DEBUG) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        logFragments(fragmentManager);
                    }
                };
                if (needCommit) {
                    transaction.runOnCommit(runnable);
                } else {
                    runnable.run();
                }
            }
        }

        /**
         * 提交事务
         */
        private void commitInner(FragmentTransaction transaction) {
            if (commitNow) {
                if (allowStateLoss) {
                    transaction.commitNowAllowingStateLoss();
                } else {
                    transaction.commitNow();
                }
            } else {
                if (allowStateLoss) {
                    transaction.commitAllowingStateLoss();
                } else {
                    transaction.commit();
                }
            }
        }

    }
}
