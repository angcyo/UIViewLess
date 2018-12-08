package com.angcyo.uiview.less.iview;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * 动态控制情感图切换
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/02
 */
public class AffectUI {

    /**
     * 情感图 内容, 此变量用来显示内容视图. 请勿用来注册
     */
    public static final int AFFECT_CONTENT = 1;
    /**
     * 情感图 加载中
     */
    public static final int AFFECT_LOADING = 2;
    /**
     * 情感图 异常
     */
    public static final int AFFECT_ERROR = 4;
    /**
     * 情感图 其他
     */
    public static final int AFFECT_OTHER = 8;

    /**
     * 当切换到非内容情感时, 内容布局的显示方式, 默认invisible
     */
    public static final int CONTENT_AFFECT_NONE = 0;
    public static final int CONTENT_AFFECT_INVISIBLE = 1;
    public static final int CONTENT_AFFECT_GONE = 2;
    public static final int CONTENT_AFFECT_REMOVE = 4;

    int affectStatus = -1;

    public static Builder build(@NonNull ViewGroup parent) {
        return new Builder(parent);
    }

    Builder builder;

    /**
     * 默认使用parent的第一个view, 当做内容
     */
    View contentView;

    /**
     * inflate之后的缓存
     */
    SparseArray<View> viewMap = new SparseArray<View>();

    /**
     * 支持动态修改此属性
     */
    int contentAffect;

    public AffectUI(Builder builder) {
        this.builder = builder;
        contentAffect = builder.contentAffect;
        initAffect();
    }

    private void initAffect() {
        int count = builder.parent.getChildCount();
        if (contentView == null && count > 0) {
            contentView = builder.parent.getChildAt(0);
        }
        for (int i = 0; i < builder.layoutMap.size(); i++) {
            viewMap.put(builder.layoutMap.keyAt(i), null);
        }
    }

    public void setContentView(View contentView) {
        this.contentView = contentView;
    }

    /**
     * 显示情感图
     */
    public void showAffect(int affect) {
        if (affectStatus == affect) {
            //相同情感
        } else {
            switchAffect(affect);
        }
    }

    //单独处理内容布局
    private void affectContentViewHandle(boolean show) {
        if (contentView != null) {
            switch (contentAffect) {
                case CONTENT_AFFECT_NONE:
//                    if (contentView.getVisibility() != View.VISIBLE) {
//                        contentView.setVisibility(View.VISIBLE);
//                    }
//
//                    if (contentView.getParent() == null) {
//                        builder.parent.addView(contentView, 0);
//                    }
                    break;
                case CONTENT_AFFECT_GONE:
                    if (show) {
                        contentView.setVisibility(View.VISIBLE);
                    } else {
                        contentView.setVisibility(View.GONE);
                    }
                    break;
                case CONTENT_AFFECT_REMOVE:
                    ViewParent parent = contentView.getParent();
                    if (parent instanceof ViewGroup) {
                        if (show) {
                            builder.parent.addView(contentView, 0);
                        } else {
                            ((ViewGroup) parent).removeView(contentView);
                        }
                    }
                    break;
                case CONTENT_AFFECT_INVISIBLE:
                    if (show) {
                        contentView.setVisibility(View.VISIBLE);
                    } else {
                        contentView.setVisibility(View.INVISIBLE);
                    }
                default:
                    break;
            }
        }
    }

    private void switchAffect(int affect) {
        int oldAffect = affectStatus;
        affectStatus = affect;

        if (builder.affectChangeListener != null) {
            builder.affectChangeListener.onAffectChangeBefore(this, oldAffect, affect);
        }

        affectContentViewHandle(affect == AFFECT_CONTENT);
        for (int i = 0; i < viewMap.size(); i++) {
            int key = viewMap.keyAt(i);
            View view = viewMap.get(key);

            if (key == affect) {
                //需要显示的情感布局
                if (view == null) {
                    //没有缓存
                    int layoutId = builder.layoutMap.get(key);
                    View rootView = LayoutInflater.from(builder.parent.getContext()).inflate(layoutId, builder.parent, false);
                    builder.parent.addView(rootView);

                    viewMap.put(affect, rootView);

                    if (builder.affectChangeListener != null) {
                        builder.affectChangeListener.onInitLayout(this, affect, rootView);
                    }
                } else {
                    builder.parent.addView(view);
                }
            } else {
                //需要隐藏的情感布局
                if (view != null && view.getParent() != null) {
                    builder.parent.removeView(view);
                }
            }
        }

        if (builder.affectChangeListener != null) {
            builder.affectChangeListener.onAffectChange(this, oldAffect, affect, viewMap.get(oldAffect), viewMap.get(affect));
        }
    }

    public void setContentAffect(int contentAffect) {
        this.contentAffect = contentAffect;
    }

    /**
     * 是否有情感图在显示
     */
    public boolean haveAffectShow() {
        boolean have = false;
        for (int i = 0; i < viewMap.size(); i++) {
            int key = viewMap.keyAt(i);
            View view = viewMap.get(key);

            if (view != null && view.getParent() != null) {
                have = true;
                break;
            }
        }
        return have;
    }

    public static class Builder {
        /**
         * 容器
         */
        ViewGroup parent;

        /**
         * 情感图和布局对应关系
         */
        SparseIntArray layoutMap = new SparseIntArray();

        /**
         * 当切换到非内容情感时, 内容布局的显示方式
         */
        int contentAffect = CONTENT_AFFECT_INVISIBLE;

        OnAffectListener affectChangeListener;

        public Builder(@NonNull ViewGroup parent) {
            this.parent = parent;
        }

        public Builder register(int affect, @LayoutRes int layoutId) {
            layoutMap.put(affect, layoutId);
            return this;
        }

        public Builder setContentAffect(int contentAffect) {
            this.contentAffect = contentAffect;
            return this;
        }

        public Builder setAffectChangeListener(OnAffectListener affectChangeListener) {
            this.affectChangeListener = affectChangeListener;
            return this;
        }

        public AffectUI create() {
            return new AffectUI(this);
        }
    }

    /**
     * 状态切换通知监听
     */
    public interface OnAffectListener {

        void onAffectChangeBefore(AffectUI affectUI, int fromAffect, int toAffect);

        void onAffectChange(AffectUI affectUI, int fromAffect, int toAffect, @Nullable View fromView, @NonNull View toView);

        /**
         * 只在第一次inflate的时候, 会调用
         */
        void onInitLayout(AffectUI affectUI, int affect, @NonNull View rootView);
    }
}
