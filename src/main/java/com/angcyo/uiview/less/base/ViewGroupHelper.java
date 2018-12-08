package com.angcyo.uiview.less.base;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.angcyo.uiview.less.R;

public class ViewGroupHelper {
    ViewGroup viewGroup;
    View selectorView;

    public ViewGroupHelper(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
    }

    public ViewGroupHelper addView(@NonNull View itemView) {
        return addView(-1, itemView);
    }

    public ViewGroupHelper addView(int index, @NonNull View itemView) {
        if (viewGroup != null) {
            viewGroup.addView(itemView, index);
        }
        return this;
    }

    public ViewGroupHelper remove(int index) {
        if (viewGroup != null) {
            if (viewGroup.getChildCount() > index) {
                viewGroup.removeViewAt(index);
            }
        }
        return this;
    }

    public ViewGroupHelper remove() {
        if (viewGroup != null && selectorView != null) {
            viewGroup.removeView(selectorView);
        }
        return this;
    }

    public ViewGroupHelper visible(int visibility) {
        if (viewGroup != null && selectorView != null) {
            if (selectorView.getVisibility() != visibility) {
                selectorView.setVisibility(visibility);
            }
        }
        return this;
    }

    public ViewGroupHelper gone() {
        return visible(View.GONE);
    }

    public ViewGroupHelper visible() {
        return visible(View.VISIBLE);
    }

    public ViewGroupHelper invisible() {
        return visible(View.INVISIBLE);
    }

    public ViewGroupHelper selector(@IdRes int id) {
        if (viewGroup != null) {
            selectorView = viewGroup.findViewById(id);
        }
        return this;
    }

    public ViewGroupHelper selectorByIndex(int index) {
        selectorView = getView(index);
        return this;
    }

    public ViewGroupHelper replace(int index, @NonNull View newView) {
        if (viewGroup != null) {
            if (viewGroup.getChildCount() > index) {
                viewGroup.removeViewAt(index);
                viewGroup.addView(newView, index);
            }
        }
        return this;
    }

    @Nullable
    public View getView(int index) {
        if (viewGroup != null) {
            if (viewGroup.getChildCount() > index) {
                return viewGroup.getChildAt(index);
            }
        }
        return null;
    }
}