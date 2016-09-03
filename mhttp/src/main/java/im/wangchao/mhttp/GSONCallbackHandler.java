package im.wangchao.mhttp;

/**
 * <p>Description  : GSONResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/3/20.</p>
 * <p>Time         : 上午9:06.</p>
 */
public abstract class GSONCallbackHandler<T> extends JSONCallbackHandler {

    @Override final protected void onSuccess(JSON data, Response response) {
        if (data.jsonArray != null) {
            onSuccess(parser(data.jsonArray.toString()));
        }
        else if (data.jsonObject != null) {
            onSuccess(parser(data.jsonObject.toString()));
        }
    }

    @Override protected void onFailure(Response response, Throwable throwable) {

    }

    /** parser Json to T */
    protected abstract T parser(String jsonString);

    public void onSuccess(T t){

    }

}
