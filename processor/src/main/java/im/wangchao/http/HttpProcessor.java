package im.wangchao.http;

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

import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * <p>Description  : HttpProcessor.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/17.</p>
 * <p>Time         : 下午4:24.</p>
 */
public class HttpProcessor extends AbstractProcessor{
    public static final String SUFFIX = "$$HttpInjector";
    private static final List<Class<? extends Annotation>> METHOD_ANNOTATION = Arrays.asList(
            Post.class,
            Get.class
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

        supportTypes.add(RootURL.class.getCanonicalName());
        supportTypes.add(Timeout.class.getCanonicalName());
        supportTypes.add(RequestContentType.class.getCanonicalName());
        supportTypes.add(CommonParams.class.getCanonicalName());
        supportTypes.add(Header.class.getCanonicalName());
        supportTypes.add(CommonParamsMethod.class.getCanonicalName());

        return supportTypes;
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, InjectClass> targetClassMap = findAndParseTargets(roundEnv);

        for (Map.Entry<TypeElement, InjectClass> entry : targetClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            InjectClass bindingClass = entry.getValue();

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

    private Map<TypeElement, InjectClass> findAndParseTargets(RoundEnvironment env){
        Map<TypeElement, InjectClass> targetClassMap = new LinkedHashMap<>();


        for (Class<? extends Annotation> method : METHOD_ANNOTATION){
            findAndParseMethod(env, method, targetClassMap);
        }

        return targetClassMap;
    }

    //解析默认值
    private void parseOther(RoundEnvironment env, InjectClass injectClass){
        // @RootURL element.
        for (Element element : env.getElementsAnnotatedWith(RootURL.class)) {
            try {
                String url = element.getAnnotation(RootURL.class).value();
                injectClass.setRootURL(url);
            } catch (Exception e) {
                logParsingError(element, RootURL.class, e);
            }
        }
        // @Timeout element.
        for (Element element : env.getElementsAnnotatedWith(Timeout.class)) {
            try {
                int timeout = element.getAnnotation(Timeout.class).value();
                injectClass.setDefaultTimeout(timeout);
            } catch (Exception e) {
                logParsingError(element, Timeout.class, e);
            }
        }
        // @CommonParams element.
        for (Element element : env.getElementsAnnotatedWith(CommonParams.class)) {
            try {
                String params = element.getAnnotation(CommonParams.class).value();
                String key = element.getSimpleName().toString();
                injectClass.addParams(key, params);
            } catch (Exception e) {
                logParsingError(element, CommonParams.class, e);
            }
        }
        // @Header element.
        for (Element element : env.getElementsAnnotatedWith(Header.class)) {
            try {
                String[] header = element.getAnnotation(Header.class).value();
                String key = element.getSimpleName().toString();

                Set<String> values = new HashSet<>();
                for (int i = 0; i < header.length; i++){
                    values.add(header[i]);
                }

                key = key.replace("_", "-");

                injectClass.addHeader(key, values);
            } catch (Exception e) {
                logParsingError(element, Header.class, e);
            }
        }
        // @RequestContentType element.
        for (Element element : env.getElementsAnnotatedWith(RequestContentType.class)) {
            try {
                String contentType = element.getAnnotation(RequestContentType.class).value();
                injectClass.setContentType(contentType);
            } catch (Exception e) {
                logParsingError(element, RequestContentType.class, e);
            }
        }
        // @RequestParams element.
        for (Element element : env.getElementsAnnotatedWith(CommonParamsMethod.class)) {
            try {
                if (!(element instanceof ExecutableElement) || element.getKind() != METHOD) {
                    throw new IllegalStateException(
                            String.format("@%s annotation must be on a method.", CommonParamsMethod.class.getSimpleName()));
                }

                ExecutableElement executableElement = (ExecutableElement) element;
                injectClass.setParamMethod(executableElement.getSimpleName().toString());
            } catch (Exception e) {
                logParsingError(element, CommonParamsMethod.class, e);
            }
        }

    }

    private void findAndParseMethod(RoundEnvironment env,
                                    Class<? extends Annotation> annotationClass,
                                    Map<TypeElement, InjectClass> targetClassMap){
        for (Element element : env.getElementsAnnotatedWith(annotationClass)) {
            try {
                parseMethodAnnotation(annotationClass, env, element, targetClassMap);
            } catch (Exception e) {
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                error(element, "Unable to bind request for @%s.\n\n%s",
                        annotationClass.getSimpleName(), stackTrace.toString());
            }
        }
    }

    private void parseMethodAnnotation(Class<? extends Annotation> annotationClass,
                                       RoundEnvironment roundEnvironment,
                                       Element element,
                                       Map<TypeElement, InjectClass> targetClassMap) throws Exception{
        if (!(element instanceof ExecutableElement) || element.getKind() != METHOD) {
            throw new IllegalStateException(
                    String.format("@%s annotation must be on a method.", annotationClass.getSimpleName()));
        }

        ExecutableElement executableElement = (ExecutableElement) element;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        //BindingClass
        InjectClass injector = getOrCreateTargetClass(targetClassMap, enclosingElement);
        parseOther(roundEnvironment, injector);

        InjectMethod methodInjector = new InjectMethod(injector, executableElement);
        injector.addMethod(methodInjector);
    }

    private InjectClass getOrCreateTargetClass(Map<TypeElement, InjectClass> targetClassMap, TypeElement enclosingElement) {
        InjectClass injector = targetClassMap.get(enclosingElement);
        if (injector == null) {
            String targetType = enclosingElement.getQualifiedName().toString();
            String classPackage = getPackageName(enclosingElement);
            String className = getClassName(enclosingElement, classPackage) + SUFFIX;

            TypeMirror elementType = enclosingElement.asType();
            boolean isInterface = isInterface(elementType);

            injector = new InjectClass(classPackage, className, targetType, isInterface);
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
}
