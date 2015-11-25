package im.wangchao.mhttp;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import im.wangchao.http.InjectApi;

/**
 * <p>Description  : HttpRequest.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午1:34.</p>
 */
final public class HttpRequest {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Method.POST, Method.GET})
    public @interface MethodType{}

    public interface Method{
        String POST = "POST";
        String GET  = "GET";
    }

    final private String                requestUrl;
    final private Headers               headers;
    final private RequestParams         requestParams;
    final private String                method;
    final private HttpClientInterface   httpClient;
    final private AbsResponseHandler    responseHandler;
    final private int                   timeout;
    final private Object                tag;

    public HttpRequest(Builder builder){
        this.requestUrl         = builder.requestUrl;
        this.headers            = builder.headers.build();
        this.requestParams      = builder.requestParams;
        this.method             = builder.method;
        this.httpClient         = builder.httpClient;
        this.responseHandler    = builder.responseHandler;
        this.timeout            = builder.timeout;
        this.tag                = builder.tag;
    }

    public String getRequestUrl(){
        return requestUrl;
    }

    public AbsResponseHandler getResponseHandler(){
        return responseHandler;
    }

    public Headers getHeaders(){
        return headers;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public String getMethod() {
        return method;
    }

    public HttpClientInterface getHttpClient() {
        return httpClient;
    }

    public Object getTag() {
        return tag;
    }

    public int getTimeout() {
        return timeout;
    }

    public Builder newBuilder(){
        return new Builder(this);
    }

    public HttpRequest execute(){
        getHttpClient().execute(this);
        return this;
    }

    public HttpRequest cancel(){
        return cancel(null);
    }

    public HttpRequest cancel(Object tag){
        getHttpClient().cancel(tag);
        return this;
    }

    public static <T> T inject(Class<T> api){
        return InjectApi.inject(api);
    }

    public static class Builder{
        private String              requestUrl;
        private Headers.Builder     headers;
        private RequestParams       requestParams;
        private String              method;
        private HttpClientInterface httpClient;
        private AbsResponseHandler  responseHandler;
        private int                 timeout;
        private Object              tag;

        /*                  default                 */
        public Builder(){
            this.method             = Method.POST;
            this.httpClient         = new OkHttpClientImpl();
            this.headers            = new Headers.Builder();
            this.requestParams      = new RequestParams();
            this.responseHandler    = new SilentResponseHandler();
            this.timeout            = 30;
        }

        private Builder(HttpRequest request){
            this.requestParams      = request.requestParams;
            this.headers            = request.headers.newBuilder();
            this.requestUrl         = request.requestUrl;
            this.method             = request.method;
            this.httpClient         = request.httpClient;
            this.responseHandler    = request.responseHandler;
            this.tag                = request.tag;
        }

        public Builder url(@NonNull String url){
            if (TextUtils.isEmpty(url)){
                throw new IllegalArgumentException("url is empty");
            }
            this.requestUrl = url;
            return this;
        }

        public Builder responseHandler(@NonNull AbsResponseHandler responseHandler){
            this.responseHandler = responseHandler;
            return this;
        }

        public Builder httpClient(@NonNull HttpClientInterface httpClient){
            this.httpClient = httpClient;
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

        public Builder headers(Headers headers){
            if (headers != null){
                this.headers = headers.newBuilder();
            }
            return this;
        }

        public Builder timeout(int timeout){
            this.timeout = timeout;
            return this;
        }

        public Builder params(RequestParams params){
            this.requestParams = params;
            return this;
        }

        public Builder method(@MethodType String method){
            if (!method.equals(Method.GET) && !method.equals(Method.POST)){
                throw new IllegalArgumentException("method is not equal to 1 or 2");
            }
            this.method = method;
            return this;
        }

        public Builder tag(String tag){
            this.tag = tag;
            return this;
        }

        public HttpRequest build(){
            if (TextUtils.isEmpty(requestUrl)){
                throw new IllegalStateException("url is empty");
            }
            if (httpClient == null){
                throw new IllegalStateException("httpClient is null");
            }
            return new HttpRequest(this);
        }
    }
}
