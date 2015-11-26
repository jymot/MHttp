package im.wangchao.mhttp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * <p>Description  : AbsResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午5:56.</p>
 */
public abstract class AbsResponseHandler {
    public enum ResponseDataType{
        JSON("application/json;charset=utf-8"),
        TEXT("text/html;charset=utf-8"),
        DATA("application/octet-stream"),
        IMAGE("image/png,image/jpeg,image/*"),
        FILE("application/octet-stream"){
            private File file;
            @Override
            public void set(Object o){
                if (o instanceof File){
                    this.file = (File) o;
                    return;
                }
                throw new IllegalArgumentException("Argument must instance of File");
            }
            @Override
            public File get(){
                return file;
            }
        };

        private Object o;
        private String accept;
        ResponseDataType(String accept){
            this.accept = accept;
        }
        public String accept(){
            return accept;
        }
        public void set(Object o){
            this.o = o;
        }
        public Object get(){
            return o;
        }
    }

    final public static int     IO_EXCEPTION_CODE   = -1;
    final public static String  DEFAULT_CHARSET     = "UTF-8";

    private static final int SUCCESS_MESSAGE    = 0;
    private static final int FAILURE_MESSAGE    = 1;
    private static final int START_MESSAGE      = 2;
    private static final int FINISH_MESSAGE     = 3;
    private static final int PROGRESS_MESSAGE   = 4;
    private static final int CANCEL_MESSAGE     = 5;

    private HttpRequest request;
    private HttpResponse response;
    private ResponseDataType responseDataType = ResponseDataType.JSON;
    private String responseCharset = DEFAULT_CHARSET;
    private boolean isCanceled;
    private boolean isFinished;

    final private Handler handler;

    abstract protected void onSuccess(HttpResponse response);
    abstract protected void onFailure(HttpResponse response, @Nullable Throwable throwable);

    protected void onStart(){}
    protected void onFinish(){}
    protected void onCancel(){}
    protected void onProgress(int bytesWritten, int bytesTotal){}

    private static class ResponderHandler extends Handler {
        private final AbsResponseHandler mResponder;

        ResponderHandler(AbsResponseHandler mResponder) {
            this.mResponder = mResponder;
        }

        @Override
        public void handleMessage(Message msg) {
            mResponder.handleMessage(msg);
        }
    }

    public AbsResponseHandler(){
        this(true);
    }

    public AbsResponseHandler(boolean useHandler){
        isCanceled = false;
        isFinished = false;
        if (useHandler){
            handler = new ResponderHandler(this);
        } else {
            if (Looper.myLooper() == null){
                throw new RuntimeException("Can't create handler inside thread that has not called Looper.prepare()");
            }
            handler = null;
        }
    }

    final public AbsResponseHandler setRequest(@NonNull HttpRequest request){
        this.request = request;
        return this;
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

    final protected String getCharset() {
        return this.responseCharset.isEmpty() ? DEFAULT_CHARSET : this.responseCharset;
    }

    final public AbsResponseHandler setResponseDataType(@NonNull ResponseDataType type){
        responseDataType = type;
        return this;
    }

    final protected void print(String message){
        Log.d(AbsResponseHandler.class.getSimpleName(), message);
    }

    @Nullable
    final protected String byteArrayToString(byte[] bytes){
        try {
            return bytes == null ? null : new String(bytes, getCharset());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public ResponseDataType getResponseDataType(){
        return responseDataType;
    }

    @NonNull
    final public HttpRequest getRequest(){
        return this.request;
    }

    @NonNull
    final public HttpResponse getResponse(){
        return this.response;
    }

    /*package*/ final void sendProgressMessage(int bytesWritten, int bytesTotal) {
        sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[]{bytesWritten, bytesTotal}));
    }

    /*package*/ final void sendSuccessMessage(HttpResponse response) {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{response}));
    }

    /*package*/ final void sendFailureMessage(HttpResponse response, @Nullable Throwable throwable) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{response, throwable}));
    }

    /*package*/ final void sendStartMessage() {
        sendMessage(obtainMessage(START_MESSAGE, null));
    }

    /*package*/ final void sendFinishMessage() {
        sendMessage(obtainMessage(FINISH_MESSAGE, null));
    }

    /*package*/ final void sendCancelMessage() {
        sendMessage(obtainMessage(CANCEL_MESSAGE, null));
    }

    private void handleMessage(Message message){
        Object[] responseObject;
        switch (message.what){
            case SUCCESS_MESSAGE:
                responseObject = (Object[]) message.obj;
                if (responseObject != null && responseObject.length != 0 && !isCanceled){
                    this.response = (HttpResponse) responseObject[0];
                    onSuccess((HttpResponse) responseObject[0]);
                }
                break;
            case FAILURE_MESSAGE:
                responseObject = (Object[]) message.obj;
                if (responseObject != null && responseObject.length == 2 && !isCanceled) {
                    this.response = (HttpResponse) responseObject[0];
                    onFailure((HttpResponse) responseObject[0], (Throwable) responseObject[1]);
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
                        //
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
        if (handler == null) {
            handleMessage(msg);
        } else if (!Thread.currentThread().isInterrupted()) {
            handler.sendMessage(msg);
        }
    }

    private Message obtainMessage(int responseMessageId, Object responseMessageData) {
        Message msg;
        if (handler == null) {
            msg = Message.obtain();
            if (msg != null) {
                msg.what = responseMessageId;
                msg.obj = responseMessageData;
            }
        } else {
            msg = Message.obtain(handler, responseMessageId, responseMessageData);
        }
        return msg;
    }

}
