package im.wangchao.mhttp;

import android.content.Context;
import android.os.Build;
import android.os.StatFs;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import im.wangchao.mhttp.internal.MBridgeInterceptor;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import static im.wangchao.mhttp.HTTPS.chooseTrustManager;
import static im.wangchao.mhttp.HTTPS.prepareKeyManager;
import static im.wangchao.mhttp.HTTPS.prepareTrustManager;

/**
 * <p>Description  : MHttp.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/2.</p>
 * <p>Time         : 上午8:40.</p>
 */
public final class MHttp {
    private static volatile MHttp instance;

    private OkHttpClient mOkHttpClient;
    private URLInterceptor mURLInterceptor;

    public static MHttp instance(){
        if (instance == null) {
            synchronized (MHttp.class){
                instance = new MHttp();
            }
        }
        return instance;
    }

    /**
     * Create annotation api
     */
    public static <T> T create(Class<T> api){
        return BindApi.bind(api);
    }

    /**
     * Set cache Dir
     */
    public static void cache(Context context, OkHttpClient.Builder builder, String dirName) {
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

        builder.cache(new Cache(cache, size));
    }

    /**
     * Trust all certificate for debug
     */
    public static void trustAllCertificate(OkHttpClient.Builder builder){
        // 自定义一个信任所有证书的TrustManager，添加SSLSocketFactory的时候要用到
        final X509TrustManager trustAllCert =
                new X509TrustManager() {
                    @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                };
        builder.sslSocketFactory(new Android5SSL(trustAllCert), trustAllCert);
    }

    /**
     * Set Certificate
     */
    public static void setCertificates(OkHttpClient.Builder builder, InputStream... certificates) throws Exception{
        setCertificates(builder, certificates, null, null);
    }

    /**
     * Set Certificate
     */
    public static void setCertificates(OkHttpClient.Builder builder, InputStream[] certificates, InputStream bksFile, String password) throws Exception {
        TrustManager[] trustManagers = prepareTrustManager(certificates);
        KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
        SSLContext sslContext = SSLContext.getInstance("TLS");

        HTTPS.MyTrustManager trustManager = new HTTPS.MyTrustManager(chooseTrustManager(trustManagers));
        sslContext.init(keyManagers, new TrustManager[]{trustManager}, new SecureRandom());
        builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
    }

    /**
     * OkHttpClient
     */
    public MHttp client(OkHttpClient client){
        OkHttpClient.Builder builder = client.newBuilder();
        if (!builder.interceptors().contains(MBridgeInterceptor.instance.get())){
            builder.addInterceptor(MBridgeInterceptor.instance.get());
        }
        mOkHttpClient = builder.build();
        return this;
    }

    /**
     * @return Current client.
     */
    public OkHttpClient client(){
        if (mOkHttpClient == null){
            throw new IllegalArgumentException("OkHttpClient cannot be null, please call the MHttp#client(OkHttpClient client) method first.");
        }
        return mOkHttpClient;
    }

    /**
     * Set connect, read and write time with {@link TimeUnit#SECONDS}
     */
    public MHttp timeout(int timeout){
        timeout(timeout, TimeUnit.SECONDS);
        return this;
    }

    public MHttp timeout(int timeout, TimeUnit unit){
        connectTimeout(timeout, unit);
        readTimeout(timeout, unit);
        writeTimeout(timeout, unit);
        return this;
    }

    public MHttp connectTimeout(long timeout, TimeUnit unit){
        mOkHttpClient = mOkHttpClient.newBuilder().connectTimeout(timeout, unit).build();
        return this;
    }

    public MHttp readTimeout(long timeout, TimeUnit unit){
        mOkHttpClient = mOkHttpClient.newBuilder().readTimeout(timeout, unit).build();
        return this;
    }

    public MHttp writeTimeout(long timeout, TimeUnit unit){
        mOkHttpClient = mOkHttpClient.newBuilder().writeTimeout(timeout, unit).build();
        return this;
    }

    public MHttp setURLInterceptor(URLInterceptor interceptor){
        this.mURLInterceptor = interceptor;
        return this;
    }

    /**
     * Cancel all request.
     */
    public MHttp cancelAll(){
        mOkHttpClient.dispatcher().cancelAll();
        return this;
    }

    /**
     * Cancel request with {@code tag}
     */
    public MHttp cancel(Object tag){
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        return this;
    }

    /*package*/ String proceedURL(String url){
        if (mURLInterceptor != null){
            return mURLInterceptor.interceptor(url);
        }
        return url;
    }

    /*package*/ HttpUrl proceedURL(HttpUrl url){
        if (mURLInterceptor != null){
            return mURLInterceptor.interceptor(url);
        }
        return url;
    }

    /*package*/ URL proceedURL(URL url){
        if (mURLInterceptor != null){
            return mURLInterceptor.interceptor(url);
        }
        return url;
    }

    //@private
    private MHttp(){
        //default instance
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(MBridgeInterceptor.instance.get())
                .build();
    }

}
