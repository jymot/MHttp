package im.wangchao.mhttp;

import okhttp3.Response;

/**
 * <p>Description  : MResponse.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/3.</p>
 * <p>Time         : 下午3:03.</p>
 */
public final class MResponse implements OkResponse{
    public static Builder builder(){
        return new Builder();
    }

    final OkRequest mRequest;
    final Response okResponse;

    private MResponse(Builder builder){
        mRequest = builder.mRequest;
        okResponse = builder.okResponse;
    }

    @Override public OkRequest request() {
        return mRequest;
    }

    @Override public Response response() {
        return okResponse;
    }

    public Builder newBuilder(){
        return new Builder(this);
    }

    public static class Builder {
        OkRequest mRequest;
        Response okResponse;

        public Builder(){

        }

        private Builder(MResponse response){
            mRequest = response.mRequest;
            okResponse = response.okResponse;
        }

        public Builder request(OkRequest request){
            mRequest = request;
            return this;
        }

        public Builder response(Response response){
            okResponse = response;
            return this;
        }

        public MResponse builder(){
            return new MResponse(this);
        }
    }
}
