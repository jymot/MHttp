package im.wangchao.mhttp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import im.wangchao.mhttp.internal.exception.ParserException;
import im.wangchao.mhttp.internal.exception.ResponseFailException;
import okhttp3.Call;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * <p>Description  : AbsResponseHandler.
 *                   Callback lifecycle as follow:
 *                                              onStart()
 *                         -------------------------------------------------------
 *                              |
 *                              |
 *                         <is canceled> --- Y ---> onCancel()
 *                              |
 *                              N
 *                              |
 *                          onFinish()
 *                              |
 *                              |
 *                        <is successful> --- N ---> onFailure() ------------------
 *                              |                                                 |
 *                              Y                                                 |
 *                              |                                                 |
 *                        backgroundParser() --<is download>--> onProgress()      |
 *                              |                                     |           |
 *                              |                                     |           |
 *                          onSuccess()                           onSuccess()     |
 *                              |                                     |           |
 *                              |                                     |           |
 *                        ---------------------------------------------------------
 *                                             onFinally()
 *                          </p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午5:56.</p>
 */
public abstract class AbsCallbackHandler<Parser_Type> implements OkCallback{
    final private static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool(Util.threadFactory("OkHttp", false));

    final public static int     IO_EXCEPTION_CODE   = -1;
    final public static String  DEFAULT_CHARSET     = "UTF-8";

    private static final int SUCCESS_MESSAGE    = 0;
    private static final int FAILURE_MESSAGE    = 1;
    private static final int START_MESSAGE      = 2;
    private static final int FINISH_MESSAGE     = 3;
    private static final int PROGRESS_MESSAGE   = 4;
    private static final int CANCEL_MESSAGE     = 5;
    private static final int FINALLY_MESSAGE    = 6;

    private OkRequest request;
    private String responseCharset = DEFAULT_CHARSET;
    private boolean isCanceled;
    private boolean isFinished;

    private ThreadMode mThreadMode = ThreadMode.MAIN;
    private Handler mainThreadPoster;
    private Handler sendingThreadPoster;

    /** Working thread depends on {@link #mThreadMode}, default UI. */
    abstract protected void onSuccess(Parser_Type data, OkResponse response);
    /** Working thread depends on {@link #mThreadMode}, default UI. */
    abstract protected void onFailure(OkResponse response, Throwable throwable);
    /** Work on the request thread, that is okhttp thread. */
    abstract protected Parser_Type backgroundParser(OkResponse response) throws Exception;

    /** Working thread depends on {@link #mThreadMode}, default UI. */
    protected void onStart(){}
    /** Working thread depends on {@link #mThreadMode}, default UI. */
    protected void onCancel(){}
    /** Working thread depends on {@link #mThreadMode}, default UI. */
    protected void onProgress(int bytesWritten, int bytesTotal){}
    /** Working thread depends on {@link #mThreadMode}, default UI. */
    protected void onFinish(){}
    /** Working thread depends on {@link #mThreadMode}, default UI. */
    protected void onFinally(){}


