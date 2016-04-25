package im.wangchao.mhttp;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * <p>Description  : HttpResponseBody. Wrapper ResponseBody</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/4/25.</p>
 * <p>Time         : 下午2:55.</p>
 */
public class HttpResponseBody extends ResponseBody {
    private final ResponseBody body;
    private final File file;
    private final byte[] bytes;

    public HttpResponseBody(ResponseBody body, byte[] bytes, File file){
        this.body   = body;
        this.file   = file;
        this.bytes  = bytes;
    }

    public File file(){
        return file;
    }

    public byte[] bytesBody(){
        return bytes;
    }

    @Override public MediaType contentType() {
        return body == null ? null : body.contentType();
    }

    @Override public long contentLength() {
        return body == null ? 0 : body.contentLength();
    }

    @Override public BufferedSource source() {
        return body == null ? null : body.source();
    }
}
