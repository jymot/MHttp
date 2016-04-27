package im.wangchao.mhttp;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * <p>Description  : HttpResponse.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午8:14.</p>
 */
public class HttpResponse {
    final private HttpRequest   request;
    final private Response      okResponse;
    final private int           code;
    final private Headers       headers;
    final private String        message;

    public HttpResponse(Builder builder){
        this.request        = builder.request;
        this.code           = builder.code;
        this.headers        = builder.headers;
        this.okResponse     = builder.okResponse;
        this.message        = builder.message;
    }

    final public int code() {
        return code;
    }

    final public Headers headers() {
        return headers;
    }

    final public byte[] bodyBytes() throws IOException{
        return okResponse == null ? new byte[0] : okResponse.body().bytes();
    }

    final public InputStream byteStream(){
        return okResponse == null ? null : okResponse.body().byteStream();
    }

    final public String message() {
        return message;
    }

    final public HttpRequest request() {
        return this.request;
    }

    /**
     * return null, when throw IOException.
     */
    @Nullable final public Response okResponse() {
        return this.okResponse;
    }

    final public Builder newBuilder(){
        return new Builder(this);
    }

    public static class Builder{
        private HttpRequest         request;
        private Response            okResponse;
        private int                 code;
        private Headers             headers;
        private String              message;

        public Builder(){
            this.headers    = new Headers.Builder().build();
        }

        public Builder(HttpResponse response){
            this.request        = response.request;
            this.code           = response.code;
            this.headers        = response.headers;
            this.okResponse     = response.okResponse;
            this.message        = response.message;
        }

        public Builder request(HttpRequest request){
            this.request = request;
            return this;
        }

        public Builder code(int code){
            this.code = code;
            return this;
        }

        public Builder header(Headers headers){
            this.headers = headers;
            return this;
        }

        public Builder response(Response okResponse){
            this.okResponse = okResponse;
            return this;
        }

        public Builder message(String message){
            this.message = message;
            return this;
        }

        public HttpResponse build(){
            return new HttpResponse(this);
        }
    }
}
