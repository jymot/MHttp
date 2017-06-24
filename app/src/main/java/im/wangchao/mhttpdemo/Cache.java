package im.wangchao.mhttpdemo;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import im.wangchao.mhttp.MHttp;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <p>Description  : Cache.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 2017/6/23.</p>
 * <p>Time         : 下午1:43.</p>
 */
public class Cache {
    final static String TAG = "wcwcwc";

    public static void setCacheDir(Context context){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //Set Cache Dir
        MHttp.cache(context, builder, "tempcache");
        OkHttpClient client = builder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                log("addNetworkInterceptor!");
                Request request = chain.request();
                long t1 = System.nanoTime();
                log(String.format("Sending request %s on %s%n%s",
                        request.url(), chain.connection(), request.headers()));

                Response response = chain.proceed(request);

                long t2 = System.nanoTime();
                log(String.format("Received response for %s in %.1fms%n%s",
                        response.request().url(), (t2 - t1) / 1e6d, response.headers()));

                return response.newBuilder()
                        .header("Cache-Control", "max-age=60")
                        .removeHeader("Pragma")
                        .build();
            }
        }).addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                log("addInterceptor!");
                Response response = chain.proceed(request);

                return response.newBuilder()
                        .header("Cache-Control", "max-age=60")
                        .removeHeader("Pragma")
                        .build();
            }
        }).build();

        MHttp.instance().client(client);
    }

    private static void log(String msg){
        Log.e(TAG, msg);
    }
}
