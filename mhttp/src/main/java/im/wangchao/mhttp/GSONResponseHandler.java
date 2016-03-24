package im.wangchao.mhttp;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <p>Description  : GSONResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/3/20.</p>
 * <p>Time         : 上午9:06.</p>
 */
public abstract class GSONResponseHandler<T> extends JSONResponseHandler{

    @Override final public void onSuccess(JSONArray jsonArray, HttpResponse response) {
        super.onSuccess(jsonArray, response);
        onSuccess(parser(jsonArray.toString()));
    }

    @Override final public void onSuccess(JSONObject jsonObject, HttpResponse response) {
        super.onSuccess(jsonObject, response);
        onSuccess(parser(jsonObject.toString()));
    }

    /** parser Json to T */
    protected abstract T parser(String jsonString);

    public void onSuccess(T t){

    }

}
