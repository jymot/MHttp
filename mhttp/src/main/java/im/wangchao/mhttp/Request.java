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

import im.wangchao.mhttp.body.MediaTypeUtils;
import im.wangchao.mhttp.rxjava2.ResponseExecuteObservable;
import io.reactivex.Observable;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
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

    private final okhttp3.Request mRawRequest;
    private final RequestParams mRequestParams;
    private final Callback mCallback;
    private final MediaType mMediaType;
    private final Executor mExecutor;
    private final ThreadMode mThreadMode;

    private okhttp3.Call mRawCall;

    private Request(Builder builder){
        mRawRequest = builder.mRawRequest;
        mRequestParams = builder.mRequestParams;
        mCallback = builder.mCallback;
        mMediaType = builder.mMediaType;
        mExecutor = builder.mExecutor;
        mThreadMode = builder.mThreadMode;
    }

    public okhttp3.Request raw() {
        return mRawRequest;
    }

    public HttpUrl url() {
        return mRawRequest.url();
    }

    public String method() {
        return mRawRequest.method();
    }

    public Headers headers() {
        return mRawRequest.headers();
    }

    public String header(String name) {
        return mRawRequest.header(name);
    }

    public List<String> headers(String name) {
        return mRawRequest.headers(name);
    }

    public RequestBody body() {
        return mRawRequest.body();
    }

    public Object tag() {
        return mRawRequest.tag();
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
        return mRawRequest.cacheControl();
    }

    public boolean isHttps() {
        return mRawRequest.isHttps();
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

    public <R> Observable<R> convert(Converter<Response, R> converter){
        return new ResponseExecuteObservable<>(this, converter);
    }

    private Call rawCall(){
        if (mRawCall == null){
            OkHttpClient client = MHttp.instance().client();
            mRawCall = client.newCall(raw());
        }
        return mRawCall;
    }

    @Override public String toString() {
        return mRawRequest.toString();
    }

    public static class Builder {
        private static final String TAG = Builder.class.getSimpleName();

        okhttp3.Request mRawRequest;
        okhttp3.Request.Builder mRawBuilder;
        RequestParams mRequestParams;
        Callback mCallback;
        String mMethod;
        MediaType mMediaType;
        Executor mExecutor;
        ThreadMode mThreadMode;

        public Builder() {
            mCallback = Callback.EMPTY;
            mMethod = Method.GET;
            mRawBuilder = new okhttp3.Request.Builder();
            mRequestParams = new RequestParams();
            mThreadMode = ThreadMode.MAIN;
        }

        private Builder(Request request) {
            mCallback = request.mCallback;
            mMethod = request.method();
            mRequestParams = request.mRequestParams;
            mRawBuilder = request.mRawRequest.newBuilder();
            mExecutor = request.mExecutor;
            mThreadMode = request.mThreadMode;
        }

        public Builder url(HttpUrl url) {
            mRawBuilder.url(MHttp.instance().proceedURL(url));
            return this;
        }

        public Builder url(String url) {
            mRawBuilder.url(MHttp.instance().proceedURL(url));
            return this;
        }

        public Builder url(URL url) {
            mRawBuilder.url(MHttp.instance().proceedURL(url));
            return this;
        }

        public Builder header(String name, String value) {
            mRawBuilder.header(name, value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            mRawBuilder.addHeader(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            mRawBuilder.removeHeader(name);
            return this;
        }

        public Builder headers(Headers headers) {
            mRawBuilder.headers(headers);
            return this;
        }

        public Builder cacheControl(CacheControl cacheControl) {
            mRawBuilder.cacheControl(cacheControl);
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
        public Builder addParameter(String key, InputStream stream, String name){
            mRequestParams.put(key, stream, name);
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
            this.mMethod = method;
            return this;
        }

        public Builder tag(Object tag) {
            mRawBuilder.tag(tag);
            return this;
        }

        public Builder callback(@NonNull Callback callback){
            mCallback = callback;
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

        public Builder contentType(@NonNull String contentType){
            this.mMediaType = MediaType.parse(contentType);
            header("Content-Type", contentType);
            return this;
        }

        public Builder contentType(@NonNull MediaType mediaType){
            this.mMediaType = mediaType;
            header("Content-Type", this.mMediaType.toString());
            return this;
        }

        public Request build() {
            if (mMediaType == null){
                // judgment request header
                List<String> headers = mRawBuilder.build().headers("Content-Type");
                final int len = headers.size();
                if (len != 0){
                    StringBuilder mediaType = new StringBuilder();
                    for (int i = 0; i < len; i++){
                        mediaType.append(headers.get(i));
                    }
                    mMediaType = MediaType.parse(mediaType.toString());
                    if (mMediaType == null){
                        mMediaType = MediaTypeUtils.DEFAULT;
                    }
                }
                // default is Application/json; charset=utf-8
                else {
                    mMediaType = MediaTypeUtils.DEFAULT;
                }
            }

            if (!Accept.EMPTY.equals(mCallback.accept())) {
                addHeader("Accept", mCallback.accept());
            }

            switch (mMethod){
                case Method.GET:
                    mRawBuilder.method(mMethod, null);
                    mRawRequest = mRawBuilder.build();
                    mRawRequest = mRawBuilder.url(mRequestParams.formatURLParams(mRawRequest.url())).build();
                    break;
                case Method.HEAD:
                    mRawBuilder.method(mMethod, null);
                    mRawRequest = mRawBuilder.build();
                    break;
                default:
                    // inject callback if exist.
                    mRawBuilder.method(mMethod, mRequestParams.requestBody(mMediaType,
                            (mCallback instanceof AbsCallbackHandler ? (AbsCallbackHandler) mCallback : null)));
                    mRawRequest = mRawBuilder.build();
                    break;
            }

            return new Request(this);
        }
    }
}
