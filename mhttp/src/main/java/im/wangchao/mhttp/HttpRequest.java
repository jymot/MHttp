package im.wangchao.mhttp;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Request;

/**
 * <p>Description  : HttpRequest.扩展 OkHttp Request</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午1:34.</p>
 */
final public class HttpRequest {
    final String                url;
    final Headers               headers;
    final RequestParams         requestParams;
    final String                method;
    final AbsResponseHandler    responseHandler;
    final Object                tag;

    /*package*/WeakReference<Call> _weakCall;
    private volatile CacheControl cacheControl; // Lazily initialized.

    public HttpRequest(Builder builder){
        this.url                = builder.url;
        this.headers            = builder.headers.build();
        this.requestParams      = builder.requestParams;
        this.method             = builder.method;
        this.responseHandler    = builder.responseHandler;
        this.tag                = builder.tag;
    }

    /** http request url */
    public String url(){
        return url;
    }

    /** http response handler */
    public AbsResponseHandler responseHandler(){
        return responseHandler;
    }

    /** http request headers */
    public Headers headers(){
        return headers;
    }

    /** http request params */
    public RequestParams requestParams() {
        return requestParams;
    }

    /** http request method */
    public String method() {
        return method;
    }

    /** http request tag */
    public Object tag() {
        return tag;
    }

    /** new HttpRequest.Builder*/
    public Builder newBuilder(){
        return new Builder(this);
    }

    /**
     * Returns the cache control directives for this response. This is never null, even if this
     * response contains no {@code Cache-Control} header.
     */
    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return result != null ? result : (cacheControl = CacheControl.parse(okhttp3.Headers.of(headers.namesAndValues())));
    }

    /** cancel this current request */
    public HttpRequest cancel(){
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final Call call = _weakCall == null ? null : _weakCall.get();
                if (call != null) {
                    call.cancel();
                }

                if (responseHandler != null) {
                    responseHandler.sendCancelMessage();
                }
            }
        };

        if (Looper.myLooper() == Looper.getMainLooper()){
            new Thread(r).start();
        } else {
            r.run();
        }

        return this;
    }

    /** execute request */
    public HttpRequest execute(){
        HttpManager.instance().enqueue(this);
        return this;
    }

    /**
     * @return Simple new default Builder
     */
    public static Builder builder(){
        return new Builder();
    }

    /** HttpRequest to okhttp3.Request */
    /*package*/ Request map(){
        final Request.Builder builder = new Request.Builder();

        builder.headers(okhttp3.Headers.of(headers.namesAndValues()));
        builder.addHeader("Accept", responseHandler.accept());

        if (method.equals(Method.GET)){
            builder.get().url(url.concat("?").concat(requestParams.formatURLParams()));
        } else {
            builder.url(url);
            builder.method(method, requestParams.requestBody());
        }

        builder.tag(tag);

        return builder.build();
    }

    @Override public boolean equals(Object o) {
        if (o == null || !(o instanceof HttpRequest)){
            return false;
        }
        HttpRequest target = (HttpRequest) o;
        String _url = target.url;
        Headers _headers = target.headers;
        RequestParams _params = target.requestParams;

        return (equals(url, _url) && equals(_headers, headers) && equals(_params, requestParams));
    }

    @Override public String toString() {
        return MessageFormat.format("HttpRequest{method={0}, url={1}, tag={2}}", method, url, tag);
    }

    private boolean equals(Object current, Object target){
        if (current != null){
            return current.equals(target);
        }
        return target == null;
    }

    public static class Builder{
        String              url;
        Headers.Builder     headers;
        RequestParams       requestParams;
        String              method;
        AbsResponseHandler  responseHandler;
        Object              tag;

        /*                  default                 */
        public Builder(){
            this.method             = Method.GET;
            this.headers            = new Headers.Builder();
            this.requestParams      = new RequestParams();
            this.responseHandler    = new SilentResponseHandler();
            this.tag                = String.valueOf(System.currentTimeMillis());
        }

        private Builder(HttpRequest request){
            this.requestParams      = request.requestParams;
            this.headers            = request.headers.newBuilder();
            this.url                = request.url;
            this.method             = request.method;
            this.responseHandler    = request.responseHandler;
            this.tag                = request.tag;
        }

        public Builder url(@NonNull String url){
            if (TextUtils.isEmpty(url)){
                throw new IllegalArgumentException("url is empty");
            }
            this.url = url;
            return this;
        }

        public Builder responseHandler(@NonNull AbsResponseHandler responseHandler){
            this.responseHandler = responseHandler;
            return this;
        }

        public Builder header(String name, String value){
            this.headers.set(name, value);
            return this;
        }

        public Builder addHeader(String name, String value){
            this.headers.add(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            this.headers.removeAll(name);
            return this;
        }

        public Builder headers(Headers headers){
            if (headers != null){
                this.headers = headers.newBuilder();
            }
            return this;
        }

        public Builder params(RequestParams params){
            this.requestParams = params;
            return this;
        }

        public Builder get(){
            return method(Method.GET);
        }

        public Builder post(){
            return method(Method.POST);
        }

        public Builder method(@Method.MethodType String method){
            this.method = method;
            return this;
        }

        public Builder tag(Object tag){
            if (tag != null ){
                this.tag = tag;
            }
            return this;
        }

        /**
         * Sets this request's {@code Cache-Control} header, replacing any cache control headers already
         * present. If {@code cacheControl} doesn't define any directives, this clears this request's
         * cache-control headers.
         */
        public Builder cacheControl(CacheControl cacheControl) {
            String value = cacheControl.toString();
            if (value.isEmpty()) return removeHeader("Cache-Control");
            return header("Cache-Control", value);
        }

        public HttpRequest build(){
            if (TextUtils.isEmpty(url)){
                throw new IllegalStateException("url is empty");
            }
            return new HttpRequest(this);
        }
    }
}
