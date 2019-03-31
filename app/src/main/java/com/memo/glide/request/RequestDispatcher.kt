package com.memo.glide.request

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.memo.glide.cache.DoubleCacheManager
import com.memo.glide.lifecycle.LifeCycleObservable
import com.memo.glide.utils.MD5Utils
import com.memo.glide.utils.log
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.BlockingQueue

/**
 * title:请求调度
 * describe:
 *
 * @author zhou
 * @date 2019-03-25 17:11
 */
class RequestDispatcher(requestQueue: BlockingQueue<BitmapRequest>) : Thread() {

    /*** Handler 用户跳转到主线程进行展示图片 ***/
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    /*** 请求队列 ***/
    private val queue: BlockingQueue<BitmapRequest> = requestQueue

    /**
     * 运行
     */
    override fun run() {
        //死循环 由于是阻塞式 在子线程中 所以不会发生界面卡顿
        while (!isInterrupted) {
            //获取当前的任务
            try {
                val bitmapRequest: BitmapRequest = queue.take()
                showPlaceHolder(bitmapRequest)
                //1、获取bitmap 先从缓存中获取 没有再从网络获取
                val bitmap: Bitmap? = getBitmap(bitmapRequest)
                //2、通过Handler到ui线程展示图片
                showImageInUiThread(bitmapRequest, bitmap)
                //生命周期
                //3、此时已经把缓存放入 那么把缓存的key uriMd5和Activity结合
                val uriMd5 = bitmapRequest.getUriMd5()
                uriMd5?.let {
                    LifeCycleObservable.instance.add(bitmapRequest.getActivity().hashCode(), uriMd5)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 显示占位图
     */
    private fun showPlaceHolder(request: BitmapRequest) {
        request.getPlaceHolder()?.let { placeHolder ->
            request.getImageView()?.let { imageView ->
                handler.post {
                    imageView.setImageResource(placeHolder)
                }
            }
        }
    }


    /**
     * 从缓存中获取bitmap
     */
    private fun getBitmap(bitmapRequest: BitmapRequest): Bitmap? {
        //从缓存中获取bitmap 先从内存中获取 再从磁盘中获取
        var bitmap: Bitmap? = DoubleCacheManager.get(bitmapRequest.getUriMd5())
        log("缓存读取成功？ ${bitmap != null} ")
        if (bitmap != null) {
            return bitmap
        } else {
            //从网络中获取
            bitmap = downloadImage(bitmapRequest.getImageUrl())
            return bitmap
        }
    }

    /**
     * 网络下载缓存
     */
    private fun downloadImage(uri: String?): Bitmap? {
        if (uri == null || uri.isEmpty()) {
            return null
        }
        var inputStream: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            val url = URL(uri)
            val conn: URLConnection = url.openConnection()
            inputStream = conn.getInputStream()
            bitmap = BitmapFactory.decodeStream(inputStream)
            //！！！将图片放入缓存
            if (bitmap != null) {
                val key: String = MD5Utils.toMD5(uri)
                log("key = $key 存入缓存")
                DoubleCacheManager.put(key, bitmap)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return bitmap
    }

    /**
     * 在ui线程中展示图片
     */
    private fun showImageInUiThread(request: BitmapRequest, bitmap: Bitmap?) {
        //需要判断图片是否为空 因为是软应用
        //bitmap是否为空
        //tag是否匹配
        handler.post {
            val imageView: ImageView? = request.getImageView()
            val errorRes: Int? = request.getError()
            if (imageView != null && imageView.tag == request.getUriMd5()) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    errorRes?.let {
                        imageView.setImageResource(it)
                    }
                }
            }
        }

        request.getRequestListener()?.let {
            if (bitmap != null) {
                handler.post {
                    it.onResourceReady(bitmap)
                }
            } else {
                handler.post {
                    it.onException()
                }
            }
        }
    }


}