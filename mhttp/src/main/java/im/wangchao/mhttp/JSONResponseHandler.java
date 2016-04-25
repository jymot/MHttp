package im.wangchao.mhttp;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>Description  : JSONResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:25.</p>
 */
public class JSONResponseHandler extends AbsResponseHandler{

    @Override final protected void onSuccess(HttpResponse response) {
        final byte[] body = response.bodyBytes();
        final String bodyString = byteArrayToString(body);
        if (bodyString != null){
            try {
                JSONObject jsonObject = new JSONObject(bodyString);
                onSuccess(jsonObject, response);
            } catch (JSONException e) {
                try {
                    JSONArray jsonArray = new JSONArray(bodyString);
                    onSuccess(jsonArray, response);
                } catch (JSONException e1) {
                    onFailure(response, new IllegalArgumentException("Response body can not cast to JSON"));
                }
            }
        } else {
            onFailure(response, null);
        }
    }

    @Override protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {

    }

    @Override protected String accept() {
        return Accept.ACCEPT_JSON;
    }

    public void onSuccess(JSONObject jsonObject, HttpResponse response){
        print("JSONResponseHandler - body has been cast to JSONObject");
    }

    public void onSuccess(JSONArray jsonArray, HttpResponse response){
        print("JSONResponseHandler - body has been cast to JSONArray");
    }

}
