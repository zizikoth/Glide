package com.memo.glide.application

import android.app.Application
import com.memo.glide.cache.DoubleCacheManager

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-25 17:36
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        DoubleCacheManager.init(this)
    }
}