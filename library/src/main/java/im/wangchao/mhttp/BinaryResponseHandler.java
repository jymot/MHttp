package im.wangchao.mhttp;

import android.support.annotation.Nullable;

/**
 * <p>Description  : BinaryResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:49.</p>
 */
public class BinaryResponseHandler extends AbsResponseHandler{
    @Override final protected void onSuccess(HttpResponse response) {
        onSuccess(response.body(), response);
    }

    @Override protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {

    }

    @Override public ResponseDataType getResponseDataType() {
        return ResponseDataType.DATA;
    }

    public void onSuccess(byte[] bytes, HttpResponse response){

    }
}
