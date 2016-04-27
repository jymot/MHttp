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
public class JSONResponseHandler extends AbsResponseHandler<JSONResponseHandler.JSON>{

    @Override protected void onSuccess(JSON data, HttpResponse response) {

    }

    @Override protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {

    }

    @Override protected JSON backgroundParser(HttpResponse response) throws Exception {
        final byte[] body = response.bodyBytes();
        final String bodyString = byteArrayToString(body);
        final JSON json = new JSON();

        if (bodyString != null){
            try {
                json.jsonObject = new JSONObject(bodyString);
            } catch (JSONException e) {
                json.jsonArray = new JSONArray(bodyString);
            }
        }
        return json;
    }

    @Override protected String accept() {
        return Accept.ACCEPT_JSON;
    }

    public static class JSON {
        public JSONObject jsonObject;
        public JSONArray jsonArray;
    }
}
