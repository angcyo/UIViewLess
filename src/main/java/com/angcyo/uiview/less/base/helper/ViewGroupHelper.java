package com.angcyo.uiview.less.base.helper;

import android.graphics.drawable.Drawable;
import android.support.annotation.*;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.angcyo.uiview.less.widget.ImageTextView;

public class ViewGroupHelper {
    View parentView;
    View selectorView;

    public static ViewGroupHelper build(View parentView) {
        return new ViewGroupHelper(parentView);
    }

    public ViewGroupHelper(View parentView) {
        this.parentView = parentView;
    }

    public ViewGroupHelper addView(@NonNull View itemView) {
        return addView(-1, itemView);
    }

    public ViewGroupHelper addView(int index, @NonNull View itemView) {
        if (parentView instanceof ViewGroup) {
            ((ViewGroup) parentView).addView(itemView, index);
        }
        return this;
    }

    public ViewGroupHelper remove(int index) {
        if (parentView instanceof ViewGroup) {
            if (((ViewGroup) parentView).getChildCount() > index) {
                ((ViewGroup) parentView).removeViewAt(index);
            }
        }
        return this;
    }

    public ViewGroupHelper remove() {
        if (parentView instanceof ViewGroup && selectorView != null) {
            ((ViewGroup) parentView).removeView(selectorView);
        }
        return this;
    }

    public ViewGroupHelper visible(int visibility) {
        if (parentView != null && selectorView != null) {
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
        if (parentView != null) {
            selectorView = parentView.findViewById(id);
        }
        return this;
    }

    public ViewGroupHelper selectorByIndex(int index) {
        selectorView = getView(index);
        return this;
    }

    public ViewGroupHelper setText(String text) {
        if (selectorView != null) {
            if (selectorView instanceof TextView) {
                ((TextView) selectorView).setText(text);
            } else if (selectorView instanceof ImageTextView) {
                ((ImageTextView) selectorView).setShowText(text);
            }
        }
        return this;
    }

    public ViewGroupHelper setBackgroundColor(@ColorInt int color) {
        if (selectorView != null) {
            selectorView.setBackgroundColor(color);
        }
        return this;
    }

    public ViewGroupHelper setBackground(@Nullable Drawable background) {
        if (selectorView != null) {
            ViewCompat.setBackground(selectorView, background);
        }
        return this;
    }

    public ViewGroupHelper setImageResource(@DrawableRes int resId) {
        if (selectorView != null) {
            if (selectorView instanceof ImageTextView) {
                ((ImageTextView) selectorView).setImageResource(resId);
            } else if (selectorView instanceof ImageView) {
                ((ImageView) selectorView).setImageResource(resId);
            }
        }
        return this;
    }

    public <T> T cast() {
        if (selectorView == null) {
            return null;
        }
        return (T) selectorView;
    }

    public ViewGroupHelper replace(int index, @NonNull View newView) {
        if (parentView instanceof ViewGroup) {
            if (((ViewGroup) parentView).getChildCount() > index) {
                ((ViewGroup) parentView).removeViewAt(index);
                ((ViewGroup) parentView).addView(newView, index);
            }
        }
        return this;
    }

    @Nullable
    public View getView(int index) {
        if (parentView instanceof ViewGroup) {
            if (((ViewGroup) parentView).getChildCount() > index) {
                return ((ViewGroup) parentView).getChildAt(index);
            }
        }
        return null;
    }
}