package im.wangchao.http.compiler;

import com.google.auto.service.AutoService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import im.wangchao.http.annotations.CommonParams;
import im.wangchao.http.annotations.CommonParamsMethod;
import im.wangchao.http.annotations.Get;
import im.wangchao.http.annotations.Header;
import im.wangchao.http.annotations.Post;
import im.wangchao.http.annotations.RequestContentType;
import im.wangchao.http.annotations.RootURL;
import im.wangchao.http.annotations.Timeout;

import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * <p>Description  : HttpProcessor.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/17.</p>
 * <p>Time         : 下午4:24.</p>
 */
@AutoService(Processor.class)
public class HttpProcessor extends AbstractProcessor{
    public static final String SUFFIX = "$$HttpBinder";
    private static final List<Class<? extends Annotation>> METHOD_ANNOTATION = Arrays.asList(
            Post.class,
            Get.class
    );

    private static final List<Class<? extends Annotation>> COMMON_ANNOTATION = Arrays.asList(
            RootURL.class,
            Timeout.class,
            RequestContentType.class,
            CommonParams.class,
            Header.class,
            CommonParamsMethod.class
    );

    private Filer       filer;
    private Elements    elementUtils;
    private Types       typeUtils;

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementUtils    = processingEnv.getElementUtils();
        typeUtils       = processingEnv.getTypeUtils();
        filer           = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportTypes = new LinkedHashSet<>();
        for (Class<? extends Annotation> method : METHOD_ANNOTATION){
            supportTypes.add(method.getCanonicalName());
        }

        for (Class<? extends Annotation> method : COMMON_ANNOTATION){
            supportTypes.add(method.getCanonicalName());
        }

        return supportTypes;
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, BindClass> targetClassMap = findAndParseTargets(roundEnv);

        for (Map.Entry<TypeElement, BindClass> entry : targetClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindClass bindingClass = entry.getValue();

            try {
                JavaFileObject jfo = filer.createSourceFile(bindingClass.getFqcn(), typeElement);
                Writer writer = jfo.openWriter();
                writer.write(bindingClass.brewJava());
                writer.flush();
                writer.close();
            } catch (Exception e) {
                error(typeElement, "Unable to write view binder for type %s: %s", typeElement,
                        e.getMessage());
            }
        }
        return true;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private Map<TypeElement, BindClass> findAndParseTargets(RoundEnvironment env){
        Map<TypeElement, BindClass> targetClassMap = new LinkedHashMap<>();


        //method list
        for (Class<? extends Annotation> method : METHOD_ANNOTATION){
            findAndParseMethod(env, method, targetClassMap);
        }

        //add common
        for (TypeElement cls : targetClassMap.keySet()){
            //遍历该类的所有成员
            for (Element member :  elementUtils.getAllMembers(cls)){
                findAndParseCommon(env, targetClassMap.get(cls), member);
            }
        }


        return targetClassMap;
    }

