package im.wangchao.mhttp;

import android.support.annotation.Nullable;

/**
 * <p>Description  : TextResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:41.</p>
 */
public class TextResponseHandler extends AbsResponseHandler{
    @Override final protected void onSuccess(HttpResponse response) {
        String text = byteArrayToString(response.body());
        onSuccess(text, response);
    }

    @Override protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {

    }

    @Override public ResponseDataType getResponseDataType() {
        return ResponseDataType.TEXT;
    }

    public void onSuccess(String text, HttpResponse response){
    }
}
