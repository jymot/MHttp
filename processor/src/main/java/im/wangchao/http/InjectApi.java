package im.wangchao.http;

/**
 * <p>Description  : InjectApi.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/19.</p>
 * <p>Time         : 上午8:23.</p>
 */
public class InjectApi {

    public static <T> T inject(Class<T> type) {
        String name = type.getCanonicalName() + HttpProcessor.SUFFIX;
        T obj = null;
        try {
            obj = (T)Class.forName(name).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }


}
