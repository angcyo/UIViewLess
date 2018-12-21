package com.angcyo.uiview.less.base;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.base.helper.TitleItemHelper;
import com.angcyo.uiview.less.base.helper.ViewGroupHelper;
import com.angcyo.uiview.less.iview.AffectUI;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.skin.SkinHelper;
import com.angcyo.uiview.less.widget.ImageTextView;
import com.angcyo.uiview.less.widget.group.FragmentContentWrapperLayout;
import com.angcyo.uiview.less.widget.group.RSoftInputLayout;
import com.angcyo.uiview.less.widget.group.TitleBarLayout;

/**
 * Email:angcyo@126.com
 * 统一标题管理的Fragment
 *
 * @author angcyo
 * @date 2018/12/07
 */
public abstract class BaseTitleFragment extends BaseFragment implements AffectUI.OnAffectListener {

    /**
     * Fragment 内容布局, 将add到这个ViewGroup
     */
    protected FrameLayout contentWrapperLayout;

    /**
     * 标题栏和padding控制的布局
     */
    protected TitleBarLayout titleBarLayout;

    /**
     * 子类的内容布局
     */
    protected View contentView;

    /**
     * 情感图控制
     */
    protected AffectUI affectUI;

    protected FragmentContentWrapperLayout fragmentContentWrapperLayout;

    //<editor-fold desc="初始化方法">

    @NonNull
    @Override
    protected View createRootView() {
        return super.createRootView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.base_title_fragment_layout;
    }

    @Override
    protected void initBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.initBaseView(viewHolder, arguments, savedInstanceState);

