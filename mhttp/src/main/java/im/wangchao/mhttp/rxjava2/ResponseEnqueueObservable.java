package im.wangchao.mhttp.rxjava2;

import im.wangchao.mhttp.AbsCallbackHandler;
import im.wangchao.mhttp.Request;
import im.wangchao.mhttp.Response;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * <p>Description  : ResponseEnqueueObservable.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 2018/3/19.</p>
 * <p>Time         : 下午4:45.</p>
 */
public class ResponseEnqueueObservable<R> extends Observable<R> {
    private final Request request;

    public ResponseEnqueueObservable(Request request){
        this.request = request;
    }

    @Override protected void subscribeActual(Observer<? super R> observer) {

//        AbsCallbackHandler<R> absCallbackHandler = (AbsCallbackHandler<R>) request.callback();
//        EnqueueDisposable<R> disposable = new EnqueueDisposable<>(observer, absCallbackHandler);
//
//        observer.onSubscribe(disposable);
//        request.newBuilder().callback(disposable).build().enqueue();
    }

    private static final class EnqueueDisposable<T> extends AbsCallbackHandler<T> implements Disposable{
        private final AbsCallbackHandler<T> originCallback;
        private final Observer<? super T> observable;
        private volatile boolean disposed;
        boolean terminated = false;

        EnqueueDisposable(Observer<? super T> observer, AbsCallbackHandler<T> callbackHandler){
            this.observable = observer;
            this.originCallback = callbackHandler;
        }

        @Override public void dispose() {
            disposed = true;
            Request request = getRequest();
            request.cancel();
        }

        @Override public boolean isDisposed() {
            return disposed;
        }

        @Override protected void onSuccess(T data, Response response) {

        }

        @Override protected void onFailure(Response response, Throwable throwable) {

        }
    }
}
