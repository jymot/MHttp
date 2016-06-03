package im.wangchao.mhttpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;

import im.wangchao.mhttp.FileCallbackHandler;
import im.wangchao.mhttp.MHttp;
import im.wangchao.mhttp.MRequest;
import im.wangchao.mhttp.OkResponse;
import im.wangchao.mhttp.TextCallbackHandler;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    final static String TAG = MainActivity.class.getSimpleName();
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MHttp.instance().cache(this, "tempcache");
        MHttp.instance().replace(MHttp.instance().newBuilder().addNetworkInterceptor(new Interceptor() {
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
        }).build());

        findViewById(R.id.getFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               MRequest.builder()
                        .get()
                        .url("http://www.cninfo.com.cn/finalpage/2014-12-13/1200461869.PDF")
                        .callback(new FileCallbackHandler(MainActivity.this) {
                            @Override
                            public void onSuccess(File file, OkResponse response) {
                                super.onSuccess(file, response);
                                log("file len: " + file.length() + ", file exists: " + file.exists() + " , path:" + file.getPath());
                            }

                            @Override
                            protected void onFinish() {
                                super.onFinish();
                                log("onFinish");
                            }

                            @Override
                            protected void onProgress(int bytesWritten, int bytesTotal) {
                                super.onProgress(bytesWritten, bytesTotal);
                                log("onProgress : " + bytesWritten + " -- " + bytesTotal);
                            }
                        }).build().send();
            }
        });


        findViewById(R.id.getBaiduText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SampleApi.instance().baidu(new TextCallbackHandler() {
                    @Override
                    public void onSuccess(String text, OkResponse response) {
                        super.onSuccess(text, response);
                        log("onSuccess: " + text);
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                        log("onFinish");
                    }
                });
            }
        });

    }

    private void log(String msg){
        Log.e(TAG, msg);
    }
}
