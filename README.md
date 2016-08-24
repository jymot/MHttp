# MHttp
okhttp wrapper for Android Http.
###Gradle:
```gradle
  compile 'im.wangchao:mhttp:1.3.5'
```
###PROGUARD
ProGuard rules now ship inside of the library and are included automatically.
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
####1.Normal Get
```java
     MRequest.builder().url("https://www.baidu.com")
                    .callback(new TextCallbackHandler(){
                        @Override protected void onSuccess(String data, OkResponse response) {
                            Log.e(MainActivity.TAG, data);
                        }
                    })
                    .build()
                    .send();
```
####2.Normal Post
```java
    MRequest.builder().url("url")
                    .addHeader("key", "value")
                    .addParameter("key", "value")
                    .callback(new JSONCallbackHandler(){
                        @Override protected void onSuccess(JSON data, OkResponse response) {
                            //TODO
                        }
                    })
                    .build()
                    .send();
```
####3.Annotation (Support Post and Get)
Turns your HTTP API into a Java interface or abstract Class.
```java
    public interface GetBaidu{
            @Get(url = "https://www.baidu.com")
           void baidu(@Callback TextCallbackHandler callback);
    }

    public interface PostApi{

           @Post(url = "http://www.baidu.com")
           MRequest postRequest(String param0, String param1, @Callback TextCallbackHandler callback);

           @Post(url = "http://www.baidu.com")
           void autoExecuteRequest(String param0, String param1, @Callback TextCallbackHandler callback);
    }
```

The MHttp class generates an implementation of this interface(abstract).
```java
    GetBaidu api = MHttp.create(GetBaidu.class);
    api.baidu(new TextCallbackHandler(){
              @Override protected void onSuccess(String data, OkResponse response) {
                  Log.e(MainActivity.TAG, data);
              }
    });

    //auto send
    PostApi api = MHttp.create(PostApi.class);
    api.autoExecuteRequest("aa", "bb", new TextCallbackHandler(){
             @Override protected void onSuccess(String data, OkResponse response) {
                 //Todo
             }
    );

    //obtain request
    PostApi api = MHttp.create(PostApi.class);
    MRequest request =  api.postRequest("aa", "bb", new TextCallbackHandler(){
             @Override protected void onSuccess(String data, OkResponse response) {
                 //Todo
             }
    });
    request.send();
```

Automatically generated classes when you use the Annotation.
```java

/**
 * Implementation GetBaidu interface
 */
public class GetExample$GetBaidu$$HttpBinder implements GetBaidu {
    public GetExample$GetBaidu$$HttpBinder() {
    }

    public void baidu(TextCallbackHandler callback) {
        RequestParams params = new RequestParams();
        Builder builder = new Builder();
        builder.requestParams(params);
        builder.url("https://www.baidu.com");
        MHttp.instance().timeout(30);
        okhttp3.Headers.Builder headerBuilder = new okhttp3.Headers.Builder();
        builder.headers(headerBuilder.build());
        builder.method("GET");
        builder.callback(callback);
        builder.build().send();
    }
}

/**
 * Implementation PostApi interface
 */
public class PostExample$PostApi$$HttpBinder implements PostApi {
    public PostExample$PostApi$$HttpBinder() {
    }

    public MRequest postRequest(String param0, String param1, TextCallbackHandler callback) {
        String FIELD_PARAM0 = "param0";
        String FIELD_PARAM1 = "param1";
        RequestParams params = new RequestParams();
        params.put("param0", param0);
        params.put("param1", param1);
        Builder builder = new Builder();
        builder.requestParams(params);
        builder.url("http://www.baidu.com");
        MHttp.instance().timeout(30);
        okhttp3.Headers.Builder headerBuilder = new okhttp3.Headers.Builder();
        builder.headers(headerBuilder.build());
        builder.method("POST");
        builder.callback(callback);
        return builder.build();
    }

    public void autoExecuteRequest(String param0, String param1, TextCallbackHandler callback) {
        String FIELD_PARAM0 = "param0";
        String FIELD_PARAM1 = "param1";
        RequestParams params = new RequestParams();
        params.put("param0", param0);
        params.put("param1", param1);
        Builder builder = new Builder();
        builder.requestParams(params);
        builder.url("http://www.baidu.com");
        MHttp.instance().timeout(30);
        okhttp3.Headers.Builder headerBuilder = new okhttp3.Headers.Builder();
        builder.headers(headerBuilder.build());
        builder.method("POST");
        builder.callback(callback);
        builder.build().send();
    }
}
```

If you want to set default configuration. You can create a Java abstract Class.
```java
public abstract class SampleDefaultApi {

    @RootURL("https://www.baidu.com/") String baseURL;
    @Timeout(40) String timeout;
    @RequestContentType(RequestParams.APPLICATION_JSON) String Content_Type;
    @Header("Android") String User_Agent;

    @CommonParamsMethod public Map<String, String> getCommonParams() {
        Map<String, String> params = new HashMap<>();
        // TODO
        return params;
    }

}
```
And you can turns your HTTP API into a Java abstract Class, and extend this abstract Class.
Example below:
```java
public abstract class SampleApi extends SampleDefaultApi{
    public static SampleApi instance() {
        return MHttp.create(SampleApi.class);
    }

    @Get(url = "https://www.baidu.com/")
    public abstract void baidu(@Callback TextCallbackHandler callback);

    @Get(url = "s", tag = "aaa")
    public abstract MRequest search(String wd, @Callback TextCallbackHandler callback);

    @Get(url = "s", tag = "aaa")
    public abstract MRequest search1(String wd, @Callback TextCallbackHandler callback, @Tag Object a);
}

```
####4.Callback
Several common methods are provided. as follows:
```
JSONCallbackHandler
TextCallbackHandler
BinaryCallbackHandler
BitmapCallbackHandler
FileCallbackHandler
GSONCallbackHandler
```
If you want to customize the Callback Method, you can inherit AbsCallbackHandler like the method above.

####5.ThreadMode
 * SENDING -- Callback will be called in the same thread, which is sending the request.
 * MAIN -- Callback will be called in Android's main thread (UI thread).
 * BACKGROUND -- Callback will be called in a background thread. That is, work on the request thread(okhttp thread).

###Contact Me
- Email:  magician.of.technique@aliyun.com

### License

    Copyright 2016 Mot. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

