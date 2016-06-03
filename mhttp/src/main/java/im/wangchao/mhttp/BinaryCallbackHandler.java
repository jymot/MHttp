package im.wangchao.mhttp;

/**
 * <p>Description  : BinaryResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:49.</p>
 */
public class BinaryCallbackHandler extends AbsCallbackHandler<byte[]> {

    @Override protected void onSuccess(byte[] data, OkResponse response) {

    }

    @Override protected void onFailure(OkResponse response, Throwable throwable) {

    }

    @Override protected byte[] backgroundParser(OkResponse response) throws Exception {
        return response.response().body().bytes();
    }

    @Override public String accept() {
        return Accept.ACCEPT_DATA;
    }
}
