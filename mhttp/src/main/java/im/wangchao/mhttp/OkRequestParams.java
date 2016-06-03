package im.wangchao.mhttp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.HttpUrl;
import okhttp3.RequestBody;

/**
 * <p>Description  : OkRequestParams.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/3.</p>
 * <p>Time         : 上午11:44.</p>
 */
public interface OkRequestParams {

    void put(String key, Object value);

    void put(String key, InputStream stream, String name, String contentType);

    void put(String key, File file, String contentType) throws FileNotFoundException;

    void remove(String key);

    boolean has(String key);

    HttpUrl formatURLParams(HttpUrl url);

    /**
     * {@link RequestBody}
     */
    RequestBody requestBody();
}
