package im.wangchao.mhttp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
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

import timber.log.Timber;

/**
 * <p>Description  : HttpClientManager.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/11/27.</p>
 * <p>Time         : 下午5:25.</p>
 */
public class HttpClientManager {
    private static volatile HttpClientManager instance;

    private HttpClientManager() {
        //default
        httpClient = new OkHttpClientRef();
        setTimeout(30, TimeUnit.SECONDS);
        setCookieHandlerHook(Cookie.instanceManager());
    }

    private IHttpClient httpClient;
    private ResponseHandlerHook responseHandlerHook;

    /**
     * 单例模式，默认为单例模式
     */
    public static HttpClientManager getSingleInstance() {
        if (instance == null) {
            synchronized (HttpClientManager.class) {
                if (instance == null) {
                    instance = createInstance();
                }
            }
        }

        return instance;
    }

    /**
     * 创建一个新的实例,
     */
    public static HttpClientManager createInstance() {
        return new HttpClientManager();
    }

    /**
     * 设置 ResponseHandlerHook
     */
    public void setResponseHandlerHook(ResponseHandlerHook hook){
        this.responseHandlerHook = hook;
    }

    /*package*/ @Nullable ResponseHandlerHook getResponseHandlerHook(){
        return this.responseHandlerHook;
    }

    /**
     * 设置默认的 IHttpClient
     *
     * @param httpClient    IHttpClient
     */
    public void setHttpClient(@NonNull IHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 创建一个 Http 指令
     *
     * @param request   HttpRequest
     * @return          IHttpCall
     */
    public IHttpCall newCall(HttpRequest request) {
        return httpClient.newCall(request);
    }

    /**
     * 设置请求超时时间
     *
     * @param timeout   超时时间
     * @param timeUnit  时间单位
     */
    public void setTimeout(long timeout, TimeUnit timeUnit) {
        httpClient.setTimeoutHook(timeout, timeUnit);
    }

    /**
     * 取消所有 Tag 为 tag 的请求，不触发 cancel 回调
     *
     * @param tag   Tag
     */
    public void cancel(Object tag) {
        httpClient.cancelHook(tag);
    }

    /**
     * 设置 CookieHandler
     *
     * @param cookieHandler CookieHandler
     */
    public void setCookieHandlerHook(CookieHandler cookieHandler) {
        httpClient.setCookieHandlerHook(cookieHandler);
    }

    /**
     * 设置缓存目录
     *
     * @param context   Context
     * @param dirName   缓存目录名称，相对于/data/data/package/caches/
     */
    public void setCache(Context context, String dirName) {
        httpClient.cache(context, dirName);
    }

    /**
     * Set Certificate
     */
    public void setCertificates(InputStream... certificates) {
        setCertificates(certificates, null, null);
    }

    public void setCertificates(InputStream[] certificates, InputStream bksFile, String password) {
        try {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(keyManagers, new TrustManager[]{new MyTrustManager(chooseTrustManager(trustManagers))}, new SecureRandom());
            httpClient.setSslSocketFactoryHook(sslContext.getSocketFactory());
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