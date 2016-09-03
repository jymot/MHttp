package im.wangchao.mhttp;

import android.graphics.Bitmap;

/**
 * <p>Description  : ImageResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:49.</p>
 */
public abstract class BitmapCallbackHandler extends AbsCallbackHandler<Bitmap> {
    @Override protected void onSuccess(Bitmap bitmap, Response response) {
    }

    @Override protected void onFailure(Response response, Throwable throwable) {

    }

    @Override public String accept() {
        return Accept.ACCEPT_IMAGE;
    }

}
