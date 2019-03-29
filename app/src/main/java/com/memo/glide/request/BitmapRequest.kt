package com.memo.glide.request

import android.app.Activity
import android.graphics.Bitmap
import android.support.annotation.Nullable
import android.widget.ImageView
import com.memo.glide.listener.BitmapRequestListener
import com.memo.glide.utils.MD5Utils
import java.lang.ref.SoftReference

/**
 * title:图片请求配置
 * describe:
 *
 * @author zhou
 * @date 2019-03-21 14:13
 */
class BitmapRequest(private var activity: Activity) {
	
	private var imageUrl: String? = null
	
	private var uriMd5: String? = null
	
	private var mSoftImageView: SoftReference<ImageView>? = null
	
	private var requestListener: BitmapRequestListener? = null
	
	private var placeHolderRes: Int? = null
	
	private var errorRes: Int? = null
	
	fun load(imageUrl: String): BitmapRequest {
		this.imageUrl = imageUrl
		uriMd5 = MD5Utils.toMD5(imageUrl)
		return this
	}
	
	fun placeHolder(placeHolderRes: Int): BitmapRequest {
		this.placeHolderRes = placeHolderRes
		return this
	}
	
	fun error(errorRes: Int): BitmapRequest {
		this.errorRes = errorRes
		return this
	}
	
	fun listener(requestListener: BitmapRequestListener): BitmapRequest {
		this.requestListener = requestListener
		return this
	}
	
	fun listener(onResourceReady: (bitmap: Bitmap) -> Unit, onException: () -> Unit): BitmapRequest {
		listener(object : BitmapRequestListener {
			/**
			 * 图片加载成功
			 */
			override fun onResourceReady(bitmap: Bitmap) {
				onResourceReady(bitmap)
			}
			
			/**
			 * 图片加载异常
			 */
			override fun onException() {
				onException()
			}
		})
		return this
	}
	
	fun into(imageView: ImageView) {
		mSoftImageView = SoftReference(imageView)
		imageView.tag = uriMd5
        RequestManager.INSTANCE.addBitmapRequest(this)
	}

	fun getActivity():Activity = activity
	
	@Nullable
	fun getImageUrl(): String? = imageUrl
	
	@Nullable
	fun getUriMd5(): String? = uriMd5
	
	@Nullable
	fun getPlaceHolder(): Int? = placeHolderRes
	
	@Nullable
	fun getError(): Int? = errorRes
	
	@Nullable
	fun getRequestListener(): BitmapRequestListener? = requestListener
	
	@Nullable
	fun getImageView(): ImageView? = mSoftImageView?.get()
	
	override fun toString(): String {
		return "BitmapRequest(activity=$activity, imageUrl=$imageUrl, uriMd5=$uriMd5, mSoftImageView=$mSoftImageView, requestListener=$requestListener, placeHolderRes=$placeHolderRes, errorRes=$errorRes)"
	}
	
	
}