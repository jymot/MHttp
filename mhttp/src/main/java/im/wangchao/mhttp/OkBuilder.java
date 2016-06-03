package im.wangchao.mhttp;

import java.net.URL;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.HttpUrl;

/**
 * <p>Description  : OkRequestBuilder.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/2.</p>
 * <p>Time         : 下午4:34.</p>
 */
public interface OkBuilder<T, R> {

    T url(HttpUrl url);

    /**
     * Sets the URL target of this request.
     *
     * @throws IllegalArgumentException if {@code url} is not a valid HTTP or HTTPS URL. Avoid this
     * exception by calling {@link HttpUrl#parse}; it returns null for invalid URLs.
     */
    T url(String url);

    /**
     * Sets the URL target of this request.
     *
     * @throws IllegalArgumentException if the scheme of {@code url} is not {@code http} or {@code
     * https}.
     */
    T url(URL url);

    /**
     * Sets the header named {@code name} to {@code value}. If this request already has any headers
     * with that name, they are all replaced.
     */
    T header(String name, String value);

    /**
     * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
     * headers like "Cookie".
     *
     * <p>Note that for some headers including {@code Content-Length} and {@code Content-Encoding},
     * OkHttp may replace {@code value} with a header derived from the request body.
     */
    T addHeader(String name, String value);

    T removeHeader(String name);

    /** Removes all headers on this builder and adds {@code headers}. */
    T headers(Headers headers);

    /**
     * Sets this request's {@code Cache-Control} header, replacing any cache control headers already
     * present. If {@code cacheControl} doesn't define any directives, this clears this request's
     * cache-control headers.
     */
    T cacheControl(CacheControl cacheControl);

    T method(String method);

    /**
     * Replace {@link OkRequestParams}
     */
    T requestParams(OkRequestParams params);

    /**
     * Attaches {@code tag} to the request. It can be used later to cancel the request. If the tag
     * is unspecified or null, the request is canceled by using the request itself as the tag.
     */
    T tag(Object tag);

    T callback(OkCallback callback);

    R build();
}
