package im.wangchao.mhttp;

import android.content.Context;

import java.net.CookieHandler;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

/**
 * <p>Description  : NewHttpClient.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/12/2.</p>
 * <p>Time         : 下午9:15.</p>
 */
public interface IHttpClient<T> {
    /**
     * 设置超时时间
     *
     * @param timeout   时间
     * @param timeUnit  单位
     */
    void setTimeoutHook(long timeout, TimeUnit timeUnit);

    /**
     * 取消 Tag = tag 的所有请求,不会触发回调
     *
     * @param tag
     */
    void cancelHook(Object tag);

    /**
     * 创建一个请求指令
     *
     * @param request   请求实体
     * @return          IHttpCall
     */
    IHttpCall newCall(HttpRequest request);

    /**
     * 设置 CookieHandler
     *
     * @param cookieHandler CookieHandler
     */
    void setCookieHandlerHook(CookieHandler cookieHandler);

    /**
     * 设置缓存目录
     *
     * @param context   Context
     * @param dirName   目录名，/data/data/package/caches/dirName
     */
    void cache(Context context, String dirName);

    /**
     * 设置 SocketFactory
     *
     * @param factory SocketFactory
     */
    void setSslSocketFactoryHook(SocketFactory factory);

    /**
     * @return getHttpClient cast to T
     */
    T getHttpClient();

}
