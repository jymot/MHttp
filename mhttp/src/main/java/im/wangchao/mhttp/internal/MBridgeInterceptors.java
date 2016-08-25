package im.wangchao.mhttp.internal;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <p>Description  : MBridgeInterceptors.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/8/24.</p>
 * <p>Time         : 下午4:32.</p>
 */
public final class MBridgeInterceptors implements Interceptor {
    private MBridgeInterceptors(){}

    public static Singleton<MBridgeInterceptors> instance = new Singleton<MBridgeInterceptors>() {
        @Override protected MBridgeInterceptors create() {
            return new MBridgeInterceptors();
        }
    };

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();

        if (request.header("User-Agent") == null) {
            builder.header("User-Agent", Version.userAgent());
        }

        return chain.proceed(builder.build());
    }
}
