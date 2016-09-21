package im.wangchao.mhttp.body;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * <p>Description  : OctetStreamBody.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/3/8.</p>
 * <p>Time         : 下午4:31.</p>
 */
public final class OctetStreamBody extends RequestBody{
    private final static MediaType CONTENT_TYPE = MediaType.parse("application/octet-stream");
    private final MediaType contentType;
    private final InputStream stream;

    public OctetStreamBody(InputStream stream){
        this(stream, null);
    }

    public OctetStreamBody(InputStream stream, String contentType){
        this.stream = stream;
        this.contentType = TextUtils.isEmpty(contentType) ? CONTENT_TYPE : MediaType.parse(contentType);
    }

    @Override public MediaType contentType() {
        return contentType;
    }

    @Override public long contentLength() throws IOException {
        try {
            return stream.available();
        } catch (Exception e){
            return super.contentLength();
        }
    }

    @Override public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(stream);
            sink.writeAll(source);
        } finally {
            Util.closeQuietly(source);
        }
    }

}
