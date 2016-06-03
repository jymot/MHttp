package im.wangchao.mhttp;

import okhttp3.Response;

/**
 * <p>Description  : OkResponse.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/3.</p>
 * <p>Time         : 下午3:02.</p>
 */
public interface OkResponse {

    /**
     * Return {@link OkRequest}, maybe null.
     */
    OkRequest request();

    /**
     * Return {@link Response}
     */
    Response response();

}
