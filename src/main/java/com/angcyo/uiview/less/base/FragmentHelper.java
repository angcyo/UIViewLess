package com.angcyo.uiview.less.base;

import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.BuildConfig;

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

        public Builder commitNow(boolean commitNow) {
            this.commitNow = commitNow;
            return this;
        }

        private void parent(Fragment lastFragment) {
            if (lastFragment == null || parentFragment == null) {
                return;
            }
            if (parentFragment instanceof IFragment &&
                    lastFragment instanceof IFragment) {
                ((IFragment) lastFragment).setFragmentInViewPager(((IFragment) parentFragment).isFragmentInViewPager());
                ((IFragment) parentFragment).setLastFragment((IFragment) lastFragment);
                if (!parentFragment.getUserVisibleHint()) {
                    lastFragment.setUserVisibleHint(false);
                }
            }
        }

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

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment resultFragment = showFragment;
            String fragmentTag = showFragment.getClass().getSimpleName();

            //如果是恢复模式, 可以拿到系统恢复的对象
            Fragment fragmentByTag = fragmentManager.findFragmentByTag(fragmentTag);
            if (fragmentByTag != null) {
                resultFragment = fragmentByTag;
            }

            boolean needCommit = true;
            boolean isFragmentAdded = resultFragment.isAdded();
            boolean isFragmentHide = false;

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

                //已经存在
                if (isFragmentHide) {
                    transaction.show(resultFragment);
                } else {
                    needCommit = false;
                    try {
                        resultFragment.setUserVisibleHint(true);

                        View fragmentView = resultFragment.getView();
                        if (fragmentView != null) {
                            if (fragmentView.getVisibility() != View.VISIBLE) {
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
                transaction.add(parentLayoutId, resultFragment, fragmentTag);
            }

            //设置lastFragment
            parent(resultFragment);

            //隐藏需要隐藏的Fragment
            if (hideFragment != null) {
                transaction.hide(hideFragment);
                needCommit = true;
            }

            //隐藏之前的Fragment
            if (hideBeforeIndex > 0) {
                /*Fragment的顺序和add的顺序保持一致, 无法修改*/
                List<Fragment> fragments = fragmentManager.getFragments();
                List<Fragment> beforeFragments = new ArrayList<>();

                for (Fragment f : fragments) {
                    if (f == resultFragment
                        /*f.getView() == resultFragment.getView()*/) {
                        continue;
                    }
                    if (getFragmentContainerId(f) ==
                            fragmentContainerId) {
                        beforeFragments.add(f);
                    }
                }
                beforeFragments.add(resultFragment);

                for (int i = 0; i < beforeFragments.size() - hideBeforeIndex; i++) {
                    transaction.hide(beforeFragments.get(i));
                    needCommit = true;
                }
            }

            //日志输出
            if (BuildConfig.DEBUG) {
                transaction.runOnCommit(new Runnable() {
                    @Override
                    public void run() {
                        logFragments(fragmentManager);
                    }
                });
            }

            //提交事务
            if (needCommit) {
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

            return resultFragment;
        }
    }
}
