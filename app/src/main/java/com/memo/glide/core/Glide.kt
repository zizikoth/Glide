package com.memo.glide.core

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.memo.glide.lifecycle.RequestManagerFragment
import com.memo.glide.request.BitmapRequest

/**
 * title:Glide主体包
 * describe:
 *
 * @author zhou
 * @date 2019-03-21 14:12
 */
object Glide {

    fun with(activity: AppCompatActivity): BitmapRequest {
        val supportFragmentManager = activity.supportFragmentManager
        //通过tag查找是否已经添加了fragment
        var fragment: Fragment? = supportFragmentManager.findFragmentByTag(this.javaClass.simpleName)
        //如果没有添加才进行添加
        if (fragment == null) {
            fragment = RequestManagerFragment()
            supportFragmentManager
                .beginTransaction()
                .add(fragment, this.javaClass.simpleName)
                .commitAllowingStateLoss()
        }
        return BitmapRequest(activity)
    }
}