package im.wangchao.mhttp;

import android.support.annotation.Nullable;

/**
 * <p>Description  : FileResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:39.</p>
 */
public class FileResponseHandler extends AbsResponseHandler {
    @Override protected void onSuccess(HttpResponse response) {

    }

    @Override protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {

    }

    @Override
    public ResponseDataType getResponseDataType() {
        return ResponseDataType.FILE;
    }
}
