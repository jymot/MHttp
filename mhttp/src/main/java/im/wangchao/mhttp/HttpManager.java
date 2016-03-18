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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import im.wangchao.http.BindApi;
import im.wangchao.mhttp.internal.cookie.MemeryCookieJar;
import im.wangchao.mhttp.internal.cookie.cache.SetCookieCache;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import timber.log.Timber;

/**
 * <p>Description  : OkHttpClientManager.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/3/8.</p>
 * <p>Time         : 下午1:29.</p>
 */
public final class HttpManager {
    private final Builder okBuilder;
    private OkHttpClient okHttpClient;
    private boolean modified;
    private static volatile HttpManager instance;

    private final ConcurrentHashMap<Object, List<HttpRequest>> requestList = new ConcurrentHashMap<>();

    private HttpManager(){
        this(new OkHttpClient());
    }

    private HttpManager(OkHttpClient okHttpClient){
        okBuilder = okHttpClient.newBuilder();
        //default Memery Cookie
        okBuilder.cookieJar(new MemeryCookieJar(new SetCookieCache()));
    }

    public static HttpManager replace(@NonNull OkHttpClient okHttpClient) {
        synchronized (HttpManager.class) {
            instance = new HttpManager(okHttpClient);
        }
        return instance;
    }

    public static HttpManager instance(){
        if (instance == null){
            synchronized (HttpManager.class){
                if (instance == null){
                    instance = new HttpManager();
                }
            }
        }
        return instance;
    }

    private void modified(){
        modified = true;
    }

    public static <T> T bind(Class<T> api){
        return BindApi.bind(api);
    }

    public OkHttpClient okHttpClient(){
        if (okHttpClient == null || modified){
            modified = false;
            okHttpClient = okBuilder.build();
            return okHttpClient;
        }

        return okHttpClient;
    }

    public HttpManager cookieJar(CookieJar cookieJar){
        modified();
        okBuilder.cookieJar(cookieJar);
        return this;
    }

    public HttpManager timeout(int timeout){
        connectTimeout(timeout, TimeUnit.SECONDS);
        readTimeout(timeout, TimeUnit.SECONDS);
        writeTimeout(timeout, TimeUnit.SECONDS);
        return this;
    }

    public HttpManager connectTimeout(long timeout, TimeUnit unit){
        modified();
        okBuilder.connectTimeout(timeout, unit);
        return this;
    }

    public HttpManager readTimeout(long timeout, TimeUnit unit){
        modified();
        okBuilder.connectTimeout(timeout, unit);
        return this;
    }

    public HttpManager writeTimeout(long timeout, TimeUnit unit){
        modified();
        okBuilder.connectTimeout(timeout, unit);
        return this;
    }

    public HttpManager cancel(@NonNull Object tag){
        if (!requestList.containsKey(tag)){
            return this;
        }

        final List<HttpRequest> list = requestList.get(tag);

        if (list == null){
            return this;
        }

        for (HttpRequest request: list){
            request.cancel();
        }

        return this;
    }

    public HttpManager enqueue(HttpRequest ...requestArray){
        if (requestArray == null || requestArray.length == 0){
            return this;
        }

        for (HttpRequest httpRequest: requestArray){
            Call call = enqueue(httpRequest);
            httpRequest.responseHandler.setRequest(httpRequest);
            httpRequest.responseHandler.sendStartMessage();
            call.enqueue(httpRequest.responseHandler);
        }

        return this;
    }

    HttpManager dequeue(@NonNull HttpRequest request){
        if (!requestList.containsKey(request.tag)){
            return this;
        }
        final List<HttpRequest> list = requestList.get(request.tag);
        if (list == null){
            return this;
        }

        list.remove(request);
        if (list.size() == 0){
            requestList.remove(request.tag);
        } else {
            requestList.replace(request.tag, list);
        }

        return this;
    }

    private Call enqueue(HttpRequest httpRequest){
        final Object tag = httpRequest.tag;
        if (requestList.containsKey(tag)){
            List<HttpRequest> list = requestList.get(tag);
            list.add(httpRequest);
            requestList.replace(tag, list);
        } else {
            List<HttpRequest> list = new ArrayList<>();
            list.add(httpRequest);
            requestList.put(tag, list);
        }
        return okHttpClient().newCall(httpRequest.map());
    }

    public HttpManager cache(Context context, String dirName) {
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
        modified();
        okBuilder.cache(new Cache(cache, size));
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
            modified();
            okBuilder.sslSocketFactory(sslContext.getSocketFactory());
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


        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException ce) {
                localTrustManager.checkServerTrusted(chain, authType);
            }
        }


        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }



}
