package im.wangchao.mhttp;

/**
 * <p>Description  : HttpClientInterface.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/17.</p>
 * <p>Time         : 下午1:18.</p>
 */
public interface HttpClientInterface {
    /**
     * 执行请求
     *
     * @param httpRequest   请求对象
     * @return              this
     */
    HttpClientInterface execute(HttpRequest httpRequest);

    /**
     * 取消请求
     *
     * @param tag       Request Tag
     * @return          this
     */
    HttpClientInterface cancel(Object tag);

}
