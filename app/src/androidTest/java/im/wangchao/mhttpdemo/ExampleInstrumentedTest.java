package im.wangchao.mhttpdemo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import im.wangchao.mhttp.Request;
import im.wangchao.mhttp.Response;
import im.wangchao.mhttp.callback.TextCallbackHandler;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("im.wangchao.mhttpdemo", appContext.getPackageName());
    }

    @Test
    public void post() {
        Request.builder().post()
                .url("https://www.baidu.com")
                .callback(new TextCallbackHandler(){
                    @Override protected void onSuccess(String data, Response response) {
                        Log.e("wcwcwc", data);
                    }

                    @Override protected void onFailure(Response response, Throwable throwable) {
                        Log.e("wcwcwc", "error");
                    }
                }).build().enqueue();
    }
}
