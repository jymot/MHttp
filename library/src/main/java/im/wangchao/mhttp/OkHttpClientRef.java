package im.wangchao.mhttp;

import android.content.Context;
import android.os.StatFs;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;

/**
 * <p>Description  : OkHttpClientRef.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/31.</p>
 * <p>Time         : 上午9:45.</p>
 */
public class OkHttpClientRef extends OkHttpClient {
    private static volatile OkHttpClientRef instance;

    public static OkHttpClientRef getInstance(){
        if (instance == null){
            synchronized (OkHttpClientRef.class){
                if (instance == null){
                    instance = new OkHttpClientRef();
                }
            }
        }
        return instance;
    }

    /**
     * 为okHttpClient设置缓存
     * @param context   上下文,用来确定包名,以及包路径
     * @param client    okHttpClient实例
     * @param dirName   缓存默认在包的cache目录,只需要传递文件夹名称即可
     */
    public static void cache(Context context, OkHttpClient client, String dirName) {
        File cache = new File(context.getApplicationContext().getCacheDir(), dirName);
        if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
        }
        long size = 5 * 1024 * 1024;
        try {
            StatFs statFs = new StatFs(cache.getAbsolutePath());
            long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
        }
        // Bound inside min/max size for disk cache.
        size = Math.max(Math.min(size, size * 10), size);
        client.setCache(new Cache(cache, size));
    }

    /**
     * 取消请求，不触发 cancel 回调
     * @param tag Tag
     */
    public void silentCancel(Object tag){
        cancel(tag);
    }

}
