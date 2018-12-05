package com.angcyo.uiview.less.base;

import android.content.Context;
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
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.ResUtil;

/**
 * Created by angcyo on 2018/12/03 23:17
 */
public class AbsFragment extends Fragment {
    protected RBaseViewHolder baseViewHolder;
    protected Context mAttachContext;

    //<editor-fold desc="生命周期, 系统的方法">

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        L.v(this.getClass().getSimpleName() + " hidden:" + hidden);
        onVisibleChanged(!hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        L.v(this.getClass().getSimpleName() + " isVisibleToUser:" + isVisibleToUser);
        onVisibleChanged(isVisibleToUser);
    }

    /**
     * 可见性变化
     */
    protected void onVisibleChanged(boolean visible /*是否可见*/) {
        L.d(this.getClass().getSimpleName() + " hidden:" + isHidden() + " visible:" + getUserVisibleHint() + " ->" + visible);
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

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
        L.d(this.getClass().getSimpleName() + "\n" + container + " " + savedInstanceState);

        int layoutId = getLayoutId();
        View rootView;
        if (layoutId != -1) {
            rootView = inflater.inflate(layoutId, container, false);
        } else {
            rootView = new View(getContext());
            rootView.setBackgroundColor(ResUtil.getColor(R.color.base_dark_red_tran));
        }
        baseViewHolder = new RBaseViewHolder(rootView);

        initBaseView(getArguments(), savedInstanceState);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        L.d(this.getClass().getSimpleName() + " " + savedInstanceState);
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

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        L.d(this.getClass().getSimpleName() + " " + savedInstanceState);
    }

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
    //</editor-fold>

    //<editor-fold desc="自定义, 可以重写 的方法">

    @LayoutRes
    protected int getLayoutId() {
        return -1;
    }

    protected void initBaseView(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {

    }
    //</editor-fold>
}
