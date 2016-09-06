package im.wangchao.mhttp;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.internal.Util;

/**
 * <p>Description  : HTTPS.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/9/2.</p>
 * <p>Time         : 下午3:58.</p>
 */
public class HTTPS {

    /**
     * Set Certificate
     */
    public static void setCertificates(OkHttpClient.Builder builder, InputStream... certificates) throws Exception {
        setCertificates(builder, certificates, null, null);
    }

    /**
     * Set Certificate
     */
    public static void setCertificates(OkHttpClient.Builder builder, InputStream[] certificates, InputStream bksFile, String password) throws Exception {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(keyManagers, new TrustManager[]{new MyTrustManager(chooseTrustManager(trustManagers))}, new SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory()).build();
    }

    private static TrustManager[] prepareTrustManager(InputStream... certificates) throws Exception {
        if (certificates == null || certificates.length <= 0) return null;

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        int index = 0;
        for (InputStream certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
            Util.closeQuietly(certificate);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.
                getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static KeyManager[] prepareKeyManager(InputStream bksFile, String password) throws Exception{
        if (bksFile == null || password == null) return null;

        KeyStore clientKeyStore = KeyStore.getInstance("BKS");
        clientKeyStore.load(bksFile, password.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(clientKeyStore, password.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    /*                  X509TrustManager                  */
    private static class MyTrustManager implements X509TrustManager {
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
}
