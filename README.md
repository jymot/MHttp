# MHttp
okhttp wrapper
### Gradle:
```gradle
  compile 'im.wangchao:mhttp:1.0.0'
```
###PROGUARD
```java
    -keep class im.wangchao.** { *; }
    -dontwarn im.wangchao.**
    -keep class **$$HttpBinder { *; }
    -keepclasseswithmembernames class * {
        @im.wangchao.* <fields>;
    }
    -keepclasseswithmembernames class * {
        @im.wangchao.* <methods>;
    }
    -keepclassmembers class * implements java.io.Serializable {  
        static final long serialVersionUID;  
        private static final java.io.ObjectStreamField[] serialPersistentFields;  
        !static !transient <fields>;  
        private void writeObject(java.io.ObjectOutputStream);  
        private void readObject(java.io.ObjectInputStream);  
        java.lang.Object writeReplace();  
        java.lang.Object readResolve();  
    }
```
###How to use

