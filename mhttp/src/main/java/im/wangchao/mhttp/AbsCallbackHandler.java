package im.wangchao.mhttp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import im.wangchao.mhttp.internal.exception.ParserException;
import im.wangchao.mhttp.internal.exception.ResponseFailException;
import okhttp3.Call;
import okhttp3.internal.Util;

/**
 * <p>Description  : AbsResponseHandler.
 *                   Callback lifecycle as follow:
 *                                              onStart()
 *                         -------------------------------------------------------
 *                              |
 *                              |
 *                         is canceled --- Y --- onCancel()
 *                              |
 *                              N
 *                              |
 *                          onFinish()
 *                              |
 *                              |
 *                        is successful --- N --- onFailure() ------------------
 *                              |                                                 |
 *                              Y                                                 |
 *                              |                                                 |
 *                        backgroundParser() --is download-- onProgress()         |
 *                              |                                     |           |
 *                              |                                     |           |
 *                          onSuccess()                           onSuccess()     |
 *                              |                                     |           |
 *                              |                                     |           |
 *                        ---------------------------------------------------------
 *                                             onFinally()
 *                          </p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午5:56.</p>
 */
public abstract class AbsCallbackHandler<Parser_Type> implements Callback {
    final private static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool(Util.threadFactory("OkHttp", false));

    final public static int     IO_EXCEPTION_CODE   = 1000;
    final public static String  DEFAULT_CHARSET     = "UTF-8";

    private Request request;
    private String responseCharset = DEFAULT_CHARSET;
    private boolean isFinished;
    private boolean isCanceled;

    private Executor mExecutor;

    /** Working thread depends on {@link #mExecutor}, default UI. */
    abstract protected void onSuccess(Parser_Type data, Response response);
    /** Working thread depends on {@link #mExecutor}, default UI. */
    abstract protected void onFailure(Response response, Throwable throwable);
    /** Work on the request thread, that is okhttp thread. */
    abstract protected Parser_Type backgroundParser(Response response) throws Exception;

    /** Working thread depends on {@link #mExecutor}, default UI. */
    protected void onStart(){}
    /** Working thread depends on {@link #mExecutor}, default UI. */
    protected void onCancel(){}
    /** Working thread depends on {@link #mExecutor}, default UI. */
    protected void onProgress(int bytesWritten, int bytesTotal){}
    /** Working thread depends on {@link #mExecutor}, default UI. */
    protected void onUploadProgress(int bytesWritten, int bytesTotal){}
    /** Working thread depends on {@link #mExecutor}, default UI. */
    protected void onFinish(){}
    /** Working thread depends on {@link #mExecutor}, default UI. */
    protected void onFinally(Response response){}


    @Override final public void onFailure(@NonNull Call call, @NonNull IOException e) {
        if (call.isCanceled()){
            sendCancelEvent();
            return;
        }
        sendFinishEvent();

        final Request req = request;
        Response response = Response.error(req,
                AbsCallbackHandler.IO_EXCEPTION_CODE,
                e.getMessage());

        sendFailureEvent(response, e);
        sendFinallyEvent(response);
    }

    @Override final public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
        if (call.isCanceled()){
            response.close();
            sendCancelEvent();
            return;
        }
        sendFinishEvent();

        final Request req = request;
        Response okResponse;
        if (response.isSuccessful()) {
            try {
                okResponse = Response.newResponse(req, response);
                Parser_Type data = backgroundParser(okResponse);
                sendSuccessEvent(data, okResponse);
            } catch (Exception e) {
                sendFailureEvent(okResponse = Response.newResponse(req, response), new ParserException());
            }
        } else {
            sendFailureEvent(okResponse = Response.newResponse(req, response), new ResponseFailException());
            response.close();
        }
        sendFinallyEvent(okResponse);
    }

    public AbsCallbackHandler(){}

    @Override public void initialize(Request request){
        isFinished = false;
        isCanceled = false;
        this.request = request;
        this.mExecutor = request.callbackExecutor();
        if (this.mExecutor == null){
            this.mExecutor = request.callbackThreadMode().executor();
        }
    }

    final public boolean isFinished(){
        return isFinished;
    }

    /**
     * Sets the charset for the response string. If not set, the default is UTF-8.
     */
    final public void setCharset(@NonNull final String charset) {
        this.responseCharset = charset;
    }

    /**
     * subclass can override this method to change charset.
     */
    protected String charset() {
        return TextUtils.isEmpty(responseCharset) ? DEFAULT_CHARSET : responseCharset;
    }

    /**
     * @return request accept
     */
    @Override public String accept(){
        return Accept.EMPTY;
    }

    final protected void print(String message){
        Log.d(AbsCallbackHandler.class.getSimpleName(), message);
    }

    @Nullable final protected String byteArrayToString(byte[] bytes){
        try {
            return bytes == null ? null : new String(bytes, charset());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    final protected Request getRequest(){
        return this.request;
    }

    /*package*/ final public void sendUploadProgressEvent(final int bytesWritten, final int bytesTotal) {
        execute(()->{
            try {
                onUploadProgress(bytesWritten, bytesTotal);
            } catch (Throwable t) {
                //Silent
            }
        });
    }

    /*package*/ final public void sendProgressEvent(final int bytesWritten, final int bytesTotal) {
        execute(()->{
            try {
                onProgress(bytesWritten, bytesTotal);
            } catch (Throwable t) {
                //Silent
            }
        });
    }

    /*package*/ final void sendSuccessEvent(final Parser_Type data, final Response response) {
        execute(() -> onSuccess(data, response));
    }

    /*package*/ final void sendFailureEvent(final Response response, @Nullable final Throwable throwable) {
        execute(() -> onFailure(response, throwable));
    }

    /*package*/ final void sendStartEvent() {
        if (request.callbackThreadMode() == ThreadMode.BACKGROUND){
            DEFAULT_EXECUTOR_SERVICE.execute(this::onStart);
        } else {
            execute(this::onStart);
        }
    }

    /*package*/ final void sendFinishEvent() {
        execute(() -> {
            AbsCallbackHandler.this.isFinished = true;
            onFinish();
        });
    }

    /*package*/ final void sendFinallyEvent(final Response response) {
        execute(() -> onFinally(response));
    }

    /*package*/ final synchronized void sendCancelEvent() {
        if (isCanceled){
            return;
        }
        execute(() -> {
            AbsCallbackHandler.this.isCanceled = true;
            onCancel();
        });
    }

    private void execute(Runnable command){
        if (mExecutor == null || threadInterrupted()){
            return;
        }

        mExecutor.execute(command);
    }

    private boolean threadInterrupted(){
        return Thread.currentThread().isInterrupted();
    }

}
