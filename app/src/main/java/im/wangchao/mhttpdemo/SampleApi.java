package im.wangchao.mhttpdemo;

import im.wangchao.http.annotations.Callback;
import im.wangchao.http.annotations.Get;
import im.wangchao.mhttp.HttpManager;
import im.wangchao.mhttp.HttpRequest;
import im.wangchao.mhttp.TextResponseHandler;

/**
 * <p>Description  : SampleApi.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/12/4.</p>
 * <p>Time         : 下午5:56.</p>
 */
public abstract class SampleApi extends SampleDefaultApi{
    public static SampleApi instance() {
        return HttpManager.bind(SampleApi.class);
    }

    @Get(url = "https://www.baidu.com/")
    public abstract void baidu(@Callback TextResponseHandler callback);

    @Get(url = "s")
    public abstract HttpRequest search(String wd,
                                       @Callback TextResponseHandler callback);
}
