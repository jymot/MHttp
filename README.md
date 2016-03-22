# MHttp
okhttp wrapper
### Gradle:
```gradle
  compile 'im.wangchao:mhttp:0.4.1'
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
#####simple
```java

    public abstract class SampleApi {
        public static SampleApi instance() {
            return HttpManager.bind(SampleApi.class);
        }

        @Get(url = "https://www.baidu.com/")
        public abstract void baidu(@Callback TextResponseHandler callback);

        @Post(url = "s")
        public abstract HttpRequest search(String wd,
                                           @Callback TextResponseHandler callback);
    }
    
```
```java

  public class TestActivity extends Activity{
      
      private void invokeTestApi(){
        //auto execute
        SampleApi.instance().baidu(new TextResponseHandler() {
                            @Override
                            public void onSuccess(String text, HttpResponse response) {
                                super.onSuccess(text, response);
                                log("onSuccess: " + text);
                            }
        
                            @Override
                            protected void onFinish() {
                                super.onFinish();
                                log("onFinish");
                            }
                        });
      }
      
      private void invokeTestApi2(){
        HttpRequest request = SimpleApi.instance().search(new TextResponseHandler(){
            @Override
            public void onSuccess(String text, HttpResponse response){
            }
        });
        
        //execute
        request.execute();
      }
    
  }
  
```

#####common settings
```java

    public abstract class SampleDefaultApi {

        @RootURL("https://www.baidu.com/") String baseURL;
        @Timeout(40) String timeout;
        @RequestContentType(RequestParams.APPLICATION_JSON) String Content_Type;
        @Header("Android") String User_Agent;

        @CommonParamsMethod
        public Map<String, String> getCommonParams() {
            Map<String, String> params = new HashMap<>();
            // TODO: 15/12/4 Common params
            return params;
        }
    } 
    

    public abstract class SampleApi extends SampleDefaultApi{
        public static SampleApi instance() {
            return HttpManager.bind(SampleApi.class);
        }

        @Get(url = "https://www.baidu.com/")
        public abstract void baidu(@Callback TextResponseHandler callback);

        @Post(url = "s")
        public abstract HttpRequest search(String wd,
                                           @Callback TextResponseHandler callback);
    }
  
```

#####normal
```java
    HttpRequest.builder()
                .get()
                .url("http://www.cninfo.com.cn/finalpage/2014-12-13/1200461869.PDF")
                .responseHandler(new FileResponseHandler(MainActivity.this) {
                    @Override
                    public void onSuccess(File file, HttpResponse response) {
                        super.onSuccess(file, response);
                        log("file len: " + file.length() + ", file exists: " + file.exists() + " , path:" + file.getPath());
                    }

                    @Override
                    protected void onFinish() {
                        super.onFinish();
                        log("onFinish");
                    }

                    @Override
                    protected void onProgress(int bytesWritten, int bytesTotal) {
                        super.onProgress(bytesWritten, bytesTotal);
                        log("onProgress : " + bytesWritten + " -- " + bytesTotal);
                    }
                }).build().execute();

```

###
link [网络请求适配性封装](http://wangchao.im/2015/11/22/mhttpadapter-post.html)
