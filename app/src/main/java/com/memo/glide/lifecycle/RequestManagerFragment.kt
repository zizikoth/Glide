package com.memo.glide.lifecycle

import android.support.v4.app.Fragment

/**
 * title:
 * describe:
 * 1:13:00
 * 当前Activity对应的所有Bitmap 当前Activity移除所有的key
 *
 * @author zhou
 * @date 2019-03-26 14:03
 */
class RequestManagerFragment : Fragment() {

    private val activityCode: Int by lazy { activity!!.hashCode() }

    override fun onDetach() {
        super.onDetach()
        LifeCycleObservable.instance.remove(activityCode)
    }
}