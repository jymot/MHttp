//package im.wangchao.http;
//
//import com.squareup.javapoet.JavaFile;
//import com.squareup.javapoet.MethodSpec;
//import com.squareup.javapoet.TypeSpec;
//
//import java.io.IOException;
//
//import javax.lang.model.element.Modifier;
//
//public class MyClass {
//    public void testWrite(){
//        MethodSpec method = MethodSpec.methodBuilder("test")
//                .addModifiers(Modifier.PUBLIC)
//                .returns(void.class)
//                .addParameter(String.class, "str")
//                .addStatement("$T.out.println(str)", System.class).build();
//
//        TypeSpec typeSpec = TypeSpec.classBuilder("HelloWorld")
//                .addModifiers(Modifier.PUBLIC)
//                .addMethod(method)
//                .build();
//
//        JavaFile javaFile = JavaFile.builder("", typeSpec).build();
//        try {
//            javaFile.writeTo(System.out);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
