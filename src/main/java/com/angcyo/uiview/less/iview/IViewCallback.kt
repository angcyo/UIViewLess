package com.angcyo.uiview.less.iview

interface IViewCallback<T> {
    fun onRequestStart()
    fun onRequestEnd(resultBody: T?)
}