# MHttpAdapter
任意第三方网络请求库的适配性封装
### Gradle:
```gradle
  compile 'im.wangchao:mhttpadapter:0.3.0'
```
###How to use
#####simple
```java
  public abstract class SimpleApi{
  
    public static SimpleApi instance() {
      return HttpRequest.inject(SimpleApi.class);
    }
  
    @Post(url="http://test.com", timeout=40, tag="tag", heads = {"key", "value"})
    public abstract void testApi(String key0,
                                 String key1,
                                 @Callback JSONResponseHandler callback);
                                 
    @GET(url="https://www.baidu.com")
    public abstract HttpRequest testApi2(@Callback JSONResponseHandler callback);
    
  }
```
```java
  public class TestActivity extends Activity{
      
      private void invokeTestApi(){
        SimpleApi.instance().testApi("value0", "value1", new JSONResponseHandler(){
        
              public void onSuccess(JSONObject jsonObject, HttpResponse response){
              }
              
        });
        //auto execute
      }
      
      private void invokeTestApi2(){
        HttpRequest request = SimpleApi.instance().testApi2(new JSONResponseHandler(){
        
            public void onSuccess(JSONObject jsonObject, HttpResponse response){
            }
            
        });
        
        //execute
        request.execute();
      }
    
  }
```

#####common settings
```java
  public abstract class BaseApi{
  
    @RootURL("http://root.com/") String baseURL;
    @Timeout(40) String timeout;
    @RequestContentType(RequestParams.APPLICATION_FORM) String Content_Type;
    @Header("Android") String User_Agent;
        
    @CommonParamsMethod
    public Map<String, String> getCommonParams() {
      Map<String, String> params = new HashMap<>();
      params.put("key", "value");
      return params;
    }
  }
  
  public abstract class SimpleApi extends BaseApi{
  
      @Post(url="test")
      public abstract void testApi(@Callback TextResponseHandler callback);
      
  }
```
###
link [网络请求适配性封装](http://wangchao.im/2015/11/22/mhttpadapter-post.html)
