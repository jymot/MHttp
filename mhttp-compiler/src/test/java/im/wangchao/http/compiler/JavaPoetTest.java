package im.wangchao.http.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import javax.lang.model.element.Modifier;

/**
 * <p>Description  : JavaPoetTest.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 2017/7/26.</p>
 * <p>Time         : 下午2:17.</p>
 */
public class JavaPoetTest {

    @Test
    public void mainTest(){
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .addStatement("$T list = new $T()", ArrayList.class, ArrayList.class)
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();

        try {
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ParameterizedTypeName.get(ClassName.get("im.wangchao.http.compiler", "Test"));
        ParameterizedTypeName.get(ClassName.get("im.wangchao.http.compiler", "TT"));

    }
}
