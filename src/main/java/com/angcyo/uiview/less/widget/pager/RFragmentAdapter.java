package com.angcyo.uiview.less.widget.pager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import com.angcyo.uiview.less.base.BaseFragment;
import com.angcyo.uiview.less.base.IFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by angcyo on 2018/12/05 22:15
 */
public class RFragmentAdapter extends FragmentStatePagerAdapter {

    /**
     * @see FragmentStatePagerAdapter#mFragments
     */
    private ArrayList<Fragment> mAllFragments;

    /**
     * 未添加的Fragment
     */
    private ArrayList<Fragment> mDataList = null;

    public RFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
        init();
    }

    public RFragmentAdapter(@NonNull FragmentManager fm, @NonNull List<Fragment> fragmentList) {
        super(fm);
        init();
        mDataList = new ArrayList<>();
        mDataList.addAll(fragmentList);
    }

    private void init() {
        try {
            Field field = FragmentStatePagerAdapter.class.getDeclaredField("mFragments");
            field.setAccessible(true);
            mAllFragments = (ArrayList<Fragment>) field.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            mAllFragments = new ArrayList<>();
        }
    }

    @Override
    final public Fragment getItem(int i) {
        Fragment fragment = createFragment(i);
        if (fragment instanceof IFragment) {
            ((IFragment) fragment).setFragmentInViewPager(true);
        }
        return fragment;
    }

    /**
     * 如果已经创建了Fragment, 则不会创建
     */
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (mDataList == null) {
            fragment = mAllFragments.get(position);
        } else {
            fragment = mDataList.get(position);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        if (mDataList == null) {
            return mAllFragments.size();
        }
        return mDataList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment = getItem(position);
        if (fragment instanceof BaseFragment) {

        }
        return super.getPageTitle(position);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        //L.e("test..." + object);
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        super.startUpdate(container);
        //L.e("test..." + container);
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        super.finishUpdate(container);
        //L.e("test..." + container);
    }
}
