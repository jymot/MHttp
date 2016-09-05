package im.wangchao.mhttpdemo;

import im.wangchao.http.annotations.Callback;
import im.wangchao.http.annotations.Get;
import im.wangchao.http.annotations.Tag;
import im.wangchao.mhttp.MHttp;
import im.wangchao.mhttp.Request;
import im.wangchao.mhttp.callback.TextCallbackHandler;

/**
 * <p>Description  : SampleApi.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/12/4.</p>
 * <p>Time         : 下午5:56.</p>
 */
public abstract class SampleApi extends SampleDefaultApi{
    public static SampleApi instance() {
        return MHttp.create(SampleApi.class);
    }

    @Get(url = "https://www.baidu.com/")
    public abstract void baidu(@Callback TextCallbackHandler callback);

    @Get(url = "s", tag = "aaa")
    public abstract Request search(String wd, @Callback TextCallbackHandler callback);

    @Get(url = "s", tag = "aaa")
    public abstract Request search1(String wd, @Callback TextCallbackHandler callback, @Tag Object a);
}
