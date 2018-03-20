package im.wangchao.mhttp.internal.log;

import android.util.Log;

import im.wangchao.mhttp.internal.Version;
import im.wangchao.mhttp.internal.interceptor.HttpLoggingInterceptor;

/**
 * <p>Description  : Logger.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 2018/3/20.</p>
 * <p>Time         : 下午5:36.</p>
 */
public class LoggerImpl implements HttpLoggingInterceptor.Logger{
    private static final String TAG = Version.moduleName();

    @Override public void log(String message) {
        Log.e(TAG, message);
    }
}
