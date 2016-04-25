package im.wangchao.mhttp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import im.wangchao.mhttp.internal.exception.ResponseFailException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;

/**
 * <p>Description  : AbsResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午5:56.</p>
 */
public abstract class AbsResponseHandler implements Callback{


    final public static int     IO_EXCEPTION_CODE   = -1;
    final public static String  DEFAULT_CHARSET     = "UTF-8";

    final private static int BUFFER_SIZE = 4096;
    final private static byte[] EMPTY_BYTES = new byte[0];

    private static final int SUCCESS_MESSAGE    = 0;
    private static final int FAILURE_MESSAGE    = 1;
    private static final int START_MESSAGE      = 2;
    private static final int FINISH_MESSAGE     = 3;
    private static final int PROGRESS_MESSAGE   = 4;
    private static final int CANCEL_MESSAGE     = 5;

    private HttpRequest request;
    private HttpResponse response;
    private String responseCharset = DEFAULT_CHARSET;
    private boolean isCanceled;
    private boolean isFinished;

    final private Handler handler;

    abstract protected void onSuccess(HttpResponse response);
    abstract protected void onFailure(HttpResponse response, @Nullable Throwable throwable);

    protected void onStart(){}
    protected void onCancel(){}
    protected void onProgress(int bytesWritten, int bytesTotal){}
    protected void onFinish(){}

    @Override final public void onFailure(Call call, IOException e) {
        sendFinishMessage();
        sendFailureMessage(onFailureResponseWrapper(request, AbsResponseHandler.IO_EXCEPTION_CODE, e.getMessage()), e);
    }

    @Override final public void onResponse(Call call, Response response) throws IOException {
        sendFinishMessage();

        if (response.isSuccessful()) {
            if (FileResponseHandler.class.isInstance(this)) {
                File file = ((FileResponseHandler) this).getFile();
                writeFile(response, file);
                sendSuccessMessage(responseWrapper(request, response, file));
            } else {
                sendSuccessMessage(responseWrapper(request, response));
            }
        } else {
            sendFailureMessage(responseWrapper(request, response), new ResponseFailException());
        }
    }

    private static class ResponderHandler extends Handler {
        private final AbsResponseHandler mResponder;

        ResponderHandler(AbsResponseHandler mResponder) {
            this.mResponder = mResponder;
        }

        @Override public void handleMessage(Message msg) {
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

    /**
     * subclass can override this method to change charset.
     */
    protected String charset() {
        return TextUtils.isEmpty(responseCharset) ? DEFAULT_CHARSET : responseCharset;
    }

    /**
     * @return request accept
     */
    protected String accept(){
        return Accept.ACCEPT_DEFAULT;
    }

    final protected void print(String message){
        Log.d(AbsResponseHandler.class.getSimpleName(), message);
    }

    /**
     * write file , send progress message
     */
    protected void writeFile(Response response, File file) throws IOException {
        if (file == null){
            throw new IllegalArgumentException("File == null");
        }
        InputStream instream = response.body().byteStream();
        long contentLength = response.body().contentLength();
        FileOutputStream buffer = new FileOutputStream(file);
        if (instream != null) {
            try {
                byte[] tmp = new byte[BUFFER_SIZE];
                int l, count = 0;
                while ((l = instream.read(tmp)) != -1 && !Thread.currentThread().isInterrupted()) {
                    count += l;
                    buffer.write(tmp, 0, l);

                    sendProgressMessage(count, (int) contentLength);
                }
            } finally {
                Util.closeQuietly(instream);
                buffer.flush();
                Util.closeQuietly(buffer);
            }
        }
    }

    private HttpResponse responseWrapper(HttpRequest request, Response response) throws IOException {
        return responseWrapper(request, response, null);
    }

    private HttpResponse responseWrapper(HttpRequest request, Response response, File file) throws IOException {
        return responseWrapper(request, response, response.code(), response.message(), file);
    }

    private HttpResponse responseWrapper(HttpRequest request,
                                         Response response,
                                         int code,
                                         String codeMessage,
                                         File file) throws IOException {
        HttpResponse.Builder builder = new HttpResponse.Builder();
        final ResponseBody responseBody = response.body();
        //response non null and file is not empty, invoke responseBody.bytes()
        final byte[] bytes = responseBody != null && file == null ? responseBody.bytes() : null;
        builder.request(request)
                .code(code)
                .header(response.headers())
                .response(response)
                .message(codeMessage)
                .body(new HttpResponseBody(responseBody, bytes,file));
        return builder.build();
    }

    private HttpResponse onFailureResponseWrapper(HttpRequest request,
                                                  int code,
                                                  String codeMessage){
        HttpResponse.Builder builder = new HttpResponse.Builder();
        builder.request(request)
                .code(code)
                .header(response.headers())
                .message(codeMessage)
                .body(new HttpResponseBody(null, new byte[0], null));
        return builder.build();
    }

    @Nullable final protected String byteArrayToString(byte[] bytes){
        try {
            return bytes == null ? null : new String(bytes, charset());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    @NonNull final public HttpRequest getRequest(){
        return this.request;
    }

    @NonNull final public HttpResponse getResponse(){
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
                if (request != null){
                    HttpManager.instance().dequeue(request);
                }
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
                if (request != null){
                    HttpManager.instance().dequeue(request);
                }
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
