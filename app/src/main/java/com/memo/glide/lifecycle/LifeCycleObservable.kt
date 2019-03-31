package com.memo.glide.lifecycle

import com.memo.glide.cache.DoubleCacheManager
import com.memo.glide.utils.log
import java.util.*

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-26 14:56
 */
class LifeCycleObservable private constructor() {

    private val activityMap: HashMap<Int, ArrayList<String>> by lazy { hashMapOf<Int, ArrayList<String>>() }
    private val addCode: StringBuilder by lazy { StringBuilder() }
    private val removeCode: StringBuilder by lazy { StringBuilder() }

    companion object {
        val instance: LifeCycleObservable by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            LifeCycleObservable()
        }
    }

    /**
     * 将ActivityCode 和 对应的bitmap的imageUrlMd5集合相对应
     */
    fun add(activityCode: Int, imageUrlMd5: String) {
        addCode.append("activityCode = $activityCode  imageUrlMd5 = $imageUrlMd5 \n")
        var urlMd5List: ArrayList<String>? = activityMap[activityCode]
        if (urlMd5List == null) {
            urlMd5List = arrayListOf()
        }
        if (!urlMd5List.contains(imageUrlMd5)) {
            urlMd5List.add(imageUrlMd5)
        }
        activityMap[activityCode] = urlMd5List
        log("add  activityCode = $activityCode  imageUrlMd5 = $imageUrlMd5 urlMd5List = ${urlMd5List.size}")
    }


    fun remove(activityCode: Int) {
        val urlMd5List: ArrayList<String>? = activityMap[activityCode]
        urlMd5List?.let {
            for (imageUrlMd5 in it) {
                //只是从内存中将所有的缓存获取
                val bitmap = DoubleCacheManager.get(imageUrlMd5, DoubleCacheManager.LRU)
                DoubleCacheManager.remove(imageUrlMd5, DoubleCacheManager.LRU)
                //回收
                if (bitmap != null && !bitmap.isRecycled) {
                    bitmap.recycle()
                }
                removeCode.append("activityCode = $activityCode  imageUrlMd5 = $imageUrlMd5 \n")
            }
            //系统回收  有些手机会执行 有些手机不会执行 可以自己手动GC查看内存
            System.gc()
            //目前添加的必删除的多
            log("\nremoveCode \n$removeCode")
        }
        activityMap.clear()

    }

}