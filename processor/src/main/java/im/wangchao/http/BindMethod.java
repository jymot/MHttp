package im.wangchao.http;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * <p>Description  : InjectMethod.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午7:25.</p>
 */
public class BindMethod {

    private final TypeMirror returnType;
    private final List<? extends VariableElement> arguments;
    private final String methodName;

    private String      httpMethod;
    private String      url;
    private Object      tag;
    private int         timeout;
    private String[]    headers;

    private BindClass injectClass;

    public BindMethod(BindClass injectClass, ExecutableElement executableElement) {
        this.injectClass = injectClass;
        String methodName = executableElement.getSimpleName().toString();
        TypeMirror returnType = executableElement.getReturnType();
        List<? extends VariableElement> arguments = executableElement.getParameters();

        this.returnType = returnType;
        this.arguments  = arguments;
        this.methodName = methodName;

        Post post = executableElement.getAnnotation(Post.class);
        Get  get  = executableElement.getAnnotation(Get.class);
        if (post != null) {
            this.httpMethod = "POST";
            this.url = post.url();
            this.tag = post.tag();
            this.timeout = post.timeout();
            this.headers = post.heads();
        } else if (get != null) {
            this.httpMethod = "GET";
            this.url = get.url();
            this.tag = get.tag();
            this.timeout = get.timeout();
            this.headers = get.heads();
        }

    }

    public String brewJava() throws Exception{
        StringBuilder sb = new StringBuilder("@Override public ");

        //build return type
        TypeKind returnTypeKind = returnType.getKind();

        String responseListenerName = null;
        Map<String, String> parameterNameMap = new LinkedHashMap<>();

        switch (returnTypeKind) {
            case DECLARED: {
                sb.append("HttpRequest ");
            } break;
            case VOID: {
                sb.append("void ");
            } break;
            default: {
                throw new Exception("other types are not supported");
            }
        }

        sb.append(methodName + "(");
        boolean isFirst = true;

        for (VariableElement variableElement : arguments) {
//            DeclaredType type = (DeclaredType)variableElement.asType();
//            String typeName = type.asElement().toString();
            String typeName = variableElement.asType().toString();
            String variableName = variableElement.getSimpleName().toString();

            Callback callback = variableElement.getAnnotation(Callback.class);
            if (callback != null){
                responseListenerName = variableName;
            } else {
                parameterNameMap.put(variableName, variableName);
            }

            if (!isFirst) {
                sb.append(", ");
            }

            sb.append(typeName);
            sb.append(" " + variableName);

            if (isFirst) {
                isFirst = false;
            }
        }

        sb.append(") {\n");
        sb.append(buildFunctionBody(parameterNameMap, responseListenerName, returnTypeKind));
        sb.append("}\n");

        return sb.toString();
    }

    private String buildFunctionBody(Map<String, String> parameters, String responseListenerName, TypeKind returnTypeKind) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> parameter: parameters.entrySet()) {
            String name = parameter.getKey();

            sb.append(" final String FIELD_" + name.toUpperCase() + " = \"");
            sb.append(name);
            sb.append("\";\n");
        }

        //请求参数
        if (injectClass.getParamMethod() != null
                && injectClass.getParamMethod().length() != 0){
            sb.append("RequestParams params = new RequestParams(" + injectClass.getParamMethod() + "());\n");
        } else {
            sb.append("RequestParams params = new RequestParams();\n");
        }
//        //common
        if (injectClass.getCommonParams() != null){
            for (Map.Entry<String, String> parameter: injectClass.getCommonParams().entrySet()){
                String parameterName = parameter.getKey();
                String variableName = parameter.getValue();

                sb.append("params.put(\"" + parameterName + "\", ");
                sb.append("\"" + variableName + "\");\n");
            }
        }

        for (Map.Entry<String, String> parameter: parameters.entrySet()) {
            String parameterName = parameter.getKey();
            String variableName = parameter.getValue();

            sb.append("params.put(FIELD_" + parameterName.toUpperCase() + ", ");
            sb.append(variableName + ");\n");
        }

        if (injectClass.getContentType() != null &&
                injectClass.getContentType().length() != 0){
            sb.append("params.setContentType(\"" + injectClass.getContentType() + "\");\n");
        }

        //请求 builder
        sb.append("HttpRequest.Builder builder = new HttpRequest.Builder();\n");
        sb.append("builder.params(params);\n");

        if (this.url.contains("://")){
            sb.append("builder.url(\"" + this.url + "\");\n");
        } else {
            sb.append("builder.url(\"" + this.injectClass.getRootURL().concat(this.url) + "\");\n");
        }

        if (this.timeout != 0){
            sb.append("HttpManager.instance().timeout(" + this.timeout + ");\n");
        } else {
            sb.append("HttpManager.instance().timeout(" + this.injectClass.getDefaultTimeout() + ");\n");
        }

        if (this.tag != null){
            sb.append("builder.tag(\"" + this.tag + "\");\n");
        }

        //Header
        sb.append("Headers.Builder headerBuilder = new Headers.Builder();\n");
        if (this.headers != null){
            int len = headers.length;
            if (len % 2 != 0){
                throw new IllegalArgumentException("Supplied arguments must be even.");
            }
            for (int i = 0; i < len; i += 2){
                String key = String.valueOf(headers[i]);
                String val = String.valueOf(headers[i + 1]);
                sb.append("headerBuilder.add(\"" + key + "\", \"" + val + "\");\n");
            }
        }
        if (this.injectClass.getHeaders() != null){
            Map<String, Set<String>> defaultHeaders = this.injectClass.getHeaders();
            int len = defaultHeaders.size();
            for (Map.Entry<String, Set<String>> header : defaultHeaders.entrySet()){
                String key = header.getKey();
                Set<String> values = header.getValue();
                for (String val : values){
                    sb.append("headerBuilder.add(\"" + key + "\", \"" + val + "\");\n");
                }
            }
        }
        sb.append("builder.headers(headerBuilder.build());\n");

        //method
        if (this.httpMethod != null) {
            sb.append("builder.method(\"" + this.httpMethod + "\");\n");
        }

        if (responseListenerName != null) {
            sb.append("builder.responseHandler(" + responseListenerName + ");\n");
        }

        switch (returnTypeKind){
            case DECLARED:{
                sb.append("return builder.build();\n");
            } break;
            case VOID:{
                sb.append("builder.build().execute();\n");
            } break;
        }

        return sb.toString();
    }
}
