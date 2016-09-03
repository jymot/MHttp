package im.wangchao.mhttpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.IOException;

import im.wangchao.mhttp.Request;
import im.wangchao.mhttp.Response;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "wcwcwc";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set Cache Dir
//        MHttp.instance().cache(this, "tempcache");
//        MHttp.instance().replace(MHttp.instance().newBuilder().addNetworkInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                log("addNetworkInterceptor!");
//                Request request = chain.request();
//                long t1 = System.nanoTime();
//                log(String.format("Sending request %s on %s%n%s",
//                        request.url(), chain.connection(), request.headers()));
//
//                Response response = chain.proceed(request);
//
//                long t2 = System.nanoTime();
//                log(String.format("Received response for %s in %.1fms%n%s",
//                        response.request().url(), (t2 - t1) / 1e6d, response.headers()));
//
//                return response.newBuilder()
//                        .header("Cache-Control", "max-age=60")
//                        .removeHeader("Pragma")
//                        .build();
//            }
//        }).addInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request request = chain.request();
//
//                log("addInterceptor!");
//                Response response = chain.proceed(request);
//
//                return response.newBuilder()
//                        .header("Cache-Control", "max-age=60")
//                        .removeHeader("Pragma")
//                        .build();
//            }
//        }).build());

        //Get
        GetExample.doNormalRequest();
        GetExample.doAnnotationRequest();

        //Post
//        PostExample.doNormalPost();
//        PostExample.executeAnnotationPost();
//        MRequest request = PostExample.getRequest();
//        request.send();
    }
    Request request;
    private void log(String msg){
        Log.e(TAG, msg);
    }
}
