package com.angcyo.uiview.less.recycler.item

import com.angcyo.uiview.less.recycler.RBaseViewHolder

/**
 * Created by angcyo on 2018/03/25 20:51
 */
open class RNoSupportItemHolder<T> : RExItemHolder<T>() {
    override fun onBindItemDataView(holder: RBaseViewHolder, posInData: Int, dataBean: T?) {

    }
}