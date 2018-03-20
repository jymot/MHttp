package im.wangchao.mhttp;

/**
 * <p>Description  : Converter.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 2018/3/20.</p>
 * <p>Time         : 下午10:26.</p>
 */
public interface Converter<R, T> {

    R apply(T t) throws Exception;
}
