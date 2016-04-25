package im.wangchao.mhttp;

import android.content.Context;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * <p>Description  : FileResponseHandler.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午2:39.</p>
 */
public class FileResponseHandler extends AbsResponseHandler {
    final private File file;

    public FileResponseHandler(Context context){
        this.file = getTempFile(context);
    }

    public FileResponseHandler(File file){
        this.file = file;
    }

    protected File getFile(){
        return file;
    }

    @Override final protected void onSuccess(HttpResponse response) {
        onSuccess(file, response);
    }

    @Override protected void onFailure(HttpResponse response, @Nullable Throwable throwable) {

    }

    @Override protected String accept() {
        return Accept.ACCEPT_FILE;
    }

    public void onSuccess(File file, HttpResponse response){

    }

    private File getTempFile(Context context){
        try {
            return File.createTempFile("temp", "_handled", context.getCacheDir());
        } catch (IOException e) {
            return null;
        }
    }
}
