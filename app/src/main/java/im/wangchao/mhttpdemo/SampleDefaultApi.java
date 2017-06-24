package im.wangchao.mhttpdemo;

import java.util.HashMap;
import java.util.Map;

import im.wangchao.http.annotations.CommonParamsMethod;
import im.wangchao.http.annotations.Header;
import im.wangchao.http.annotations.RequestContentType;
import im.wangchao.http.annotations.RootURL;
import im.wangchao.http.annotations.Timeout;

import static im.wangchao.mhttp.body.MediaTypeUtils.APPLICATION_JSON;

/**
 * <p>Description  : SampleDefaultApi.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/12/4.</p>
 * <p>Time         : 下午5:54.</p>
 */
public abstract class SampleDefaultApi {

    @RootURL("https://www.baidu.com/") String baseURL;
    @Timeout(40) String timeout;
    @RequestContentType(APPLICATION_JSON) String Content_Type;
    @Header("Android") String User_Agent;

    @CommonParamsMethod
    public Map<String, String> getCommonParams() {
        Map<String, String> params = new HashMap<>();
        // TODO: 15/12/4 Common params
        return params;
    }
}
