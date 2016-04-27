package im.wangchao.mhttp;

import android.graphics.Bitmap;

/**
 * <p>Description  : ImageResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:49.</p>
 */
public abstract class BitmapResponseHandler extends AbsResponseHandler<Bitmap>{
    @Override protected void onSuccess(Bitmap bitmap, HttpResponse response) {
    }

    @Override protected void onFailure(HttpResponse response, Throwable throwable) {

    }

    @Override protected String accept() {
        return Accept.ACCEPT_IMAGE;
    }

}
