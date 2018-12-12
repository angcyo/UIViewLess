package com.angcyo.uiview.less.base.helper;

import android.graphics.drawable.Drawable;
import android.support.annotation.*;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.angcyo.uiview.less.resources.ResUtil;
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

    public ViewGroupHelper selector(@NonNull View view) {
        selectorView = view;
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

    public ViewGroupHelper setTextColor(@ColorInt int color) {
        if (selectorView != null) {
            if (selectorView instanceof TextView) {
                ((TextView) selectorView).setTextColor(color);
            } else if (selectorView instanceof ImageTextView) {
                ((ImageTextView) selectorView).setTextShowColor(color);
            }
        }
        return this;
    }

    //<editor-fold desc="Drawable过滤颜色方法">

    public ViewGroupHelper colorFilter(@ColorInt int color) {
        if (selectorView != null) {
            colorFilterView(selectorView, color);
        }
        return this;
    }

    public ViewGroupHelper colorFilter(@Nullable ViewGroup view, @ColorInt int color) {
        if (view != null) {
            for (int i = 0; i < view.getChildCount(); i++) {
                View childAt = view.getChildAt(i);
                if (childAt instanceof ViewGroup) {
                    colorFilter((ViewGroup) childAt, color);
                } else {
                    colorFilterView(childAt, color);
                }
            }
        }
        return this;
    }

    public ViewGroupHelper colorFilterView(@Nullable View view, @ColorInt int color) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                colorFilter((ViewGroup) view, color);
            } else if (view instanceof TextView) {
                // ((TextView) view).setTextColor(color);
            } else if (view instanceof ImageView) {
                ((ImageView) view).setImageDrawable(ResUtil.filterDrawable(((ImageView) view).getDrawable(), color));
            }
        }
        return this;
    }
    //</editor-fold>

    //<editor-fold desc="文本过滤颜色的方法">

    public ViewGroupHelper textColorFilter(@ColorInt int color) {
        if (selectorView != null) {
            textColorFilterView(selectorView, color);
        }
        return this;
    }

    public ViewGroupHelper textColorFilter(@Nullable ViewGroup view, @ColorInt int color) {
        if (view != null) {
            for (int i = 0; i < view.getChildCount(); i++) {
                View childAt = view.getChildAt(i);
                if (childAt instanceof ViewGroup) {
                    textColorFilter((ViewGroup) childAt, color);
                } else {
                    textColorFilterView(childAt, color);
                }
            }
        }
        return this;
    }

    public ViewGroupHelper textColorFilterView(@Nullable View view, @ColorInt int color) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                textColorFilter((ViewGroup) view, color);
            } else if (view instanceof TextView) {
                ((TextView) view).setTextColor(color);
            } else if (view instanceof ImageTextView) {
                ((ImageTextView) view).setTextShowColor(color);
            }
        }
        return this;
    }

    //</editor-fold>

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