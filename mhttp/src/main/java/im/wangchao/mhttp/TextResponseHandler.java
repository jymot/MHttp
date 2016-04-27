package im.wangchao.mhttp;

/**
 * <p>Description  : TextResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:41.</p>
 */
public class TextResponseHandler extends AbsResponseHandler<String>{
    @Override protected void onSuccess(String data, HttpResponse response) {
    }

    @Override protected void onFailure(HttpResponse response, Throwable throwable) {

    }

    @Override protected String backgroundParser(HttpResponse response) throws Exception {
        return byteArrayToString(response.bodyBytes());
    }

    @Override protected String accept() {
        return Accept.ACCEPT_TEXT;
    }
}
