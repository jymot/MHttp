package im.wangchao.mhttp;

/**
 * <p>Description  : BinaryResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:49.</p>
 */
public class BinaryResponseHandler extends AbsResponseHandler<byte[]>{
    @Override protected void onSuccess(byte[] data, HttpResponse response) {
    }

    @Override protected void onFailure(HttpResponse response, Throwable throwable) {

    }

    @Override protected byte[] backgroundParser(HttpResponse response) throws Exception{
        return response.bodyBytes();
    }

    @Override protected String accept() {
        return Accept.ACCEPT_DATA;
    }
}
