package im.wangchao.mhttpdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import im.wangchao.mhttp.HttpClientManager;
import im.wangchao.mhttp.HttpRequest;
import im.wangchao.mhttp.HttpResponse;
import im.wangchao.mhttp.ResponseHandlerHook;
import im.wangchao.mhttp.TextResponseHandler;

public class MainActivity extends AppCompatActivity implements ResponseHandlerHook{
    final static String TAG = MainActivity.class.getSimpleName();
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        HttpClientManager.getSingleInstance().setResponseHandlerHook(this);

    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url("http://www.baidu.com");
        builder.method(HttpRequest.Method.GET);
        builder.responseHandler(new TextResponseHandler() {
            @Override public void onSuccess(String text, HttpResponse response) {
                Log.e(TAG, text);
            }
        });
        builder.build().execute();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ProgressDialog progressDialog;

    @Override
    public boolean doStart() {
        progressDialog = ProgressDialog.show(this, "Title", "Message");
        return false;
    }

    @Override
    public boolean doSuccess(HttpResponse response) {
        return false;
    }

    @Override
    public boolean doFailure(HttpResponse response) {
        return false;
    }

    @Override
    public boolean doProgress(int bytesWritten, int bytesTotal) {
        return false;
    }

    @Override
    public boolean doCancel() {
        return false;
    }

    @Override
    public boolean doFinish() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        return false;
    }
}
