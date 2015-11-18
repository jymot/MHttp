package im.wangchao.mhttp;

import android.support.annotation.Nullable;

/**
 * <p>Description  : SilentResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/25.</p>
 * <p>Time         : 下午9:36.</p>
 */
/*package*/ class SilentResponseHandler extends AbsResponseHandler {
    @Override
    protected void onSuccess(HttpResponse response) {
    }

    @Override
    protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {
    }
}
