package im.wangchao.mhttp;

/**
 * <p>Description  : TextResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:41.</p>
 */
public class TextCallbackHandler extends AbsCallbackHandler<String> {
    @Override protected void onSuccess(String data, OkResponse response) {
    }

    @Override protected void onFailure(OkResponse response, Throwable throwable) {
    }

    @Override protected String backgroundParser(OkResponse response) throws Exception {
        return byteArrayToString(response.response().body().bytes());
    }

    @Override public String accept() {
        return Accept.ACCEPT_TEXT;
    }
}
