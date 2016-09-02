package im.wangchao.mhttpdemo;

import im.wangchao.http.annotations.Callback;
import im.wangchao.http.annotations.Post;
import im.wangchao.mhttp.JSONCallbackHandler;
import im.wangchao.mhttp.MHttp;
import im.wangchao.mhttp.Request;
import im.wangchao.mhttp.OkResponse;
import im.wangchao.mhttp.TextCallbackHandler;

/**
 * <p>Description  : PostExample.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/6.</p>
 * <p>Time         : 上午8:49.</p>
 */
public class PostExample {

    public static void doNormalPost(){
        Request.builder().url("http://wangchao.im")
                .addHeader("key", "value")
                .addParameter("key", "value")
                .callback(new JSONCallbackHandler(){
                    @Override protected void onSuccess(JSON data, OkResponse response) {

                    }
                })
                .build()
                .send();
    }

    public static void executeAnnotationPost(){
        PostApi api = MHttp.create(PostApi.class);
        api.autoExecuteRequest("aa", "bb", new TextCallbackHandler(){
            @Override protected void onSuccess(String data, OkResponse response) {
                //Todo
            }
        });
    }

    public static Request getRequest(){
        PostApi api = MHttp.create(PostApi.class);
        return api.postRequest("aa", "bb", new TextCallbackHandler(){
            @Override protected void onSuccess(String data, OkResponse response) {
                //Todo
            }
        });
    }

    public interface PostApi{

        @Post(url = "http://wangchao.im")
        Request postRequest(String param0, String param1, @Callback TextCallbackHandler callback);

        @Post(url = "http://wangchao.im")
        void autoExecuteRequest(String param0, String param1, @Callback TextCallbackHandler callback);
    }
}
