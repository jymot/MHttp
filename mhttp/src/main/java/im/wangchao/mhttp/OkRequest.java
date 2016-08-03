package im.wangchao.mhttp;

import java.util.List;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * <p>Description  : OkRequest.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/2.</p>
 * <p>Time         : 下午4:11.</p>
 */
public interface OkRequest<T, R> {
    /**
     * Returns the real OkHttp Request
     */
    Request request();

    HttpUrl url();

    String method();

    Headers headers();

    String header(String name);

    List<String> headers(String name);

    RequestBody body();

    Object tag();

    /**
     * Create custom Builder
     */
    T newBuilder();

    OkCallback callback();

    OkRequestParams requestParams();

    /**
     * Returns the cache control directives for this response. This is never null, even if this
     * response contains no {@code Cache-Control} header.
     */
    CacheControl cacheControl();

    R send();

    /**
     * Cancel this request
     */
    R cancel();

    boolean isHttps();

    int timeout();

    ThreadMode callbackThreadMode();
}
