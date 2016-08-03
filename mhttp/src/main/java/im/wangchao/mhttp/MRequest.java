package im.wangchao.mhttp;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * <p>Description  : MRequest.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/2.</p>
 * <p>Time         : 下午4:16.</p>
 */
public final class MRequest implements OkRequest<MRequest.Builder, MRequest> {
    public static Builder builder(){
        return new Builder();
    }

    final Request okRequest;
    final OkRequestParams mRequestParams;
    final OkCallback mCallback;
    final int timeout;
    final ThreadMode mThreadMode;

    private MRequest(Builder builder){
        okRequest = builder.okRequest;
        mRequestParams = builder.mRequestParams;
        mCallback = builder.mCallback;
        timeout = builder.timeout;
        mThreadMode = builder.mThreadMode;
    }

    @Override public Request request() {
        return okRequest;
    }

    @Override public HttpUrl url() {
        return okRequest.url();
    }

    @Override public String method() {
        return okRequest.method();
    }

    @Override public Headers headers() {
        return okRequest.headers();
    }

    @Override public String header(String name) {
        return okRequest.header(name);
    }

    @Override public List<String> headers(String name) {
        return okRequest.headers(name);
    }

    @Override public RequestBody body() {
        return okRequest.body();
    }

    @Override public Object tag() {
        return okRequest.tag();
    }

    @Override public Builder newBuilder() {
        okRequest.newBuilder();
        return new Builder(this);
    }

    @Override public OkCallback callback() {
        return mCallback;
    }

    /**
     * Returns the cache control directives for this response. This is never null, even if this
     * response contains no {@code Cache-Control} header.
     */
    @Override public CacheControl cacheControl() {
        return okRequest.cacheControl();
    }

    @Override public boolean isHttps() {
        return okRequest.isHttps();
    }

    @Override public int timeout() {
        return timeout;
    }

    /**
     * @return {@link OkCallback} in which thread work.
     */
    @Override public ThreadMode callbackThreadMode() {
        return mThreadMode;
    }

    @Override public OkRequestParams requestParams(){
        return mRequestParams;
    }

    /**
     * Send this request
     */
    @Override public MRequest send(){
        MHttp.instance().enqueue(this);
        return this;
    }

    /**
     * Cancel this request
     */
    @Override public MRequest cancel(){
        MHttp.instance().cancel(okRequest.tag());
        return this;
    }

    @Override public String toString() {
        return okRequest.toString();
    }

    public static class Builder implements OkBuilder<Builder, MRequest> {
        private static final String TAG = Builder.class.getSimpleName();

        Request okRequest;
        Request.Builder okBuilder;
        OkRequestParams mRequestParams;
        OkCallback mCallback;
        int timeout;
        private String method;
        ThreadMode mThreadMode;

        public Builder() {
            mCallback = OkCallback.EMPTY;
            method = Method.GET;
            okBuilder = new Request.Builder();
            mRequestParams = new RequestParams();
            timeout = 30;
            mThreadMode = ThreadMode.MAIN;
        }

        private Builder(MRequest request) {
            mCallback = request.mCallback;
            method = request.method();
            mRequestParams = request.mRequestParams;
            okBuilder = request.okRequest.newBuilder();
            timeout = request.timeout;
            mThreadMode = request.mThreadMode;
        }

        @Override public Builder url(HttpUrl url) {
            okBuilder.url(url);
            return this;
        }

        @Override public Builder url(String url) {
            okBuilder.url(url);
            return this;
        }

        @Override public Builder url(URL url) {
            okBuilder.url(url);
            return this;
        }

        @Override public Builder header(String name, String value) {
            okBuilder.header(name, value);
            return this;
        }

        @Override public Builder addHeader(String name, String value) {
            okBuilder.addHeader(name, value);
            return this;
        }

        @Override public Builder removeHeader(String name) {
            okBuilder.removeHeader(name);
            return this;
        }

        @Override public Builder headers(Headers headers) {
            okBuilder.headers(headers);
            return this;
        }

        @Override public Builder cacheControl(CacheControl cacheControl) {
            okBuilder.cacheControl(cacheControl);
            return this;
        }

        public Builder get() {
            return method(Method.GET);
        }

        public Builder head() {
            return method(Method.HEAD);
        }

        public Builder post() {
            return method(Method.POST);
        }

        public Builder delete() {
            return method(Method.DELETE);
        }

        public Builder put() {
            return method(Method.PUT);
        }

        public Builder patch() {
            return method(Method.PATCH);
        }

        /**
         * Simple to add request parameter
         */
        public Builder addParameter(String key, Object value){
            mRequestParams.put(key, value);
            return this;
        }

        /**
         * Simple to add request parameter
         */
        public Builder addParameter(String key, InputStream stream, String name, String contentType){
            mRequestParams.put(key, stream, name, contentType);
            return this;
        }

        /**
         * Simple to add request parameter
         */
        public Builder addParameter(String key, File file, String contentType){
            try {
                mRequestParams.put(key, file, contentType);
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return this;
        }

        @Override public Builder requestParams(OkRequestParams params) {
            if (params != null){
                mRequestParams = params;
            }
            return this;
        }

        @Override public Builder method(@NonNull String method) {
            this.method = method;
            return this;
        }

        @Override public Builder tag(Object tag) {
            okBuilder.tag(tag);
            return this;
        }

        @Override public Builder callback(@NonNull OkCallback callback){
            mCallback = callback;
            return this;
        }

        public Builder callbackThreadMode(ThreadMode mode){
            mThreadMode = mode;
            return this;
        }

        @Override public MRequest build() {
            boolean isGet = Method.GET.equals(method);

            if (!Accept.EMPTY.equals(mCallback.accept())) {
                addHeader("Accept", mCallback.accept());
            }

            okBuilder.method(method, isGet ? null : mRequestParams.requestBody());
            okRequest = okBuilder.build();

            if (isGet){
                okRequest = okBuilder.url(mRequestParams.formatURLParams(okRequest.url())).build();
            }
            return new MRequest(this);
        }
    }
}
