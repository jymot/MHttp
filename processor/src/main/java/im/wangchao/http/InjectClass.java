package im.wangchao.http;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Description  : BindingClass.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 上午7:56.</p>
 */
public class InjectClass {
    private final String classPackage;
    private final String className;
    private final String targetClass;
    private final boolean isInterface;
    private final Set<InjectMethod> methods;

    //公共参数
    private Map<String, String> commonParams = new HashMap<>();
    //默认 timeout
    private int         defaultTimeout = 0;
    //
    private String      rootURL = "";
    //
    private String      contentType;
    //
    private Map<String, Set<String>> headers = new HashMap<>();
    //
    private String paramMethod = "";

    public InjectClass(String classPackage, String className, String targetClass, boolean isInterface) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.isInterface = isInterface;
        this.methods = new LinkedHashSet<>();
    }

    public String getParamMethod() {
        return paramMethod;
    }

    public void setParamMethod(String paramMethod) {
        this.paramMethod = paramMethod;
    }

    public Map<String, String> getCommonParams() {
        return commonParams;
    }

    public void addParams(String key, String value) {
        this.commonParams.put(key, value);
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public String getRootURL() {
        return rootURL;
    }

    public void setRootURL(String rootURL) {
        this.rootURL = rootURL;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, Set<String>> getHeaders() {
        return headers;
    }

    public void addHeader(String key, Set<String> values) {
        headers.put(key, values);
    }

    public void addMethod(InjectMethod e) {
        methods.add(e);
    }

    public String getFqcn() {
        return classPackage + "." + className;
    }

    public String brewJava() throws Exception{
        StringBuilder builder = new StringBuilder("package " + this.classPackage + ";\n");
        builder.append("\n");
        builder.append("import im.wangchao.mhttp.AbsResponseHandler;\n");
        builder.append("import im.wangchao.mhttp.HttpRequest;\n");
        builder.append("import im.wangchao.mhttp.Headers;\n");
        builder.append("import im.wangchao.mhttp.RequestParams;\n");
        builder.append("\n");

        String action = this.isInterface ? "implements" : "extends";

        builder.append("public class " + this.className + " " + action + " " + this.targetClass + " {\n");

        for (InjectMethod methodInjector : methods) {
            builder.append(methodInjector.brewJava());
        }
        builder.append("}\n");
        return builder.toString();
    }
}
