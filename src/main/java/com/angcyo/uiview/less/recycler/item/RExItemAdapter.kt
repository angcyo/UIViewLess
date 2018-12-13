package com.angcyo.uiview.less.recycler.item

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.angcyo.lib.L
import com.angcyo.uiview.less.recycler.RBaseViewHolder
import com.angcyo.uiview.less.recycler.adapter.RExBaseAdapter
import com.angcyo.uiview.less.utils.RUtils

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：实现Item Layout, Type , Bind 分发
 * 创建人员：Robi
 * 创建时间：2018/03/16 14:01
 * 修改人员：Robi
 * 修改时间：2018/03/16 14:01
 * 修改备注：
 * Version: 1.0.0
 */
open class RExItemAdapter<ItemType, DataType> :
    RExBaseAdapter<String, DataType, String> {

    var itemFactory: RExItemFactory<ItemType, DataType>

    constructor(context: Context, itemFactory: RExItemFactory<ItemType, DataType>) : super(context) {
        this.itemFactory = itemFactory
    }

    constructor(context: Context, datas: List<DataType>, itemFactory: RExItemFactory<ItemType, DataType>) : super(
        context,
        datas
    ) {
        this.itemFactory = itemFactory
    }

    override fun getItemLayoutId(viewType: Int): Int {
        return itemFactory.getItemLayoutId(viewType)
    }

    override fun onChildViewAttachedToWindow(view: View, adapterPosition: Int, layoutPosition: Int) {
        super.onChildViewAttachedToWindow(view, adapterPosition, layoutPosition)
        getItemHolderByPosition(adapterPosition - headerCount)?.let {
            it.onChildViewAttachedToWindow(view, adapterPosition, layoutPosition)
        }
    }

    override fun onChildViewDetachedFromWindow(view: View, adapterPosition: Int, layoutPosition: Int) {
        super.onChildViewDetachedFromWindow(view, adapterPosition, layoutPosition)
        getItemHolderByPosition(adapterPosition - headerCount)?.let {
            it.onChildViewDetachedFromWindow(view, adapterPosition, layoutPosition)
        }
    }

    /**必须调用此方法*/
    open fun initItemFactory() {
        itemFactory.initItemFactory(this)
    }

    /**根据position, 返回Adapter对应的item类型*/
    override fun getItemType(position: Int): Int {
        val data: DataType? = if (position >= mAllDatas.size || position < 0) {
            //return NO_ITEM_TYPE
            null
        } else {
            mAllDatas[position]
        }
        return itemFactory.getItemType(data, position)
    }

    override fun onBindDataView(holder: RBaseViewHolder, posInData: Int, dataBean: DataType?) {
        super.onBindDataView(holder, posInData, dataBean)
        val itemHolder = itemFactory.getItemLayoutHolder(holder.itemViewType)
        itemHolder?.onBindItemDataView(holder, posInData, dataBean)
    }

    /**根据位置, 返回处理的ItemHolder*/
    open fun getItemHolderByPosition(position: Int): RExItemHolder<DataType>? {
        return itemFactory.getItemLayoutHolder(getItemType(position))
    }

    /**根据类型, 返回处理的ItemHolder*/
    fun getItemHolderByItemType(itemType: ItemType): RExItemHolder<DataType>? {
        var result: RExItemHolder<DataType>? = null
        mAllDatas.forEachIndexed { index, dataType ->
            if (itemType == itemFactory.getItemTypeFromData(dataType, index)) {
                result = getItemHolderByPosition(index)
            }
        }
        return result
    }

    /**根据类型, 返回相同类型对应的数据列表*/
    fun getDataByItemType(itemType: ItemType): MutableList<DataType> {
        val result = mutableListOf<DataType>()
        mAllDatas.forEachIndexed { index, dataType ->
            if (itemType == itemFactory.getItemTypeFromData(dataType, index)) {
                result.add(dataType)
            }
        }
        return result
    }

    /**根据类型, 返回相同类型对应的数据索引列表*/
    fun getIndexByItemType(itemType: ItemType): MutableList<Int> {
        val result = mutableListOf<Int>()
        mAllDatas.forEachIndexed { index, dataType ->
            if (itemType == itemFactory.getItemTypeFromData(dataType, index)) {
                result.add(index)
            }
        }
        return result
    }

    /**返回数据在相同类型数据列表中的索引*/
    fun indexOf(itemType: ItemType, data: DataType): Int {
        return getDataByItemType(itemType).indexOf(data)
    }

    fun removeDataByItemType(itemType: ItemType) {
        if (!RUtils.isListEmpty(mAllDatas)) {
            for (index in mAllDatas.size - 1 downTo 0) {
                if (itemType == itemFactory.getItemTypeFromData(mAllDatas[index], index)) {
                    deleteItem(index)
                }
            }
        }
    }

    /**
     * 替换数据列表中, 相同类型的数据item, 如果不存在则, 添加.
     *
     * 日过找到多个相同的item,则只更新第一个
     * */
    fun replaceData(data: DataType) {
        var oldIndex = -1
        val targetItemType = itemFactory.getItemTypeFromData(data, oldIndex)

        for (i in 0 until mAllDatas.size) {
            val dataType = mAllDatas[i]
            val typeFromData = itemFactory.getItemTypeFromData(dataType, i)

            if (targetItemType is String) {
                if (TextUtils.equals(targetItemType, typeFromData as String)) {
                    oldIndex = i
                    break
                }
            } else {
                if (targetItemType == typeFromData) {
                    oldIndex = i
                    break
                }
            }
        }

        if (oldIndex != -1) {
            //找到了旧的数据
            mAllDatas[oldIndex] = data
            notifyItemChanged(oldIndex)
        } else {
            //没有旧数据
            addLastItem(data)
        }
    }

    /**
     * 替换连续相同类型的数据List, 如果是0大小的list, 那么会清除之前所有的item
     *
     * 需要保证数据是连续的
     * */
    fun replaceDataList(
        dataList: List<DataType>?,
        itemType: ItemType? = null, //当 deleteAllOnListEmpty 为ture 时, 需要指定
        deleteAllOnListEmpty: Boolean = true
    ) {
        if (dataList == null) {
            return
        }

        val isListEmpty = RUtils.isListEmpty(dataList)

        if (deleteAllOnListEmpty) {
        } else {
            if (isListEmpty) {
                return
            }
        }

        var oldStartIndex = -1
        var oldSize = 0
        val newSize = dataList.size

        val targetItemType = if (isListEmpty) {
            itemType
        } else {
            itemFactory.getItemTypeFromData(dataList.first(), oldStartIndex)
        }

        if (targetItemType == null) {
            L.e("未知的ItemType:$targetItemType")
            return
        }

        for (i in 0 until mAllDatas.size) {
            val dataType = mAllDatas[i]
            val typeFromData = itemFactory.getItemTypeFromData(dataType, i)

            if (targetItemType is String) {
                if (TextUtils.equals(targetItemType, typeFromData as String)) {
                    if (oldStartIndex == -1) {
                        oldStartIndex = i
                    }
                    oldSize++
                }
            } else {
                if (targetItemType == typeFromData) {
                    if (oldStartIndex == -1) {
                        oldStartIndex = i
                    }
                    oldSize++
                }
            }
        }

        if (oldStartIndex != -1) {
            //找到了旧的数据
            if (newSize > oldSize) {
                for (i in oldStartIndex until oldStartIndex + oldSize) {
                    mAllDatas[i] = dataList[i - oldStartIndex]
                }
                notifyItemRangeChanged(oldStartIndex, oldSize)

                val lastIndex = oldStartIndex + oldSize
                for (i in lastIndex until oldStartIndex + newSize) {
                    mAllDatas.add(i, dataList[i - oldStartIndex])
                }
                notifyItemRangeInserted(lastIndex, newSize - oldSize)
            } else if (newSize == oldSize) {
                for (i in oldStartIndex until oldStartIndex + oldSize) {
                    mAllDatas[i] = dataList[i - oldStartIndex]
                }
                notifyItemRangeChanged(oldStartIndex, oldSize)
            } else {
                //newSize < oldSize
                for (i in oldStartIndex until oldStartIndex + newSize) {
                    mAllDatas[i] = dataList[i - oldStartIndex]
                }
                if (newSize > 0) {
                    notifyItemRangeChanged(oldStartIndex, newSize)
                }

                for (i in oldStartIndex + oldSize - 1 downTo oldStartIndex + newSize) {
                    mAllDatas.removeAt(i)
                }
                notifyItemRangeRemoved(oldStartIndex + newSize, oldSize - newSize)
            }
        } else {
            //没有旧数据
            appendAllData(dataList)
        }

    }

}