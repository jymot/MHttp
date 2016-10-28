package im.wangchao.mhttp;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * <p>Description  : MRequest.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/2.</p>
 * <p>Time         : 下午4:16.</p>
 */
public final class Request {
    public static Builder builder(){
        return new Builder();
    }

    private final okhttp3.Request rawRequest;
    private final RequestParams mRequestParams;
    private final Callback mCallback;
    private final int timeout;
    private final Executor mExecutor;
    private final ThreadMode mThreadMode;

    private okhttp3.Call rawCall;

    private Request(Builder builder){
        rawRequest = builder.rawRequest;
        mRequestParams = builder.mRequestParams;
        mCallback = builder.mCallback;
        timeout = builder.timeout;
        mExecutor = builder.mExecutor;
        mThreadMode = builder.mThreadMode;
    }

    public okhttp3.Request raw() {
        return rawRequest;
    }

    public HttpUrl url() {
        return rawRequest.url();
    }

    public String method() {
        return rawRequest.method();
    }

    public Headers headers() {
        return rawRequest.headers();
    }

    public String header(String name) {
        return rawRequest.header(name);
    }

    public List<String> headers(String name) {
        return rawRequest.headers(name);
    }

    public RequestBody body() {
        return rawRequest.body();
    }

    public Object tag() {
        return rawRequest.tag();
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public Callback callback() {
        return mCallback;
    }

    /**
     * Returns the cache control directives for this response. This is never null, even if this
     * response contains no {@code Cache-Control} header.
     */
    public CacheControl cacheControl() {
        return rawRequest.cacheControl();
    }

    public boolean isHttps() {
        return rawRequest.isHttps();
    }

    public int timeout() {
        return timeout;
    }

    /**
     * The executor used for {@link Callback} methods on which thread work.
     */
    public Executor callbackExecutor() {
        return mExecutor;
    }

    /**
     * {@link Callback} methods on which thread work.
     */
    public ThreadMode callbackThreadMode() {
        return mThreadMode;
    }

    public RequestParams requestParams(){
        return mRequestParams;
    }

    /**
     * Send the async request.
     */
    public Request enqueue(){
        MHttp.instance().timeout(timeout());

        Callback callback = callback();
        callback.initialize(this);
        if (callback instanceof AbsCallbackHandler){
            ((AbsCallbackHandler) callback).sendStartEvent();
        }
        rawCall().enqueue(callback);
        return this;
    }

    /**
     * Send the sync request.
     */
    public Response execute() throws IOException {
        MHttp.instance().timeout(timeout());
        return Response.newResponse(this, rawCall().execute());
    }

    /**
     * Cancel this request
     */
    public Request cancel(){
        if (rawCall().isCanceled()){
            return this;
        }
        rawCall().cancel();
        return this;
    }

    private Call rawCall(){
        if (rawCall == null){
            OkHttpClient client = MHttp.instance().client();
            rawCall = client.newCall(raw());
        }
        return rawCall;
    }

    @Override public String toString() {
        return rawRequest.toString();
    }

    public static class Builder {
        private static final String TAG = Builder.class.getSimpleName();

        okhttp3.Request rawRequest;
        okhttp3.Request.Builder rawBuilder;
        RequestParams mRequestParams;
        Callback mCallback;
        int timeout;
        private String method;
        Executor mExecutor;
        ThreadMode mThreadMode;

        public Builder() {
            mCallback = Callback.EMPTY;
            method = Method.GET;
            rawBuilder = new okhttp3.Request.Builder();
            mRequestParams = new RequestParams();
            timeout = 30;
            mThreadMode = ThreadMode.MAIN;
        }

        private Builder(Request request) {
            mCallback = request.mCallback;
            method = request.method();
            mRequestParams = request.mRequestParams;
            rawBuilder = request.rawRequest.newBuilder();
            timeout = request.timeout;
            mExecutor = request.mExecutor;
            mThreadMode = request.mThreadMode;
        }

        public Builder url(HttpUrl url) {
            rawBuilder.url(url);
            return this;
        }

        public Builder url(String url) {
            rawBuilder.url(url);
            return this;
        }

        public Builder url(URL url) {
            rawBuilder.url(url);
            return this;
        }

        public Builder header(String name, String value) {
            rawBuilder.header(name, value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            rawBuilder.addHeader(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            rawBuilder.removeHeader(name);
            return this;
        }

        public Builder headers(Headers headers) {
            rawBuilder.headers(headers);
            return this;
        }

        public Builder cacheControl(CacheControl cacheControl) {
            rawBuilder.cacheControl(cacheControl);
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

        public Builder requestParams(RequestParams params) {
            if (params != null){
                mRequestParams = params;
            }
            return this;
        }

        public Builder method(@NonNull String method) {
            this.method = method;
            return this;
        }

        public Builder tag(Object tag) {
            rawBuilder.tag(tag);
            return this;
        }

        public Builder callback(@NonNull Callback callback){
            mCallback = callback;
            return this;
        }

        public Builder timeout(int timeout){
            this.timeout = timeout;
            return this;
        }

        public Builder callbackExecutor(Executor executor){
            mExecutor = executor;
            return this;
        }

        public Builder callbackThreadMode(ThreadMode threadMode){
            mThreadMode = threadMode;
            return this;
        }

        public Builder userAgent(String ua){
            header("User-Agent", ua);
            return this;
        }

        public Request build() {
            MHttp.instance().timeout(timeout);

            if (!Accept.EMPTY.equals(mCallback.accept())) {
                addHeader("Accept", mCallback.accept());
            }

            switch (method){
                case Method.GET:
                    rawBuilder.method(method, null);
                    rawRequest = rawBuilder.build();
                    rawRequest = rawBuilder.url(mRequestParams.formatURLParams(rawRequest.url())).build();
                    break;
                case Method.HEAD:
                    rawBuilder.method(method, null);
                    rawRequest = rawBuilder.build();
                    break;
                default:
                    rawBuilder.method(method, mRequestParams.requestBody(rawBuilder.build().headers()));
                    rawRequest = rawBuilder.build();
                    break;
            }

            return new Request(this);
        }
    }
}
