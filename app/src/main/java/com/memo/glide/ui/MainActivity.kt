package com.memo.glide.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.memo.glide.R
import com.memo.glide.core.Glide
import com.memo.glide.utils.imageUrls
import com.memo.glide.utils.log
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val url = "http://pic36.nipic.com/20131203/12728082_134842497000_2.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mIvSingle.setOnClickListener {
            addImage(url)
        }
        mIvMany.setOnClickListener {
            for (url in imageUrls) {
                addImage(url)
            }
        }
    }

    private fun addImage(url: String) {
        val imageView = ImageView(this)
        imageView.layoutParams = ViewGroup.LayoutParams(900, 600)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        mLlContainer.addView(imageView)
        Glide.with(this)
                .load(url)
                .placeHolder(R.mipmap.loading)
                .error(R.mipmap.error)
                .listener({
                    log("图片加载成功 url = $url width = ${it.width} height = ${it.height}")
                }, {
                    log("图片加载失败")
                })
                .into(imageView)

    }

}
