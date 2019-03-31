package com.memo.glide.utils

import android.util.Log
import android.widget.Toast
import com.memo.glide.application.App

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-26 09:40
 */

fun log(content: Any?) {
    Log.i("Glide", content?.toString() ?: "数据为null")
}

fun toast(content: Any?) {
    Toast.makeText(App.appContext, content?.toString() ?: "数据为null", Toast.LENGTH_SHORT).show()
}

val imageUrls: Array<String> =
    arrayOf(
        "这是一张错误的图片",
        "https://i0.hdslb.com/bfs/article/c0b06ce5df8e8c98e1d8d0a095945fc56d94d2df.jpg@1320w_750h.webp",
        "https://i0.hdslb.com/bfs/article/7cbba016d14ab87bea42113d85fb3f40935f481a.jpg@1320w_854h.webp",
        "https://i0.hdslb.com/bfs/article/aa92755edb9f11f01b22ee4aa2fbc251467a6241.jpg@1320w_738h.webp",
        "https://i0.hdslb.com/bfs/article/972e87ca02f1c31f887ff74b1a6e09f7cd526871.jpg@1320w_808h.webp",
        "https://i0.hdslb.com/bfs/article/dc05948fe4346a1f72f3cc95afd2a77c5f56c073.jpg@1320w_844h.webp",
        "https://i0.hdslb.com/bfs/article/8191c5a9cec859a749bc2b61746e91dfc22cbb19.jpg@1320w_892h.webp",
        "https://i0.hdslb.com/bfs/article/6f82bd12a3a5ed170b93841a968167af8cc23569.jpg@1320w_880h.webp",
        "https://i0.hdslb.com/bfs/article/3a1b352bcc2b38388523ed6411dd597cdfd87a83.jpg@1320w_770h.webp",
        "https://i0.hdslb.com/bfs/article/4e5229239610a1eda9add3cb5109e7e1b7c86a50.jpg@1320w_880h.webp",
        "https://i0.hdslb.com/bfs/article/ff14ea325b2aa30451bec59d8f287f2763159aff.jpg@1320w_990h.webp",
        "https://i0.hdslb.com/bfs/article/54dfa0a1a4810e78cd0910b46c6e82f0882d1180.jpg@1320w_770h.webp",
        "https://i0.hdslb.com/bfs/article/bbd895e4a0afcad3d4984897f48179ab58593667.jpg@1320w_642h.webp",
        "https://i0.hdslb.com/bfs/article/86908f9efd2107c9368ff6d9c701a8d60367b7fb.jpg@1320w_766h.webp",
        "https://i0.hdslb.com/bfs/article/6533fa0f2e82fb9ec52367fd226a6d31148ef37d.jpg@1320w_676h.webp",
        "https://i0.hdslb.com/bfs/article/a70146e0a6f4fcde7365796037412b0da4ac907f.jpg@1320w_894h.webp",
        "https://i0.hdslb.com/bfs/article/b8ece002a8503d17677fdff8f77b004ddc7d6687.jpg@1320w_742h.webp",
        "https://i0.hdslb.com/bfs/article/6e76e4edf9df4482401b4addc29460fd5859e380.jpg@1320w_990h.webp",
        "https://i0.hdslb.com/bfs/article/7bd8164395a546f86f711cd0c36182484b1ee6d6.jpg@1320w_786h.webp",
        "https://i0.hdslb.com/bfs/article/99195aefc0013acdda4dad0cd8f2c9624b51b4d8.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/fba8e8475ea7ad99389db46e9a8999f60f3ed966.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/58100d9da51e99995cd76de0a725e7cd81181879.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/17f4ba03f0ede3ddb50238e3c5f948d444aac136.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/c4707dcb031d2ee9710263741d67f93a0ea19b72.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/7e9108870eca845c028e57e81a3300ce5129e1de.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/541d591263dc78c660437cd95fc5be9e1895b0ee.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/9192dd9f5b1d4ca3f75cecbe3ac286d8ebbaceca.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/726141cf8ff112021dbca82e5b1744e15f162643.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/54a619b5b0e360b3c75ef1a0ba857ed59e1782bd.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/c9f93c62ee52199bf19483df56aa8bbcd0af555d.jpg@1466w_824h.webp",
        "https://i0.hdslb.com/bfs/article/d3fd682648ed28a9279c38d690650483127b1c9e.jpg@1466w_824h.webp"
    )