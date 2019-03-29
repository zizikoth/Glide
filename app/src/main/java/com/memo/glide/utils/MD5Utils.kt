package com.memo.glide.utils

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-25 17:02
 */
object MD5Utils {

    fun toMD5(plainText: String): String {
        var secretBytes: ByteArray? = null
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(plainText.toByteArray())
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("没有md5这个算法！")
        }

        val md5code = StringBuilder(BigInteger(1, secretBytes).toString(16))
        // 如果生成数字未满32位，需要前面补0
        for (i in 0 until 32 - md5code.length) {
            md5code.insert(0, "0")
        }
        return md5code.toString()
    }
}
