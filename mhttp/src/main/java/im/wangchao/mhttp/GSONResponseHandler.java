package im.wangchao.mhttp;

import android.support.annotation.Nullable;

/**
 * <p>Description  : GSONResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/3/20.</p>
 * <p>Time         : 上午9:06.</p>
 */
public abstract class GSONResponseHandler<T> extends JSONResponseHandler{

    @Override final protected void onSuccess(JSON data, HttpResponse response) {
        if (data.jsonArray != null) {
            onSuccess(parser(data.jsonArray.toString()));
        }
        else if (data.jsonObject != null) {
            onSuccess(parser(data.jsonObject.toString()));
        }
    }

    @Override protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {

    }
    @Override protected JSON backgroundParser(HttpResponse response) throws Exception {
        return super.backgroundParser(response);
    }

    /** parser Json to T */
    protected abstract T parser(String jsonString);

    public void onSuccess(T t){

    }

}
