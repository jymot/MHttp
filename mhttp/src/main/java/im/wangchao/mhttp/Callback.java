package im.wangchao.mhttp;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * <p>Description  : OkCallback.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/6/3.</p>
 * <p>Time         : 上午10:16.</p>
 */
public interface Callback extends okhttp3.Callback {
    int START_MESSAGE      = 0;
    int PROGRESS_MESSAGE   = 1;
    int SUCCESS_MESSAGE    = 2;
    int FAILURE_MESSAGE    = 3;
    int FINISH_MESSAGE     = 4;
    int FINALLY_MESSAGE    = 5;
    int CANCEL_MESSAGE     = 6;

    Callback EMPTY = new Callback() {
        @Override public void initialize(Request request) {}

        @Override public String accept() {
            return Accept.EMPTY;
        }

        @Override public void onFailure(Call call, IOException e) {}

        @Override public void onResponse(Call call, Response response) throws IOException {
            response.close();
        }
    };

    /**
     * Initialize the callback.
     */
    void initialize(Request request);

    /**
     * Request accept.
     */
    String accept();

}
