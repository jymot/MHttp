package im.wangchao.mhttp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * <p>Description  : AbsResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午5:56.</p>
 */
public abstract class AbsResponseHandler implements Callback{

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            $Accept.ACCEPT_JSON, $Accept.ACCEPT_TEXT, $Accept.ACCEPT_DATA, $Accept.ACCEPT_IMAGE, $Accept.ACCEPT_FILE
    })
    public @interface Accept{}
    public interface $Accept{
        String ACCEPT_JSON  = "application/json;charset=utf-8";
        String ACCEPT_TEXT  = "text/html;charset=utf-8";
        String ACCEPT_DATA  = "application/octet-stream";
        String ACCEPT_IMAGE = "image/png,image/jpeg,image/*";
        String ACCEPT_FILE = "application/octet-stream";
    }

    final public static int     IO_EXCEPTION_CODE   = -1;
    final public static String  DEFAULT_CHARSET     = "UTF-8";

    final private static int BUFFER_SIZE = 4096;

    private static final int SUCCESS_MESSAGE    = 0;
    private static final int FAILURE_MESSAGE    = 1;
    private static final int START_MESSAGE      = 2;
    private static final int FINISH_MESSAGE     = 3;
    private static final int PROGRESS_MESSAGE   = 4;
    private static final int CANCEL_MESSAGE     = 5;

    private HttpRequest request;
    private HttpResponse response;
    private String responseAccept = $Accept.ACCEPT_JSON;
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
        Log.e("wcwcwc", "onFailure" + e.getMessage() , e);
        sendFinishMessage();
        sendFailureMessage(responseWrapper(request, AbsResponseHandler.IO_EXCEPTION_CODE, e.getMessage(), new Headers.Builder().build(), new byte[0], null), e);
    }

    @Override final public void onResponse(Call call, Response response) throws IOException {
        sendFinishMessage();

        if (response.isSuccessful()) {
            Headers headers = parseOkHeader(response.headers());
            if (FileResponseHandler.class.isInstance(this)) {
                File file = ((FileResponseHandler) this).getFile();
                writeFile(response, file);
                sendSuccessMessage(responseWrapper(request, response.code(), response.message(), headers, new byte[0], file));
            } else {
                sendSuccessMessage(responseWrapper(request, response.code(), response.message(), headers, response.body().bytes(), null));
            }
        } else {
            okhttp3.Headers okHeaders = response.headers();
            Headers headers = parseOkHeader(okHeaders);
            sendFailureMessage(responseWrapper(request, response.code(), response.message(), headers, response.body().bytes(), null), null);
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
     * @return response accept
     */
    protected String accept(){
        return responseAccept;
    }

    final public AbsResponseHandler setResponseAccept(@NonNull String accept){
        this.responseAccept = accept;
        return this;
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

    private HttpResponse responseWrapper(HttpRequest request,
                                         int code,
                                         String codeMessage,
                                         Headers headers,
                                         byte[] body,
                                         File file){
        HttpResponse.Builder builder = new HttpResponse.Builder();
        builder.request(request)
                .code(code)
                .header(headers)
                .body(body)
                .message(codeMessage)
                .bodyFile(file);
        return builder.build();
    }

    /**
     * parse OkHeader to Headers {@link Headers}
     */
    private Headers parseOkHeader(@NonNull okhttp3.Headers okHeaders){
        Headers.Builder builder = new Headers.Builder();

        Map<String, List<String>> okHeadersMap = okHeaders.toMultimap();
        List<String> values;
        for (Map.Entry<String, List<String>> head : okHeadersMap.entrySet()) {
            values = head.getValue();
            if (values.isEmpty()) {
                continue;
            }
            for (int i = 0; i < values.size(); i++) {
                builder.add(head.getKey(), values.get(i));
            }
        }
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
