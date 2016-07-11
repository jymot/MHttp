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

import im.wangchao.mhttp.internal.exception.ParserException;
import im.wangchao.mhttp.internal.exception.ResponseFailException;
import okhttp3.Call;
import okhttp3.Response;

/**
 * <p>Description  : AbsResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午5:56.</p>
 */
public abstract class AbsCallbackHandler<Parser_Type> implements OkCallback{

    final public static int     IO_EXCEPTION_CODE   = -1;
    final public static String  DEFAULT_CHARSET     = "UTF-8";

    private static final int SUCCESS_MESSAGE    = 0;
    private static final int FAILURE_MESSAGE    = 1;
    private static final int START_MESSAGE      = 2;
    private static final int FINISH_MESSAGE     = 3;
    private static final int PROGRESS_MESSAGE   = 4;
    private static final int CANCEL_MESSAGE     = 5;

    private OkRequest request;
    private String responseCharset = DEFAULT_CHARSET;
    private boolean isCanceled;
    private boolean isFinished;

    final private Handler handler;

    /** Work on UI Thread */
    abstract protected void onSuccess(Parser_Type data, OkResponse response);
    /** Work on UI Thread */
    abstract protected void onFailure(OkResponse response, Throwable throwable);
    /** Work on Work Thread */
    abstract protected Parser_Type backgroundParser(OkResponse response) throws Exception;

    /** Work on UI Thread */
    protected void onStart(){}
    /** Work on UI Thread */
    protected void onCancel(){}
    /** Work on UI Thread */
    protected void onProgress(int bytesWritten, int bytesTotal){}
    /** Work on UI Thread */
    protected void onFinish(){}

    @Override final public void onFailure(Call call, IOException e) {
        if (call.isCanceled()){
            sendCancelMessage();
            return;
        }
        sendFinishMessage();

        OkRequest requestRef = request;
        OkResponse okResponse = MResponse.builder()
                .response(new Response.Builder().code(AbsCallbackHandler.IO_EXCEPTION_CODE).message(e.getMessage()).build())
                .request(requestRef)
                .builder();
        sendFailureMessage(okResponse, e);
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
    }

    private static class ResponderHandler extends Handler {
        private final AbsCallbackHandler mResponder;

        ResponderHandler(AbsCallbackHandler mResponder) {
            super(Looper.getMainLooper());
            this.mResponder = mResponder;
        }

        @Override public void handleMessage(Message msg) {
            mResponder.handleMessage(msg);
        }
    }

    public AbsCallbackHandler(){
        isCanceled = false;
        isFinished = false;
        handler = new ResponderHandler(this);
    }

    @Override final public void setRequest(OkRequest request) {
        this.request = request;
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

    private void sendMessage(Message msg) {
        if (!Thread.currentThread().isInterrupted()) {
            handler.sendMessage(msg);
        }
    }

    private Message obtainMessage(int responseMessageId, Object responseMessageData) {
        return Message.obtain(handler, responseMessageId, responseMessageData);
    }

}