        onInitBaseView(viewHolder, arguments, savedInstanceState);
    }

    protected void onInitBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        fragmentContentWrapperLayout = (FragmentContentWrapperLayout) viewHolder.itemView;

        contentWrapperLayout = baseViewHolder.v(R.id.base_content_wrapper_layout);
        titleBarLayout = baseViewHolder.v(R.id.base_title_bar_layout);

        initContentLayout(arguments);
        initBaseTitleLayout(arguments);
        initLeftControlLayout();
        initRightControlLayout();
    }

    /**
     * 初始化标题部分
     */
    protected void initBaseTitleLayout(@Nullable Bundle arguments) {
        //设置标题背景颜色
        if (titleBarLayout != null) {
            titleBarLayout.setBackgroundColor(SkinHelper.getSkin().getThemeColor());
        }

        //设置标题
        setTitleString(getFragmentTitle());
    }

    /**
     * 左边控制按钮初始化
     */
    protected void initLeftControlLayout() {
        if (getParentFragment() == null) {
            //添加返回按钮
            addLeftItem(createBackItem());
        }
    }

    /**
     * 右边控制按钮初始化
     */
    protected void initRightControlLayout() {

    }

    /**
     * 初始化内容部分
     */
    protected void initContentLayout(@Nullable Bundle arguments) {
        int contentLayoutId = getContentLayoutId();
        if (contentLayoutId == -1) {
            contentView = createContentView(contentWrapperLayout);
        } else {
            contentView = LayoutInflater.from(mAttachContext).inflate(contentLayoutId, contentWrapperLayout, false);
        }
        contentWrapperLayout.addView(contentView);

        //情感图
        affectUI = createAffectUI();
    }

    /**
     * 创建情感图控制类
     */
    protected AffectUI createAffectUI() {
        return AffectUI.build(contentWrapperLayout)
                .register(AffectUI.AFFECT_LOADING, R.layout.base_affect_loading)
                .register(AffectUI.AFFECT_ERROR, R.layout.base_affect_error)
                .register(AffectUI.AFFECT_OTHER, R.layout.base_affect_other)
                .setContentAffect(AffectUI.CONTENT_AFFECT_INVISIBLE)
                .setAffectChangeListener(this)
                .create();
    }

    /**
     * 切换情感图
     */
    protected void switchAffectUI(int affect) {
        switchAffectUI(affect, null);
    }

    protected void switchAffectUI(int affect, Object extraObj) {
        if (affectUI != null) {
            affectUI.showAffect(affect, extraObj);
        }
    }

    /**
     * 浮动标题栏, 自动设置透明背景标题栏
     */
    public void floatTitleBar() {
        if (fragmentContentWrapperLayout != null) {
            fragmentContentWrapperLayout.setContentLayoutState(FragmentContentWrapperLayout.CONTENT_BACK_OF_TITLE);
        }
        setTitleBarLayoutColor(Color.TRANSPARENT);
        hideTitleShadow();
    }

    /**
     * 隐藏默认的装饰
     */
    public void hideBaseStyle() {
        hideTitleView();
        hideTitleShadow();
        hideBackView();
    }

    /**
     * 设置标题栏的背景颜色
     */
    public void setTitleBarLayoutColor(@ColorInt int color) {
        titleControl()
                .selector(R.id.base_title_bar_layout)
                .setBackgroundColor(color);
    }

    /**
     * 设置显示的标题
     */
    public void setTitleString(@NonNull String title) {
        titleControl().selector(R.id.base_title_view).setText(title);
    }

    /**
     * 隐藏标题视图
     */
    public void hideTitleView() {
        titleControl().selector(R.id.base_title_view).gone();
    }

    /**
     * 替换标题栏
     */
    public void replaceTitleBarLayout(@LayoutRes int layoutId) {
        if (titleBarLayout != null) {
            titleBarLayout.removeAllViews();
            LayoutInflater.from(titleBarLayout.getContext()).inflate(layoutId, titleBarLayout, true);
        }
    }

    public void replaceTitleBarLayout(@NonNull View view) {
        if (titleBarLayout != null) {
            titleBarLayout.removeAllViews();
            titleBarLayout.addView(view);
        }
    }

    /**
     * 创建返回按钮
     */
    protected View createBackItem() {
        ImageTextView backItem = TitleItemHelper.createItem(mAttachContext, R.drawable.base_back,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onTitleBackClick(v);
                    }
                });
        backItem.setId(R.id.base_title_back_view);
        return backItem;
    }

    /**
     * 标题栏默认的返回按钮点击事件
     */
    public void onTitleBackClick(@Nullable View view) {
        //hideTitleBar();
        hideSoftInput();
        backFragment();
    }

    /**
     * 关闭Fragment
     */
    public void backFragment() {
        backFragment(true);
    }

    public void backFragment(boolean checkBackPress) {
        FragmentManager fragmentManager = requireFragmentManager();
//        if (getParentFragment() == null) {
//            fragmentManager = requireFragmentManager();
//        } else {
//            fragmentManager = getChildFragmentManager();
//        }

        FragmentHelper.build(fragmentManager)
                .parentLayoutId(this)
                .defaultExitAnim()
                .setCheckBackPress(checkBackPress)
                .back(getActivity());
    }

    /**
     * 隐藏键盘
     */
    public void hideSoftInput() {
        View fragmentRootView = getView();
        if (fragmentRootView != null) {
            View focus = fragmentRootView.findFocus();
            if (focus instanceof EditText) {
                RSoftInputLayout.hideSoftInput(focus);
            }
        }
    }

    /**
     * 添加左边控制按钮
     */
    public void addLeftItem(@NonNull View itemView) {
        leftControl().addView(itemView);
    }

    public void addRightItem(@NonNull View itemView) {
        rightControl().addView(itemView);
    }

    public ViewGroupHelper rootControl() {
        return new ViewGroupHelper(getView());
    }

    public ViewGroupHelper contentControl() {
        return new ViewGroupHelper(baseViewHolder.vg(R.id.base_content_wrapper_layout));
    }

    public ViewGroupHelper leftControl() {
        return new ViewGroupHelper(baseViewHolder.vg(R.id.base_title_left_layout));
    }

    public ViewGroupHelper rightControl() {
        return new ViewGroupHelper(baseViewHolder.vg(R.id.base_title_right_layout));
    }

    public ViewGroupHelper titleControl() {
        return new ViewGroupHelper(titleBarLayout);
    }

    //</editor-fold>

    //<editor-fold desc="界面属性控制方法">

    /**
     * 隐藏标题栏
     */
    public void hideTitleBar() {
        ViewGroupHelper.build(baseViewHolder.itemView)
                .selector(R.id.base_title_bar_layout)
                .gone()
                .selector(R.id.base_title_shadow_view)
                .gone();
    }

    /**
     * 隐藏返回按钮
     */
    public void hideBackView() {
        leftControl().selector(R.id.base_title_back_view).gone();
    }

    /**
     * 移除返回按钮
     */
    public void removeBackView() {
        leftControl().selector(R.id.base_title_back_view).remove();
    }

    public void hideTitleShadow() {
        ViewGroupHelper.build((ViewGroup) getView()).selector(R.id.base_title_shadow_view).gone();
    }

    public void removeTitleShadow() {
        ViewGroupHelper.build((ViewGroup) getView()).selector(R.id.base_title_shadow_view).remove();
    }

    //</editor-fold>

    //<editor-fold desc="需要重写的方法">

    /**
     * 获取内容布局id
     */
    @LayoutRes
    protected int getContentLayoutId() {
        return -1;
    }

    @NonNull
    protected View createContentView(@NonNull ViewGroup contentWrapperLayout) {
        TextView textView = new TextView(contentWrapperLayout.getContext());
        textView.setText("默认的内容布局\n请重写\ngetContentLayoutId()\n或\ncreateContentView()\n方法, 自定义.");
        return textView;
    }
    //</editor-fold>

    //<editor-fold desc="情感图回调方法">

    @Override
    public void onAffectChangeBefore(@NonNull AffectUI affectUI, int fromAffect, int toAffect) {

    }

    @Override
    public void onAffectChange(@NonNull AffectUI affectUI, int fromAffect, int toAffect,
                               @Nullable View fromView, @NonNull View toView) {
    }

    @Override
    public void onAffectInitLayout(@NonNull AffectUI affectUI, int affect, @NonNull View rootView) {

    }

    //</editor-fold>


}
