package im.wangchao.mhttpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;

import im.wangchao.mhttp.FileResponseHandler;
import im.wangchao.mhttp.HttpManager;
import im.wangchao.mhttp.HttpRequest;
import im.wangchao.mhttp.HttpResponse;
import im.wangchao.mhttp.TextResponseHandler;
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

        HttpManager.instance().cache(this, "tempcache");
        HttpManager.instance().addNetworkInterceptor(new Interceptor() {
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
        });
        HttpManager.instance().addInterceptor(new Interceptor() {
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
        });
        findViewById(R.id.getFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               HttpRequest.builder()
                        .get()
                        .url("http://www.cninfo.com.cn/finalpage/2014-12-13/1200461869.PDF")
                        .responseHandler(new FileResponseHandler(MainActivity.this) {
                            @Override
                            public void onSuccess(File file, HttpResponse response) {
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
                        }).build().execute();
            }
        });


        findViewById(R.id.getBaiduText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SampleApi.instance().baidu(new TextResponseHandler() {
                    @Override
                    public void onSuccess(String text, HttpResponse response) {
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
