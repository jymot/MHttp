package im.wangchao.mhttp;

import android.content.Context;
import android.os.Build;
import android.os.StatFs;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.CookieHandler;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

/**
 * <p>Description  : OkHttpClientRef.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/31.</p>
 * <p>Time         : 上午9:45.</p>
 */
/*package*/ class OkHttpClientRef extends OkHttpClient implements IHttpClient<OkHttpClient> {
    public OkHttpClientRef(){}

    private WeakReference<IHttpCall> call;

    @Override public void setTimeoutHook(long timeout, TimeUnit timeUnit) {
        setConnectTimeout(timeout, TimeUnit.SECONDS);
        setWriteTimeout(timeout, TimeUnit.SECONDS);
        setReadTimeout(timeout, TimeUnit.SECONDS);
    }

    @Override public void cancelHook(Object tag) {
        cancel(tag);
    }

    @Override public IHttpCall newCall(HttpRequest request) {
        OkHttpClientCallImpl okHttpClientCall = new OkHttpClientCallImpl(request, this);
        call = new WeakReference<IHttpCall>(okHttpClientCall);
        return okHttpClientCall;
    }

    @Override public void setCookieHandlerHook(CookieHandler cookieHandler) {
        setCookieHandler(cookieHandler);
    }

    /**
     * 为okHttpClient设置缓存
     * @param context   上下文,用来确定包名,以及包路径
     * @param dirName   缓存默认在包的cache目录,只需要传递文件夹名称即可
     */
    @Override public void cache(Context context, String dirName) {
        File cache = new File(context.getApplicationContext().getCacheDir(), dirName);
        if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
        }
        long size = 5 * 1024 * 1024;
        try {
            StatFs statFs = new StatFs(cache.getAbsolutePath());
            long count, blockSize;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1){
                count = statFs.getBlockCountLong();
                blockSize = statFs.getBlockSizeLong();
            } else {
                count = statFs.getBlockCount();
                blockSize = statFs.getBlockSize();
            }
            long available = count * blockSize;
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
        }
        // Bound inside min/max size for disk cache.
        size = Math.max(Math.min(size, size * 10), size);
        setCache(new Cache(cache, size));
    }

    @Override public void setSslSocketFactoryHook(SocketFactory factory) {
        setSocketFactory(factory);
    }

    @Override public OkHttpClientRef getHttpClient() {
        return this;
    }


}
