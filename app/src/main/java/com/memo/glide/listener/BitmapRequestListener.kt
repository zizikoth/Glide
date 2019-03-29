package com.memo.glide.listener

import android.graphics.Bitmap

/**
 * title:图片请求回调
 * describe:
 *
 * @author zhou
 * @date 2019-03-21 14:17
 */
interface BitmapRequestListener {
    /**
     * 图片加载成功
     */
    fun onResourceReady(bitmap:Bitmap)

    /**
     * 图片加载异常
     */
    fun onException()
}