    @Override final public void onFailure(Call call, IOException e) {
        if (call.isCanceled()){
            sendCancelMessage();
            return;
        }
        sendFinishMessage();

        OkRequest requestRef = request;
        OkResponse okResponse = MResponse.builder()
                .response(new Response.Builder()
                        .request(requestRef.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(AbsCallbackHandler.IO_EXCEPTION_CODE)
                        .message(e.getMessage())
                        .build())
                .request(requestRef)
                .builder();
        sendFailureMessage(okResponse, e);
        sendFinallyMessage();
    }

    @Override final public void onResponse(Call call, Response response) throws IOException {
        if (call.isCanceled()){
            sendCancelMessage();
            return;
        }
        sendFinishMessage();

        OkRequest requestRef = request;
        if (response.isSuccessful()) {
            try {
                OkResponse okResponse = MResponse.builder().response(response).request(requestRef).builder();
                Parser_Type data = backgroundParser(okResponse);
                sendSuccessMessage(data, okResponse);
            } catch (Exception e) {
                sendFailureMessage(MResponse.builder().response(response).request(requestRef).builder(), new ParserException());
            }
        } else {
            sendFailureMessage(MResponse.builder().response(response).request(requestRef).builder(), new ResponseFailException());
        }
        sendFinallyMessage();
    }

    private static class ResponderHandler extends Handler {
        private final AbsCallbackHandler mResponder;

        ResponderHandler(AbsCallbackHandler mResponder) {
            super(Looper.getMainLooper());
            this.mResponder = mResponder;
        }

        ResponderHandler(AbsCallbackHandler mResponder, Looper looper) {
            super(looper);
            this.mResponder = mResponder;
        }

        @Override public void handleMessage(Message msg) {
            mResponder.handleMessage(msg);
        }
    }

    public AbsCallbackHandler(){
        isCanceled = false;
        isFinished = false;
    }

    @Override final public void setRequest(OkRequest request) {
        this.request = request;
        mThreadMode = request.callbackThreadMode();
        if (mThreadMode == ThreadMode.SENDING){
            if (Looper.myLooper() == null){
                throw new RuntimeException("The Looper of the current thread is null, please call Looper.prepare() on your thread.");
            }
            sendingThreadPoster = new ResponderHandler(this, Looper.myLooper());
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

    final protected OkRequest getRequest(){
        return this.request;
    }

    /*package*/ final void sendProgressMessage(int bytesWritten, int bytesTotal) {
        sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[]{bytesWritten, bytesTotal}));
    }

    /*package*/ final void sendSuccessMessage(Parser_Type data, OkResponse response) {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{data, response}));
    }

    /*package*/ final void sendFailureMessage(OkResponse response, @Nullable Throwable throwable) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{response, throwable}));
    }

    @Override public final void sendStartMessage() {
        sendMessage(obtainMessage(START_MESSAGE, null));
    }

    /*package*/ final void sendFinishMessage() {
        sendMessage(obtainMessage(FINISH_MESSAGE, null));
    }

    /*package*/ final void sendFinallyMessage() {
        sendMessage(obtainMessage(FINALLY_MESSAGE, null));
    }

    /*package*/ final void sendCancelMessage() {
        sendMessage(obtainMessage(CANCEL_MESSAGE, null));
    }

    @SuppressWarnings("unchecked") private void handleMessage(Message message){
        Object[] responseObject;
        switch (message.what){
            case SUCCESS_MESSAGE:
                responseObject = (Object[]) message.obj;
                if (responseObject != null && responseObject.length != 0 && !isCanceled){
                    onSuccess((Parser_Type) responseObject[0], (OkResponse) responseObject[1]);
                }
                break;
            case FAILURE_MESSAGE:
                responseObject = (Object[]) message.obj;
                if (responseObject != null && responseObject.length == 2 && !isCanceled) {
                    onFailure((OkResponse) responseObject[0], (Throwable) responseObject[1]);
                }
                break;
            case START_MESSAGE:
                onStart();
                break;
            case FINISH_MESSAGE:
                this.isFinished = true;
                onFinish();
                break;
            case FINALLY_MESSAGE:
                onFinally();
                break;
            case PROGRESS_MESSAGE:
                responseObject = (Object[]) message.obj;
                if (responseObject != null && responseObject.length == 2) {
                    try {
                        onProgress((Integer) responseObject[0], (Integer) responseObject[1]);
                    } catch (Throwable t) {
                        //Silent
                    }
                }
                break;
            case CANCEL_MESSAGE:
                this.isCanceled = true;
                onCancel();
                break;
        }
    }

    private Handler mainThreadPoster(){
        if (mainThreadPoster == null){
            mainThreadPoster = new ResponderHandler(this);
        }

        return mainThreadPoster;
    }

    private void sendMessage(final Message msg) {
        if (msg == null || Thread.currentThread().isInterrupted()) {
            return;
        }
        switch (mThreadMode){
            case SENDING:
                sendingThreadPoster.sendMessage(msg);
                break;
            case MAIN:
                mainThreadPoster().sendMessage(msg);
                break;
            case BACKGROUND:
                if (msg.what == START_MESSAGE){
                    DEFAULT_EXECUTOR_SERVICE.execute(new Runnable() {
                        @Override public void run() {
                            handleMessage(msg);
                        }
                    });
                    return;
                }
                handleMessage(msg);
                break;
        }
    }

    private Message obtainMessage(int responseMessageId, Object responseMessageData) {
        switch (mThreadMode){
            case SENDING:
                return Message.obtain(sendingThreadPoster, responseMessageId, responseMessageData);
            case MAIN:
                return Message.obtain(mainThreadPoster(), responseMessageId, responseMessageData);
            case BACKGROUND:
                Message background = Message.obtain();
                background.what = responseMessageId;
                background.obj = responseMessageData;
                return background;
        }
        return null;
    }

}
