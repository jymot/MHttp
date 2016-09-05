package im.wangchao.mhttp.callback;

import im.wangchao.mhttp.AbsCallbackHandler;
import im.wangchao.mhttp.Accept;
import im.wangchao.mhttp.Response;

/**
 * <p>Description  : BinaryResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:49.</p>
 */
public class BinaryCallbackHandler extends AbsCallbackHandler<byte[]> {

    @Override protected void onSuccess(byte[] data, Response response) {

    }

    @Override protected void onFailure(Response response, Throwable throwable) {

    }

    @Override protected byte[] backgroundParser(Response response) throws Exception {
        return response.raw().body().bytes();
    }

    @Override public String accept() {
        return Accept.ACCEPT_DATA;
    }
}
