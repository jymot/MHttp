package im.wangchao.mhttp.adapter.rxjava2;

import java.io.IOException;

import im.wangchao.mhttp.Request;
import im.wangchao.mhttp.Response;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * <p>Description  : ResponseExecuteObservable.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 2018/3/19.</p>
 * <p>Time         : 下午4:46.</p>
 */
public class ResponseExecuteObservable<T> extends Observable<T> {
    private final Request request;

    ResponseExecuteObservable(Request request){
        this.request = request;
    }

    @Override protected void subscribeActual(Observer<? super T> observer) {
        try {
            Response response = request.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final class ExecuteDisposable implements Disposable {

        @Override public void dispose() {

        }

        @Override public boolean isDisposed() {
            return false;
        }
    }
}
