package im.wangchao.mhttpdemo;

import android.util.Log;

import im.wangchao.http.annotations.Callback;
import im.wangchao.http.annotations.Get;
import im.wangchao.mhttp.MHttp;
import im.wangchao.mhttp.Request;
import im.wangchao.mhttp.OkResponse;
import im.wangchao.mhttp.TextCallbackHandler;

/**
 * <p>Description  : Get.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/6.</p>
 * <p>Time         : 上午8:38.</p>
 */
public class GetExample {

    public static Request doNormalRequest(){
        return Request.builder().url("http://wangchao.im")
                .callback(new TextCallbackHandler(){
                    @Override protected void onSuccess(String data, OkResponse response) {
                        Log.e(MainActivity.TAG, "normal : " + data);
                    }

                    @Override
                    protected void onCancel() {
                        Log.e(MainActivity.TAG, "onCancel");
                    }
                })
                .build()
                .send();
    }

    public static void doAnnotationRequest(){
        GetBaidu baidu = MHttp.create(GetBaidu.class);
        baidu.baidu(new TextCallbackHandler(){
            @Override protected void onSuccess(String data, OkResponse response) {
                Log.e(MainActivity.TAG, data);
            }
        });
    }

    public interface GetBaidu{
        @Get(url = "http://wangchao.im")
        void baidu(@Callback TextCallbackHandler callback);
    }
}
