package im.wangchao.mhttp;

/**
 * <p>Description  : TextResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:41.</p>
 */
public class TextCallbackHandler extends AbsCallbackHandler<String> {
    @Override protected void onSuccess(String data, Response response) {
    }

    @Override protected void onFailure(Response response, Throwable throwable) {
    }

    @Override protected String backgroundParser(Response response) throws Exception {
        return byteArrayToString(response.raw().body().bytes());
    }

    @Override public String accept() {
        return Accept.ACCEPT_TEXT;
    }
}
