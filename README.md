# Glide
### 概况
首先为什么要用Glide呢，最主要的原因就是缓存机制和它的生命周期绑定
#### 一、缓存机制
所有的图片缓存机制都是使用的三级缓存，即内存缓存，磁盘缓存，服务器存储。
![三级缓存.png](https://upload-images.jianshu.io/upload_images/4356451-4a0481fc3f36f47c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
除此之外，Glide的缓存的数据是由Glide的各种设置决定的，例如一张图片100x100设置宽高80x80和50x50的图片两种类型展示，那么缓存中会存在有80x80和50x50的两张图片，当你再次设置的时候就直接读取这两张图片，而不是读取原图重新设置宽高，这里采用的是使用空间换取时间的策略，这就是见仁见智了，由于目前手机的内存会变得越来越大，所以这种方案也是没有问题的。

#### 二、生命周期绑定
Glide.with(context)
在使用Glide的时候会传入当前上下文，但是通过上下文是无法获取当前Activity生命周期回调的，所以Glide使用的策略是在当前Activity中添加一个空白的Fragment，通过该Fragment来进行回调当前界面的生命周期，在当前Fragment的销毁的时候，将内存中的绑定当前Activity的bitmap回收，可以将占用的内存进行释放。这个也是我最喜欢Glide的原因，其他的图片加载框架都是事先占用部分的手机内存，只有在App退出的时候才会进行释放，内存允许的情况下，可以达到当前App所有的图片都放在内存中，加载效率更快。可是在实际情况中，不同页面出现相同图片的概率还是比较小的，而且就算是出现相同的图片，那也只是很少的一部分，所以我更欣赏Glide的做法，将bitmap和生命周期绑定，生命周期结束bitmap回收，这也是更符合我们的编程思想。
这里有一个比较重要的一点就是如何将bitmap与当前的Activity进行绑定。这里使用的方案是创建一个HashMap<Int, ArrayList<String>>，第一个参数放入Activity的hashCode，用来对应Activity，第二个参数放入内存缓存中存入bitmap的key，那么我们可以通过找到当前Activity的hashCode来获取所有的内存缓存的bitmap的key集合，再通过这些key来获取bitmap进行回收。

### 手撸Glide （非官方）
#### 缓存
那么首先仿照Glide的样子来创建一个Glide
```
        Glide.with(this)
                .load(url)
                .placeHolder(R.mipmap.loading)
                .error(R.mipmap.error)
                .listener({
                    log("图片加载成功 url = $url " +
                            "width = ${it.width} " +
                            "height = ${it.height}")
                }, {
                    log("图片加载失败")
                })
                .into(imageView)
```
那么对于Glide来说with方法创建了一个BitmapRequest，所以with方法返回一个bitmap的请求实体
###### Glide
```
object Glide {
    fun with(activity: AppCompatActivity): BitmapRequest {
        return BitmapRequest(activity)
    }
}
```
###### BitmapRequest
对于一个BitmapRequest来说我们需要知道这一张图片的基本数据，地址url，图片imageView，然后我们可以添加一些友好设置，加载占位图placeHolderRes，加载失败图errorRes，加载监听listener成功和失败。对于图片来说，我们并不想直接持有，所以采用软引用的方式SoftReference<ImageView>，此外对于缓存来说需要一个key所以还需要一个参数uriMd5
```
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
```
###### BitmapManager
由于图片加载肯定不会是一张一张的加载，这里就存在并发的问题，那么我们就需要构建一个加载队列，将一个个图片加载请求放入这个请求队列去进行加载图片。那么我们可以看手机的cpu核心数来进行分配几条流水线来进行加载。
```
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
        if (dispatchers.isNotEmpty()) {
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
```

###### RequestDispatcher
在执行加载图片的时候是需要开启一个死循环，由于是阻塞式的所以不糊造成界面的卡顿，无法理解的可以想想Handler的死循环读取message，异曲同工。
在这里我们进行一些对于图片加加载，从内存、磁盘、网络中读取图片，分别进行不同的操作来存放bitma
我们在BitmapRequest中也放置了一些参数，在这里都可以用上
！！！这里是继承Thread的，所以更新ui的操作都需要转到主线程中，这里采用handler来进行线程调度
```
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
                //给图片设置占位图
                showPlaceHolder(bitmapRequest)
                //1、获取bitmap 先从缓存中获取 没有再从网络获取
                val bitmap: Bitmap? = getBitmap(bitmapRequest)
                //2、通过Handler到ui线程展示图片
                showImageInUiThread(bitmapRequest, bitmap)
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
                    //bitmap为空 说明加载失败 显示错误图片
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
```
#### 生命周期
那么到这里图片的加载和缓存就已经完成了，接下来就是重点的生命周期的绑定，其实生命周期的绑定很简单，就往简单了想，不要去想的复杂，思路：
1.通过传入的上下文创建一个Fragment
2.先自定义一个HashMap<Int, ArrayList<String>>，内部参数<Activity的HashCode,ArrayList<内存缓存中bitmap的key，就是地址的MD5>>
获取到BitmapRequest的上下文activity，获取activity的HashCode
获取通过地址得到三级缓存中提供的bitmap之后，将key（BitmapRequest的uriMd5）放入上方HashMap的HashCode对应的列表中
3.在创建的Fragment被销毁的时候，移除内存中的bitmap并且回收，调用gc

###### LifeCycleObservable
```
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
```


###### RequestDispatcher的run()中，添加
```
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
```

###### RequestManagerFragment 销毁

```

class RequestManagerFragment : Fragment() {

    private val activityCode: Int by lazy { activity!!.hashCode() }

    override fun onDetach() {
        super.onDetach()
        LifeCycleObservable.instance.remove(activityCode)
    }
}
```
#### 三、内存变化效果图
首先是进入首页，添加存储权限，点击跳转到另一个界面，点击加载30多张图片，不进行复用，内存会暴涨到300M，然后点击退出，将当前界面绑定的bitmap全部回收，并且调用gc，内存恢复到初始状态


![内存变化.GIF](https://upload-images.jianshu.io/upload_images/4356451-0a14635b3677fc76.GIF?imageMogr2/auto-orient/strip)

#### 四、源码地址
[🔥 自定义Glide来理解Glide设计模式](https://github.com/zmemo/Glide)



