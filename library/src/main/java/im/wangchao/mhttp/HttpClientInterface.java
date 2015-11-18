package im.wangchao.mhttp;

/**
 * <p>Description  : HttpClientInterface.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午1:18.</p>
 */
public interface HttpClientInterface {

    HttpClientInterface execute(HttpRequest httpRequest);

    HttpClientInterface cancel(HttpRequest request);

}
