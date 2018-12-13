package com.angcyo.uiview.less.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.ResUtil;

/**
 * Created by angcyo on 2018/12/03 23:17
 * <p>
 * 一些生命周期日志的输出,和创建跟视图
 */
public abstract class AbsFragment extends Fragment {

    //<editor-fold desc="对象属性">

    protected RBaseViewHolder baseViewHolder;
    protected Context mAttachContext;

    //</editor-fold">

    /**
     * 保存回调方法之前的状态值
     */
    protected boolean mUserVisibleHintOld = true;
    protected boolean mHiddenOld = false;

    //<editor-fold desc="生命周期, 系统的方法">

    /**
     * 此方法, 通常在 hide show fragment的时候调用
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        boolean old = mHiddenOld;
        mHiddenOld = hidden;
        L.v(this.getClass().getSimpleName() + " hiddenOld:" + old + " hidden:" + hidden + " isAdded:" + isAdded());
        onVisibleChanged(old, mUserVisibleHintOld, !hidden);
    }

    /**
     * 此方法, 通常在 FragmentStatePagerAdapter 中调用
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        boolean old = mUserVisibleHintOld;
        mUserVisibleHintOld = isVisibleToUser;
        L.v(this.getClass().getSimpleName() + " isVisibleToUserOld:" + old + " isVisibleToUser:" + isVisibleToUser + " isAdded:" + isAdded());
        onVisibleChanged(mHiddenOld, old, isVisibleToUser);
    }

    /**
     * 可见性变化
     */
    protected void onVisibleChanged(boolean oldHidden, boolean oldUserVisibleHint, boolean visible /*是否可见*/) {
        L.d(this.getClass().getSimpleName() + " isAdded:" + isAdded()
                + " hidden:" + oldHidden + "->" + isHidden() + " visible:" + oldUserVisibleHint + "->" + getUserVisibleHint() + " ->" + visible);
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        StringBuilder builder = new StringBuilder();
        FragmentHelper.logFragment(childFragment, builder);
        L.d(this.getClass().getSimpleName() + builder);
    }

    /**
     * OnAttach -> OnCreate -> OnCreateView -> OnActivityCreated -> OnViewStateRestored -> OnStart -> OnResume
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAttachContext = context;
        L.d(this.getClass().getSimpleName() + "\n" + context + " id:" + getId() + " tag:" + getTag() + "\nParent:" + getParentFragment());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.d(this.getClass().getSimpleName() + " " + savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        L.d(this.getClass().getSimpleName() + "\n" + container + " state:" + (savedInstanceState == null ? "×" : "√"));

        int layoutId = getLayoutId();
        View rootView;
        if (layoutId != -1) {
            rootView = inflater.inflate(layoutId, container, false);
        } else {
            rootView = createRootView();
        }
        baseViewHolder = new RBaseViewHolder(rootView);

        initBaseView(baseViewHolder, getArguments(), savedInstanceState);

        return rootView;
    }

    /**
     * 状态恢复, 回调顺序 最优先
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        L.d(this.getClass().getSimpleName() + " state:" + (savedInstanceState == null ? "×" : "√"));
    }

    @Override
    public void onStart() {
        super.onStart();
        L.d(this.getClass().getSimpleName());
    }

    @Override
    public void onResume() {
        super.onResume();
        L.d(this.getClass().getSimpleName());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        L.d(this.getClass().getSimpleName() + " " + outState);
    }

    /**
     * View需要恢复状态
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        L.d(this.getClass().getSimpleName() + " state:" + (savedInstanceState == null ? "×" : "√"));
    }

    /**
     * OnPause -> OnStop -> OnDestroyView -> OnDestroy -> OnDetach
     */
    @Override
    public void onPause() {
        super.onPause();
        L.d(this.getClass().getSimpleName());
    }

    @Override
    public void onStop() {
        super.onStop();
        L.d(this.getClass().getSimpleName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        L.d(this.getClass().getSimpleName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.d(this.getClass().getSimpleName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        L.d(this.getClass().getSimpleName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //</editor-fold>

    //<editor-fold desc="自定义, 可以重写 的方法">

    /**
     * 不指定布局Id的时候, 可以用代码创建跟视图
     */
    @NonNull
    protected View createRootView() {
        View view = new View(getContext());
        view.setBackgroundColor(ResUtil.getColor(R.color.base_dark_red_tran));
        return view;
    }

    @LayoutRes
    protected int getLayoutId() {
        return -1;
    }

    protected void initBaseView(@NonNull RBaseViewHolder viewHolder,
                                @Nullable Bundle arguments,
                                @Nullable Bundle savedInstanceState) {

    }
    //</editor-fold>
}
