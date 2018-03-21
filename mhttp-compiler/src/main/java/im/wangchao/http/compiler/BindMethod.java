package im.wangchao.http.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import im.wangchao.http.annotations.Post;
import im.wangchao.http.annotations.Tag;

/**
 * <p>Description  : InjectMethod.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 下午7:25.</p>
 */
/*package*/ class BindMethod {

    private final TypeMirror returnType;
    private final List<? extends VariableElement> arguments;
    private final String methodName;

    private String      httpMethod;
    private String      url;
    private Object      tag;
    private int         timeout;
    private String[]    headers;

    private BindClass injectClass;

    /*package*/ BindMethod(BindClass injectClass, ExecutableElement executableElement) {
        this.injectClass = injectClass;
        String methodName = executableElement.getSimpleName().toString();
        TypeMirror returnType = executableElement.getReturnType();
        List<? extends VariableElement> arguments = executableElement.getParameters();

        this.returnType = returnType;
        this.arguments  = arguments;
        this.methodName = methodName;

        Post post = executableElement.getAnnotation(Post.class);
        im.wangchao.http.annotations.Get get  = executableElement.getAnnotation(im.wangchao.http.annotations.Get.class);
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

    MethodSpec brewMethod() throws Exception{
        MethodSpec.Builder result = MethodSpec.methodBuilder(methodName);
        result.addModifiers(Modifier.PUBLIC);

        //build return type
        TypeKind returnTypeKind = returnType.getKind();
        switch (returnTypeKind) {
            case DECLARED: {
                result.returns(TypeName.get(Class.forName("im.wangchao.mhttp.Request")));
            } break;
            case VOID: {
                result.returns(void.class);
            } break;
            default: {
                throw new Exception("other types are not supported");
            }
        }

        Map<String, String> parameterNameMap = new LinkedHashMap<>();
        //tag object
        String tagObjVariableName = null;
        String callbackVariableName = null;

        VariableElement variableElement;
        for (int i = 0, len = arguments.size(); i < len; i++){
            variableElement = arguments.get(i);
            TypeMirror type = variableElement.asType();
            String variableName = variableElement.getSimpleName().toString();

            im.wangchao.http.annotations.Callback callback = variableElement.getAnnotation(im.wangchao.http.annotations.Callback.class);
            Tag t = variableElement.getAnnotation(Tag.class);

            if (callback != null){
                callbackVariableName = variableName;
            } else if (t != null){
                tagObjVariableName = variableName;
            } else {
                parameterNameMap.put(variableName, variableName);
            }

            result.addParameter(TypeName.get(type), variableName);
        }

        buildFunctionBody(result, parameterNameMap, returnTypeKind, callbackVariableName, tagObjVariableName);

        return result.build();
    }

    private void buildFunctionBody(MethodSpec.Builder result, Map<String, String> parameters, TypeKind returnTypeKind, String callbackVariableName, String tagObjVariableName) throws Exception{
        addRequestParams(result, parameters);
        addHeaders(result);
        addRequest(result, returnTypeKind, callbackVariableName, tagObjVariableName);
    }

    private void addRequestParams(MethodSpec.Builder result, Map<String, String> parameters) throws Exception{
        // RequestParams 参数 key
        for (Map.Entry<String, String> parameter: parameters.entrySet()) {
            String name = parameter.getKey();
            result.addStatement("final String FIELD_" + name.toUpperCase() + " = $S", name);
        }

        ClassName requestParamsClass = ClassName.bestGuess("im.wangchao.mhttp.RequestParams");
        // 请求参数
        if (injectClass.getParamMethod() != null
                && injectClass.getParamMethod().length() != 0){
            result.addStatement("$T params = new $T(" + injectClass.getParamMethod() + "())", requestParamsClass, requestParamsClass);
        } else {
            result.addStatement("$T params = new $T()", requestParamsClass, requestParamsClass);
        }

        for (Map.Entry<String, String> parameter: parameters.entrySet()) {
            String parameterName = parameter.getKey();
            String variableName = parameter.getValue();

            result.addStatement("params.put(FIELD_" + parameterName.toUpperCase() + ", " + variableName + ")");
        }
    }

    private void addHeaders(MethodSpec.Builder result) throws Exception{
        ClassName headersBuilderClass = ClassName.bestGuess("okhttp3.Headers.Builder");

        result.addStatement("$T headerBuilder = new $T()", headersBuilderClass, headersBuilderClass);

        //Header
        if (this.headers != null){
            int len = headers.length;
            if (len % 2 != 0){
                throw new IllegalArgumentException("Supplied arguments must be even.");
            }
            for (int i = 0; i < len; i += 2){
                String key = String.valueOf(headers[i]);
                String val = String.valueOf(headers[i + 1]);
                result.addStatement("headerBuilder.add($S, $S)", key, val);
            }
        }
        if (this.injectClass.getHeaders() != null){
            Map<String, Set<String>> defaultHeaders = this.injectClass.getHeaders();
            for (Map.Entry<String, Set<String>> header : defaultHeaders.entrySet()){
                String key = header.getKey();
                Set<String> values = header.getValue();
                for (String val : values){
                    result.addStatement("headerBuilder.add($S, $S)", key, val);
                }
            }
        }
    }

    private void addRequest(MethodSpec.Builder result, TypeKind returnTypeKind, String callbackVariableName, String tagObjVariableName) throws Exception{
        ClassName requestBuilderClass = ClassName.bestGuess("im.wangchao.mhttp.Request.Builder");

//        Class<?> requestBuilderClass = Class.forName("im.wangchao.mhttp.Request$Builder");
        result.addStatement("$T builder = new $T()", requestBuilderClass, requestBuilderClass);

        result.addStatement("builder.requestParams(params)");
        result.addStatement("builder.headers(headerBuilder.build())");

        if (injectClass.getContentType() != null &&
                injectClass.getContentType().length() != 0){
            result.addStatement("builder.contentType($S)", injectClass.getContentType());
        }

        if (this.url.contains("://")){
            result.addStatement("builder.url($S)", url);
        } else {
            result.addStatement("builder.url($S)", this.injectClass.getRootURL().concat(this.url));
        }

        // 不处理 timeout
//        ClassName mHttpClass = ClassName.bestGuess("im.wangchao.mhttp.MHttp");
//        if (this.timeout != 0){
//            result.addStatement("$T.instance().timeout(" + this.timeout + ")", mHttpClass);
//        } else {
//            result.addStatement("$T.instance().timeout(" + this.injectClass.getDefaultTimeout() + ")", mHttpClass);
//        }

        if (tagObjVariableName != null){
            result.addStatement("builder.tag(" + tagObjVariableName + ")");
        } else if (this.tag != null && this.tag.toString().length() != 0){
            result.addStatement("builder.tag($S)", this.tag);
        }

        //method
        if (this.httpMethod != null) {
            result.addStatement("builder.method($S)", this.httpMethod);
        }

        if (callbackVariableName != null) {
            result.addStatement("builder.callback(" + callbackVariableName + ")");
        }

        switch (returnTypeKind){
            case DECLARED:{
                result.addStatement("return builder.build()");
            } break;
            case VOID:{
                result.addStatement("builder.build().enqueue()");
            } break;
        }
    }

}
