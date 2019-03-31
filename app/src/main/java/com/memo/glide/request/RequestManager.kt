package com.memo.glide.request

import com.memo.glide.utils.log
import java.util.concurrent.LinkedBlockingQueue

/**
 * title:请求队列管理
 * describe:
 *
 * @author zhou
 * @date 2019-03-25 17:05
 */
class RequestManager private constructor() {


    /*** 请求调度 给予多个来解决并发问题 ***/
    private val dispatchers: ArrayList<RequestDispatcher> = arrayListOf()


    companion object {
        val INSTANCE: RequestManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            val manager = RequestManager()
            manager.start()
            manager
        }
    }

    /*** 请求队列 ***/
    private val requestQueue = LinkedBlockingQueue<BitmapRequest>()

    /**
     * 添加一个网络请求
     */
    fun addBitmapRequest(request: BitmapRequest) {
        //如果队列中没有这个图片请求 添加
        if (!requestQueue.contains(request)) {
            requestQueue.add(request)
        } else {
            log("当前任务已存在\n$request")
        }
    }

    /**
     * 开启请求，通过RequestDispatcher进行调度 获取bitmap
     */
    private fun start() {
        stop()
        val threadCount: Int = Runtime.getRuntime().availableProcessors()
        for (i in 0 until threadCount) {
            val requestDispatcher = RequestDispatcher(requestQueue)
            //开始加载bitmap
            requestDispatcher.start()
            dispatchers.add(requestDispatcher)
        }
    }

    /**
     * 关闭线程
     */
    private fun stop() {
        if (!dispatchers.isNullOrEmpty()) {
            for (dispatcher: RequestDispatcher in dispatchers) {
                //如果线程没有被中断 中断线程
                if (!dispatcher.isInterrupted) {
                    dispatcher.interrupt()
                }
            }
            dispatchers.clear()
        }
    }

}