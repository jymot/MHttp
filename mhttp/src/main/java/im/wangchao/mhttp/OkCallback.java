package im.wangchao.mhttp;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * <p>Description  : OkCallback.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/3.</p>
 * <p>Time         : 上午10:16.</p>
 */
public interface OkCallback extends Callback {
    OkCallback EMPTY = new OkCallback() {
        @Override public void initialize() {
        }

        @Override public void sendStartMessage() {
        }

        @Override public void setRequest(OkRequest request) {
        }

        @Override public String accept() {
            return Accept.EMPTY;
        }

        @Override public void onFailure(Call call, IOException e) {
        }

        @Override public void onResponse(Call call, Response response) throws IOException {
        }
    };

    /**
     * Initialize the callback.
     */
    void initialize();

    /**
     * Send message when the request starts to send
     */
    void sendStartMessage();

    /**
     * Set request
     */
    void setRequest(OkRequest request);

    /**
     * Request accept
     */
    String accept();

}
