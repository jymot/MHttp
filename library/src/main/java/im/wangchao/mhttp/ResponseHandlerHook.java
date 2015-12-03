package im.wangchao.mhttp;

/**
 * <p>Description  : ResponseHandlerHook.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/12/3.</p>
 * <p>Time         : 上午8:46.</p>
 */
public interface ResponseHandlerHook {
    /**
     * @return false 继续执行真实的 onStart，反之不执行
     */
    boolean doStart();

    /**
     * @return false 继续执行真实的 doSuccess，反之不执行
     */
    boolean doSuccess(HttpResponse response);

    /**
     * @return false 继续执行真实的 doFailure，反之不执行
     */
    boolean doFailure(HttpResponse response);

    /**
     * @return false 继续执行真实的 onProgress，反之不执行
     */
    boolean doProgress(int bytesWritten, int bytesTotal);

    /**
     * @return false 继续执行真实的 doCancel，反之不执行
     */
    boolean doCancel();

    /**
     * @return false 继续执行真实的 doFinish，反之不执行
     */
    boolean doFinish();
}
