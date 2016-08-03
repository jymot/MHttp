package im.wangchao.mhttp;

/**
 * <p>Description  : ThreadMode.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/8/3.</p>
 * <p>Time         : 下午12:44.</p>
 */
public enum ThreadMode {
    /**
     * Callback will be called in Android's main thread (UI thread).
     */
    MAIN,

    /**
     * Callback will be called in a background thread. That is, work on the request thread(okhttp thread).
     */
    BACKGROUND
}
