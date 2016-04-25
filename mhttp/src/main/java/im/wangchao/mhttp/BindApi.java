package im.wangchao.mhttp;


/**
 * <p>Description  : BindApi.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/19.</p>
 * <p>Time         : 上午8:23.</p>
 */
final class BindApi {
    private static final String SUFFIX = "$$HttpBinder";

    @SuppressWarnings("unchecked") public static <T> T bind(Class<T> type) {
        String name = type.getCanonicalName() + SUFFIX;
        T obj = null;
        try {
            obj = (T)Class.forName(name).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }


}
