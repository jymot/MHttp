package im.wangchao.mhttp;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

/**
 * <p>Description  : Cookie.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/8/25.</p>
 * <p>Time         : 下午8:42.</p>
 */
public class Cookie {
    private volatile static CookieManager cookieManager;
    private Cookie(){}

    public static CookieManager instanceManager(){
        if (cookieManager == null){
            synchronized (Cookie.class){
                if (cookieManager == null){
                    cookieManager = new CookieManager();
                    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                }
            }
        }

        return cookieManager;
    }

    public static List<HttpCookie> getCookies(CookieManager manager){
        CookieStore cookieStore = manager.getCookieStore();
        return cookieStore.getCookies();
    }
}
