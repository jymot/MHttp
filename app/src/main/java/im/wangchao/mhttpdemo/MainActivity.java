package im.wangchao.mhttpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import im.wangchao.mhttp.Request;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    final static String TAG = "wcwcwc";
    Request request;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button doRequest = (Button) findViewById(R.id.doRequest);
        final Button cancel = (Button) findViewById(R.id.cancel);
        assert doRequest != null;
        assert cancel != null;
        doRequest.setOnClickListener(this);
        cancel.setOnClickListener(this);

        Cache.setCacheDir(this);

        //Get
//        GetExample.doNormalRequest();
//        GetExample.doAnnotationRequest();

        //Post
//        PostExample.doNormalPost();
//        PostExample.executeAnnotationPost();
//        MRequest request = PostExample.getRequest();
//        request.send();
    }

    private void log(String msg){
        Log.e(TAG, msg);
    }

    @Override public void onClick(View v) {
        switch (v.getId()){
            case R.id.doRequest:
                request = GetExample.doNormalRequest();
//                GetExample.doAnnotationRequest();
                break;
            case R.id.cancel:
                if (request != null){
//                    MHttp.instance().cancel(request.tag());
                    request.cancel();
                }
                break;
        }
    }
}