    /**
     * 解析公共配置 Annotation
     */
    private void findAndParseCommon(RoundEnvironment env, BindClass injectClass, Element member){

        // @RootURL element.
        if (env.getElementsAnnotatedWith(RootURL.class).contains(member)) {
            try {
                String url = member.getAnnotation(RootURL.class).value();
                injectClass.setRootURL(url);
            } catch (Exception e) {
                logParsingError(member, RootURL.class, e);
            }
        }
        // @Timeout element.
        else if (env.getElementsAnnotatedWith(Timeout.class).contains(member)) {
            try {
                int timeout = member.getAnnotation(Timeout.class).value();
                injectClass.setDefaultTimeout(timeout);
            } catch (Exception e) {
                logParsingError(member, Timeout.class, e);
            }
        }
        // @CommonParams element.
        else if (env.getElementsAnnotatedWith(CommonParams.class).contains(member)) {
            try {
                String params = member.getAnnotation(CommonParams.class).value();
                String key = member.getSimpleName().toString();
                injectClass.addParams(key, params);
            } catch (Exception e) {
                logParsingError(member, CommonParams.class, e);
            }
        }
        // @Header element.
        else if (env.getElementsAnnotatedWith(Header.class).contains(member)) {
            try {
                String[] header = member.getAnnotation(Header.class).value();
                String key = member.getSimpleName().toString();

                Set<String> values = new HashSet<>();
                for (int i = 0; i < header.length; i++){
                    values.add(header[i]);
                }

                key = key.replace("_", "-");

                injectClass.addHeader(key, values);
            } catch (Exception e) {
                logParsingError(member, Header.class, e);
            }
        }
        // @RequestContentType element.
        else if (env.getElementsAnnotatedWith(RequestContentType.class).contains(member)) {
            try {
                String contentType = member.getAnnotation(RequestContentType.class).value();
                injectClass.setContentType(contentType);
            } catch (Exception e) {
                logParsingError(member, RequestContentType.class, e);
            }
        }
        // @RequestParams element.
        else if (env.getElementsAnnotatedWith(CommonParamsMethod.class).contains(member)) {
            try {
                if (!(member instanceof ExecutableElement) || member.getKind() != METHOD) {
                    throw new IllegalStateException(
                            String.format("@%s annotation must be on a method.", CommonParamsMethod.class.getSimpleName()));
                }

                ExecutableElement executableElement = (ExecutableElement) member;
                injectClass.setParamMethod(executableElement.getSimpleName().toString());
            } catch (Exception e) {
                logParsingError(member, CommonParamsMethod.class, e);
            }
        }

    }

    /**
     * 解析 Method {@link #METHOD_ANNOTATION}
     */
    private void findAndParseMethod(RoundEnvironment env,
                                    Class<? extends Annotation> annotationClass,
                                    Map<TypeElement, BindClass> targetClassMap){
        for (Element element : env.getElementsAnnotatedWith(annotationClass)) {
            try {
                parseMethodAnnotation(annotationClass, element, targetClassMap);
            } catch (Exception e) {
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                error(element, "Unable to bind request for @%s.\n\n%s",
                        annotationClass.getSimpleName(), stackTrace.toString());
            }
        }
    }

    /**
     * 解析请求方法注解,并生成对应的方法
     */
    private void parseMethodAnnotation(Class<? extends Annotation> annotationClass,
                                       Element element,
                                       Map<TypeElement, BindClass> targetClassMap) throws Exception{
        if (!(element instanceof ExecutableElement) || element.getKind() != METHOD) {
            throw new IllegalStateException(
                    String.format("@%s annotation must be on a method.", annotationClass.getSimpleName()));
        }

        ExecutableElement executableElement = (ExecutableElement) element;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        //BindingClass
        BindClass injector = getOrCreateTargetClass(targetClassMap, enclosingElement);

        BindMethod methodInjector = new BindMethod(injector, executableElement);
        injector.addMethod(methodInjector);
    }

    /**
     * 获取BindClass
     */
    private BindClass getOrCreateTargetClass(Map<TypeElement, BindClass> targetClassMap, TypeElement enclosingElement) {
        BindClass injector = targetClassMap.get(enclosingElement);
        if (injector == null) {
            String targetType = enclosingElement.getQualifiedName().toString();
            String classPackage = getPackageName(enclosingElement);
            String className = getClassName(enclosingElement, classPackage) + SUFFIX;

            TypeMirror elementType = enclosingElement.asType();
            boolean isInterface = isInterface(elementType);

            injector = new BindClass(classPackage, className, targetType, isInterface);
            targetClassMap.put(enclosingElement, injector);
        }
        return injector;
    }

    private boolean isInterface(TypeMirror typeMirror) {
        return (typeMirror instanceof DeclaredType) && ((DeclaredType) typeMirror).asElement().getKind() == INTERFACE;
    }

    /**
     * 获取类名
     */
    private String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    /**
     * 获取某个类型的包名
     */
    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation,
                                 Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    private void logMessage(String msg){
        processingEnv.getMessager().printMessage(NOTE, msg);
    }
}
