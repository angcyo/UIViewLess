package com.angcyo.uiview.less.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/13
 */
public class BaseAppCompatActivity extends AppCompatActivity {

    protected RBaseViewHolder viewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewHolder = new RBaseViewHolder(getWindow().getDecorView());

        L.v("taskId:" + getTaskId());
    }
}
