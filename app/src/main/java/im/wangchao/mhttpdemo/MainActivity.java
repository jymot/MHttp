package im.wangchao.mhttpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.File;

import im.wangchao.mhttp.FileResponseHandler;
import im.wangchao.mhttp.HttpRequest;
import im.wangchao.mhttp.HttpResponse;
import im.wangchao.mhttp.TextResponseHandler;

public class MainActivity extends AppCompatActivity {
    final static String TAG = MainActivity.class.getSimpleName();
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.getFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HttpRequest.Builder()
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
                                Log.e("wcwcwc", "onFinish");
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
                SampleApi api = SampleApi.instance();
                if (api == null){
                    log("api == null");
                    return;
                }
                api.baidu(new TextResponseHandler(){
                    @Override public void onSuccess(String text, HttpResponse response) {
                        super.onSuccess(text, response);
                        log("onSuccess: " + text);
                    }

                    @Override protected void onFinish() {
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
