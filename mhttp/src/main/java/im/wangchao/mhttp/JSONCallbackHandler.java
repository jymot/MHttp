package im.wangchao.mhttp;

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
public class JSONCallbackHandler extends AbsCallbackHandler<JSONCallbackHandler.JSON> {

    @Override protected void onSuccess(JSON data, OkResponse response) {

    }

    @Override protected void onFailure(OkResponse response, Throwable throwable) {

    }

    @Override protected JSON backgroundParser(OkResponse response) throws Exception {
        final byte[] body = response.response().body().bytes();
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

    @Override public String accept() {
        return Accept.ACCEPT_JSON;
    }

    public static class JSON {
        public JSONObject jsonObject;
        public JSONArray jsonArray;
    }
}
