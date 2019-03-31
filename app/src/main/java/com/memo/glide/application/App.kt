package com.memo.glide.application

import android.app.Application
import android.content.Context
import com.memo.glide.cache.DoubleCacheManager

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-25 17:36
 */
class App : Application() {

    companion object {
        lateinit var appContext: Context
    }


    override fun onCreate() {
        super.onCreate()
        appContext = this
        DoubleCacheManager.init(this)
    }
}