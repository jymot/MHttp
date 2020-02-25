package im.wangchao.mhttp.callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import im.wangchao.mhttp.AbsCallbackHandler;
import im.wangchao.mhttp.Accept;
import im.wangchao.mhttp.Response;

/**
 * <p>Description  : JSONResponseHandler.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:25.</p>
 */
public class JSONCallbackHandler extends AbsCallbackHandler<JSONCallbackHandler.JSON> {

    @Override public void onSuccess(JSON data, Response response) {

    }

    @Override public void onFailure(Response response, Throwable throwable) {

    }

    @Override public JSON backgroundParser(Response response) throws Exception {
        final byte[] body = response.raw().body().bytes();
        final String bodyString = byteArrayToString(body);
        JSON json = new JSON();

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
