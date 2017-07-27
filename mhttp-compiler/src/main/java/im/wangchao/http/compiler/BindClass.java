package im.wangchao.http.compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * <p>Description  : BindingClass.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/18.</p>
 * <p>Time         : 上午7:56.</p>
 */
/*package*/ class BindClass {
    private final String classPackage;
    public final String className;
    private final TypeMirror superClassType;
    private final boolean isInterface;
    private final Set<BindMethod> methods = new LinkedHashSet<>();

    //默认 timeout
    private int         defaultTimeout = 30;
    //
    private String      rootURL = "";
    //
    private String      contentType;
    //
    private Map<String, Set<String>> headers = new HashMap<>();
    //
    private String paramMethod = "";

    /*package*/ BindClass(String classPackage, String className, TypeMirror superClassType, boolean isInterface) {
        this.classPackage = classPackage;
        this.className = className;
        this.superClassType = superClassType;
        this.isInterface = isInterface;
    }

    String getParamMethod() {
        return paramMethod;
    }

    void setParamMethod(String paramMethod) {
        this.paramMethod = paramMethod;
    }

    int getDefaultTimeout() {
        return defaultTimeout;
    }

    void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    String getRootURL() {
        return rootURL;
    }

    void setRootURL(String rootURL) {
        this.rootURL = rootURL;
    }

    String getContentType() {
        return contentType;
    }

    void setContentType(String contentType) {
        this.contentType = contentType;
    }

    Map<String, Set<String>> getHeaders() {
        return headers;
    }

    void addHeader(String key, Set<String> values) {
        headers.put(key, values);
    }

    void addMethod(BindMethod e) {
        methods.add(e);
    }

    public String getFqcn() {
        return classPackage + "." + className;
    }

    JavaFile brewJava(TypeElement typeElement) throws Exception{
        return JavaFile.builder(classPackage, createType(typeElement))
                .addFileComment("Generated code from mhttp. Do not modify!").build();
    }

    private TypeSpec createType(TypeElement typeElement) throws Exception{
        TypeSpec.Builder result = TypeSpec.classBuilder(className)
                .addOriginatingElement(typeElement)
                .addModifiers(Modifier.PUBLIC);

        if (this.isInterface) {
            result.addSuperinterface(TypeName.get(superClassType));
        } else {
            result.superclass(TypeName.get(superClassType));
        }

        for (BindMethod methodInjector : methods) {
            result.addMethod(methodInjector.brewMethod());
        }

        return result.build();
    }
}
