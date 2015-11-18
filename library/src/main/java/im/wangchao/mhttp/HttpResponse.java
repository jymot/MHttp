package im.wangchao.mhttp;

/**
 * <p>Description  : HttpResponse.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午8:14.</p>
 */
public class HttpResponse {
    final private HttpRequest   request;
    final private int           code;
    final private Headers       headers;
    final private byte[]        body;
    final private String        message;

    public HttpResponse(Builder builder){
        this.request        = builder.request;
        this.code           = builder.code;
        this.headers        = builder.headers;
        this.body           = builder.body;
        this.message        = builder.message;
    }

    final public int code() {
        return code;
    }

    final public Headers headers() {
        return headers;
    }

    final public byte[] body() {
        return body;
    }

    final public String message() {
        return message;
    }

    final public HttpRequest request() {
        return this.request;
    }

    final public Builder newBuilder(){
        return new Builder(this);
    }

    public static class Builder{
        private HttpRequest request;
        private int         code;
        private Headers     headers;
        private byte[]      body;
        private String      message;

        public Builder(){

        }

        public Builder(HttpResponse response){
            this.request        = response.request;
            this.code           = response.code;
            this.headers        = response.headers;
            this.body           = response.body;
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

        public Builder body(byte[] body){
            this.body = body;
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
