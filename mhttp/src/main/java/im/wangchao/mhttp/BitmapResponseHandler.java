package im.wangchao.mhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

/**
 * <p>Description  : ImageResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:49.</p>
 */
public class BitmapResponseHandler extends AbsResponseHandler{
    @Override final protected void onSuccess(HttpResponse response) {
        onSuccess(BitmapFactory.decodeStream(response.byteStream()), response);
    }

    @Override protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {

    }

    @Override protected String accept() {
        return Accept.ACCEPT_IMAGE;
    }

    public void onSuccess(Bitmap bitmap, HttpResponse response){

    }
}
