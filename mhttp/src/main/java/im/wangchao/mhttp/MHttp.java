package im.wangchao.mhttp;

import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import im.wangchao.mhttp.internal.MBridgeInterceptors;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * <p>Description  : MHttp.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/2.</p>
 * <p>Time         : 上午8:40.</p>
 */
public final class MHttp {
    private static volatile MHttp instance;

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


    private OkHttpClient mOkHttpClient;
    private MBridgeInterceptors mBridgeInterceptors = new MBridgeInterceptors();

    public MHttp replace(@NonNull OkHttpClient replace){
        return build(replace.newBuilder());
    }

    public OkHttpClient okHttpClient(){
        return mOkHttpClient;
    }

    public OkHttpClient.Builder newBuilder() {
        return mOkHttpClient.newBuilder();
    }

    public MHttp build(OkHttpClient.Builder builder){
        if (!builder.interceptors().contains(mBridgeInterceptors)){
            builder.addInterceptor(mBridgeInterceptors);
        }
        mOkHttpClient = builder.build();
        return this;
    }

    /**
     * Set connect, read and write time with {@link TimeUnit#SECONDS}
     */
    public MHttp timeout(int timeout){
        connectTimeout(timeout, TimeUnit.SECONDS);
        readTimeout(timeout, TimeUnit.SECONDS);
        writeTimeout(timeout, TimeUnit.SECONDS);
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

    /**
     * Set {@link CookieJar}
     */
    public MHttp cookieJar(CookieJar cookieJar){
        mOkHttpClient = mOkHttpClient.newBuilder().cookieJar(cookieJar).build();
        return this;
    }

    /**
     * @return {@link CookieJar}
     */
    public CookieJar cookieJar(){
        return mOkHttpClient.cookieJar();
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

    /**
     * Async request
     */
    public MHttp enqueue(OkRequest request){
        timeout(request.timeout());

        Call call = mOkHttpClient.newCall(request.request());
        OkCallback callback = request.callback();
        callback.initialize();
        callback.setRequest(request);
        callback.sendStartMessage();
        call.enqueue(callback);
        return this;
    }

    /**
     * Set cache Dir
     */
    public MHttp cache(Context context, String dirName) {
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

        mOkHttpClient = mOkHttpClient.newBuilder().cache(new Cache(cache, size)).build();
        return this;
    }

    /**
     * Set Certificate
     */
    public void setCertificates(InputStream... certificates) {
        setCertificates(certificates, null, null);
    }

    /**
     * Set Certificate
     */
    public void setCertificates(InputStream[] certificates, InputStream bksFile, String password) {
        try {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(keyManagers, new TrustManager[]{new MyTrustManager(chooseTrustManager(trustManagers))}, new SecureRandom());
            mOkHttpClient = mOkHttpClient.newBuilder()
                    .sslSocketFactory(sslContext.getSocketFactory()).build();
        } catch (Exception ignore) {
            Timber.e(ignore, ignore.getMessage());
        }
    }

    private TrustManager[] prepareTrustManager(InputStream... certificates) {
        if (certificates == null || certificates.length <= 0) return null;
        try {

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException ignore) {
                    Timber.e(ignore, ignore.getMessage());
                }
            }
            TrustManagerFactory trustManagerFactory = null;

            trustManagerFactory = TrustManagerFactory.
                    getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            return trustManagerFactory.getTrustManagers();
        } catch (Exception ignore) {
            Timber.e(ignore, ignore.getMessage());
        }
        return null;

    }

    private KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
        try {
            if (bksFile == null || password == null) return null;

            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksFile, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();

        }  catch (Exception ignore) {
            Timber.e(ignore, ignore.getMessage());
        }
        return null;
    }

    private X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    /*                  X509TrustManager                  */
    private class MyTrustManager implements X509TrustManager {
        private X509TrustManager defaultTrustManager;
        private X509TrustManager localTrustManager;

        public MyTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
            TrustManagerFactory var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            var4.init((KeyStore) null);
            defaultTrustManager = chooseTrustManager(var4.getTrustManagers());
            this.localTrustManager = localTrustManager;
        }


        @Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            try {
//                defaultTrustManager.checkClientTrusted(chain, authType);
//            } catch (CertificateException e) {
//                localTrustManager.checkClientTrusted(chain, authType);
//            }
        }

        @Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                localTrustManager.checkServerTrusted(chain, authType);
            }
        }


        @Override public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    //@private
    private MHttp(){
        //default instance
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(mBridgeInterceptors)
                .build();
    }

}
