package com.memo.glide.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * title:DiskLruCache管理
 * describe:
 *
 * @author zhou
 * @date 2019-03-25 17:35
 */

public class DiskLruCacheManager {

    private static int maxSize = 20 * 1024 * 1024;
    private DiskLruCache mDiskLruCache;
    private final static String defaultName = "default";

    public DiskLruCacheManager(Context context) {
        this(context, defaultName, maxSize);
    }

    public DiskLruCacheManager(Context context, int maxDiskLruCacheSize) {
        this(context, defaultName, maxDiskLruCacheSize);
    }

    public DiskLruCacheManager(Context context, String dirName) {
        this(context, dirName, maxSize);
    }

    public DiskLruCacheManager(Context context, String dirName, int maxDiskLruCacheSize) {
        try {
            mDiskLruCache = DiskLruCache.open(getDiskCacheFile(context, dirName), getAppVersion(context), 1, maxDiskLruCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件夹地址，如果不存在，则创建
     *
     * @param context 上下文
     * @param dirName 文件名
     * @return File 文件
     */
    private File getDiskCacheFile(Context context, String dirName) {
        File cacheDir = packDiskCacheFile(context, dirName);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    /**
     * 获取文件夹地址
     *
     * @param context 上下文
     * @param dirName 文件名
     * @return File 文件
     */
    private File packDiskCacheFile(Context context, String dirName) {
        String cachePath;
        if (context.getExternalCacheDir() != null) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + dirName);
    }

    /**
     * 获取当前应用程序的版本号。
     */
    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 使用MD5算法对传入的key进行加密并返回。
     */
    private String Md5(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Bitmap格式数据写入到outputstream中
     *
     * @param bm   Bitmap数据
     * @param baos outputstream
     * @return outputstream
     */
    private OutputStream Bitmap2OutputStream(Bitmap bm, OutputStream baos) {
        if (bm != null) {
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        }
        return baos;
    }

    /**
     * 将缓存记录同步到journal文件中。
     */
    public void fluchCache() {
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取硬盘缓存
     *
     * @param key 所有
     * @return Bitmap格式缓存
     */
    public Bitmap getDiskCache(String key) {
        String md5Key = Md5(key);
        Bitmap bitmap = null;
        try {
            if (mDiskLruCache != null) {
                DiskLruCache.Snapshot snapshot = mDiskLruCache.get(md5Key);
                if (snapshot != null) {
                    bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 设置key对应的缓存
     *
     * @param key    索引
     * @param bitmap Bitmap格式数据
     * @return 是否写入
     */
    public boolean putDiskCache(String key, Bitmap bitmap) {
        String md5Key = Md5(key);
        try {
            if (mDiskLruCache != null) {
                if (mDiskLruCache.get(md5Key) != null) {
                    return true;
                }
                DiskLruCache.Editor editor = mDiskLruCache.edit(md5Key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    Bitmap2OutputStream(bitmap, outputStream);
                    editor.commit();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteDiskCache() {
        try {
            if (mDiskLruCache != null) {
                mDiskLruCache.delete();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void removeDiskCache(String key) {
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.remove(key);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void deleteFile(Context context, String dirName) {
        try {
            DiskLruCache.deleteContents(packDiskCacheFile(context, dirName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int size() {
        int size = 0;
        if (mDiskLruCache != null) {
            size = (int) mDiskLruCache.size();
        }
        return size;
    }

    public void close() {
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}