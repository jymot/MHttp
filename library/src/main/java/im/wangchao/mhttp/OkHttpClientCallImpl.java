package im.wangchao.mhttp;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import timber.log.Timber;

/**
 * <p>Description  : OkHttpClientImpl.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午1:23.</p>
 */
/*package*/ class OkHttpClientCallImpl implements IHttpCall {
    final private static MediaType MEDIA_TYPE_STREAM = MediaType.parse(RequestParams.APPLICATION_OCTET_STREAM);
    final private static int BUFFER_SIZE = 4096;

    private AbsResponseHandler responseHandler;
    private String url;
    private WeakReference<Call> weakCall;

    final private OkHttpClient httpClient;
    final private Request.Builder requestBuilder;
    final private HttpRequest httpRequest;

    public OkHttpClientCallImpl(HttpRequest request, OkHttpClient httpClient){
        this.httpRequest     = request;
        this.requestBuilder  = new Request.Builder();
        this.httpClient      = httpClient;
        init(request);
    }

    private void init(HttpRequest httpRequest){
        setUrl(httpRequest.getRequestUrl());
        setTimeout(httpRequest.getTimeout());
        setHeaders(httpRequest.getHeaders());
        setRequestParams(httpRequest.getRequestParams(), httpRequest.getMethod());
        setResponseHandler(httpRequest);
        setTag(httpRequest.getTag());
    }

    @Override public IHttpCall execute() {

        switch (httpRequest.getMethod()){
            case HttpRequest.Method.GET: {
                get(httpRequest);
            } break;
            case HttpRequest.Method.POST: {
                post(httpRequest);
            } break;
        }
        return this;

    }

    @Override public IHttpCall cancel() {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final Call call = weakCall == null ? null : weakCall.get();
                if (call != null) {
                    call.cancel();
                }

                if (responseHandler != null) {
                    responseHandler.sendCancelMessage();
                }
            }
        };

        if (Looper.myLooper() == Looper.getMainLooper()){
            new Thread(r).start();
        } else {
            r.run();
        }

        return this;
    }

    private IHttpCall setTag(Object tag) {
        if (tag == null || TextUtils.isEmpty(tag.toString())){
            String millis = String.valueOf(System.currentTimeMillis());
            tag = TextUtils.isEmpty(url) ? millis : url.concat(millis);
        }
        requestBuilder.tag(tag);
        return this;
    }

    private IHttpCall setUrl(String url) {
        this.url = url;
        requestBuilder.url(url);
        return this;
    }

    private IHttpCall setTimeout(int second) {
        if (second <= 0){
            return this;
        }
        httpRequest.getHttpClient().setTimeout(second, TimeUnit.SECONDS);
        return this;
    }

    private IHttpCall setHeaders(Headers headers) {
        com.squareup.okhttp.Headers okHeaders = com.squareup.okhttp.Headers.of(headers.namesAndValues());
        requestBuilder.headers(okHeaders);
        return this;
    }

    private IHttpCall setRequestParams(RequestParams params, String method) {
        if (params == null){
            return this;
        }
        switch (method){
            case HttpRequest.Method.GET:{
                try {
                    requestBuilder.url(url.concat("?").concat(params.formatURLParams()));
                } catch (UnsupportedEncodingException e) {
                    Timber.e(e, e.getMessage());
                }
            } break;
            case HttpRequest.Method.POST:{
                if (params.isJSON()){
                    requestBuilder.method(method, createJsonRequestBody(params));
                }
                if (params.isForm()){
                    requestBuilder.method(method, createFormRequestBody(params));
                }
                if (params.isMultipart()){
                    requestBuilder.method(method, createMultiPartRequestBody(params));
                }
            }break;
        }
        return this;
    }

    private IHttpCall setResponseHandler(HttpRequest request) {
        if (request == null){
            return this;
        }
        this.responseHandler = request.getResponseHandler();
        this.responseHandler.setRequest(request);
        requestBuilder.addHeader("Accept", responseHandler.getResponseDataType().accept());
        return this;
    }

    private IHttpCall post(final HttpRequest httpRequest) {
        Request okRequest   = requestBuilder.build();
        Call    call        = httpClient.newCall(okRequest);
        weakCall = new WeakReference<>(call);

        if (responseHandler != null){
            responseHandler.sendStartMessage();
        }

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (responseHandler != null) {
                    responseHandler.sendFailureMessage(responseWrapper(httpRequest, AbsResponseHandler.IO_EXCEPTION_CODE, e.getMessage(), new Headers.Builder().build(), new byte[0]), e);
                    responseHandler.sendFinishMessage();
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (responseHandler != null) {
                        if (responseHandler.getResponseDataType() == AbsResponseHandler.ResponseDataType.FILE) {
                            writeFile(responseHandler, response, (File) responseHandler.getResponseDataType().get());
                        }
                        Headers headers = parseOkHeader(response.headers());
                        responseHandler.sendSuccessMessage(responseWrapper(httpRequest, response.code(), response.message(), headers, response.body().bytes()));
                    }
                } else {
                    if (responseHandler != null) {
                        com.squareup.okhttp.Headers okHeaders = response.headers();
                        Headers headers = parseOkHeader(okHeaders);
                        responseHandler.sendFailureMessage(responseWrapper(httpRequest, response.code(), response.message(), headers, response.body().bytes()), null);
                    }
                }

                if (responseHandler != null) {
                    responseHandler.sendFinishMessage();
                }
            }
        });
        return this;
    }

    private IHttpCall get(HttpRequest httpRequest) {
        //okHttp post 和 get调用方式类似所以直接调用 post，已经在设置参数时设置了请求方法
        post(httpRequest);
        return this;
    }

    /**
     * 包装 HttpResponse
     */
    private HttpResponse responseWrapper(HttpRequest request,
                                         int code,
                                         String codeMessage,
                                         Headers headers,
                                         byte[] body){
        HttpResponse.Builder builder = new HttpResponse.Builder();
        builder.request(request)
                .code(code)
                .header(headers)
                .body(body)
                .message(codeMessage);
        return builder.build();
    }

    /**
     * write file , send progress message
     */
    private void writeFile(AbsResponseHandler handler, Response response, File file) throws IOException {
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

                    handler.sendProgressMessage(count, (int) contentLength);
                }
            } finally {
                Util.closeQuietly(instream);
                buffer.flush();
                Util.closeQuietly(buffer);
            }
        }
    }

    /**
     * parse OkHeader to Headers {@link Headers}
     */
    private Headers parseOkHeader(@NonNull com.squareup.okhttp.Headers okHeaders){
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

    /**
     * create JSON request body
     */
    private RequestBody createJsonRequestBody(RequestParams requestParams){
        MediaType mediaType = MediaType.parse(RequestParams.APPLICATION_JSON + "; charset=" + requestParams.getContentEncoding());
        return RequestBody.create(mediaType, requestParams.parseJSON());
    }

    /**
     * create form request body
     */
    private RequestBody createFormRequestBody(RequestParams requestParams){
        FormEncodingBuilder builder= new FormEncodingBuilder();
        Map<String, String> form = requestParams.getUrlParams();
        for (Map.Entry<String, String> entry: form.entrySet()){
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    /**
     * create stream request body
     */
    private RequestBody createStreamBody(@NonNull final String contentType,@NonNull final InputStream inputStream){
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(contentType);
            }

            @Override
            public long contentLength() throws IOException {
                try {
                    return inputStream.available();
                } catch (Exception e){
                    return super.contentLength();
                }
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }

    /**
     * create multi part request body
     */
    private RequestBody createMultiPartRequestBody(RequestParams requestParams){
        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.FORM);

        //form
        Map<String, String> form = requestParams.getUrlParams();
        for (Map.Entry<String, String> entry: form.entrySet()){
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }

        //stream
        Map<String, RequestParams.StreamWrapper> stream = requestParams.getStreamParams();
        for (Map.Entry<String, RequestParams.StreamWrapper> streamEntry: stream.entrySet()){
            builder.addPart(
                    com.squareup.okhttp.Headers.of("Content-Disposition",
                            String.format("form-data;name=\"%s\";filename=\"%s\"", streamEntry.getKey(), streamEntry.getValue().name),
                            "Content-Transfer-Encoding", "binary"),
                    createStreamBody(streamEntry.getValue().contentType, streamEntry.getValue().inputStream)
                    );
        }

        //file
        Map<String, RequestParams.FileWrapper> files = requestParams.getFileParams();
        for (Map.Entry<String, RequestParams.FileWrapper> file: files.entrySet()){
            builder.addPart(RequestBody.create(MediaType.parse(file.getValue().contentType), file.getValue().file));
        }

        return builder.build();
    }

}
