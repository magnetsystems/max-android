/*
 * Copyright (c) 2015 Magnet Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package retrofit;

import android.content.Context;
import android.util.Log;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.MaxModule;
import com.magnet.max.android.MaxRestAuthenticator;
import com.magnet.max.android.auth.AuthTokenProvider;
import com.magnet.max.android.connectivity.ConnectivityManager;
import com.magnet.max.android.rest.MagnetCallAdapter;
import com.magnet.max.android.rest.RequestInterceptor;
import com.magnet.max.android.rest.RequestManager;
import com.magnet.max.android.rest.RestConstants;
import com.magnet.max.android.rest.annotation.Timeout;
import com.magnet.max.android.rest.marshalling.MagnetGsonConverterFactory;
import com.magnet.max.android.util.StringUtil;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import retrofit.http.HTTP;
import retrofit.http.Header;

import static retrofit.Utils.checkNotNull;

/**
 * Adapts a Java interface to a REST API.
 * <p>
 * API endpoints are defined as methods on an interface with annotations providing metadata about
 * the form in which the HTTP call should be made.
 * <p>
 * The relative path for a given method is obtained from an annotation on the method describing
 * the request type. The built-in methods are {@link retrofit.http.GET GET},
 * {@link retrofit.http.PUT PUT}, {@link retrofit.http.POST POST}, {@link retrofit.http.PATCH
 * PATCH}, {@link retrofit.http.HEAD HEAD}, and {@link retrofit.http.DELETE DELETE}. You can use a
 * custom HTTP method with {@link HTTP @HTTP}.
 * <p>
 * Method parameters can be used to replace parts of the URL by annotating them with
 * {@link retrofit.http.Path @Path}. Replacement sections are denoted by an identifier surrounded
 * by curly braces (e.g., "{foo}"). To add items to the query string of a URL use
 * {@link retrofit.http.Query @Query}.
 * <p>
 * The body of a request is denoted by the {@link retrofit.http.Body @Body} annotation. The object
 * will be converted to request representation by a call to
 * {@link Converter#toBody(Object) toBody}
 * on the supplied {@link Converter} for this instance. A {@link RequestBody} can also be used
 * which will not use the {@code Converter}.
 * <p>
 * Alternative request body formats are supported by method annotations and corresponding parameter
 * annotations:
 * <ul>
 * <li>{@link retrofit.http.FormUrlEncoded @FormUrlEncoded} - Form-encoded data with key-value
 * pairs specified by the {@link retrofit.http.Field @Field} parameter annotation.
 * <li>{@link retrofit.http.Multipart @Multipart} - RFC 2387-compliant multi-part data with parts
 * specified by the {@link retrofit.http.Part @Part} parameter annotation.
 * </ul>
 * <p>
 * Additional static headers can be added for an endpoint using the
 * {@link retrofit.http.Headers @Headers} method annotation. For per-request control over a header
 * annotate a parameter with {@link Header @Header}.
 * <p>
 * By default, methods return a {@link retrofit.Call} which represents the HTTP request. The generic
 * parameter of the call is the response body type and will be converted by a call to
 * {@link Converter#fromBody(ResponseBody) fromBody} on the supplied {@link Converter} for
 * this instance. {@link ResponseBody} can also be used which will not use the {@code Converter}.
 * <p>
 * For example:
 * <pre>
 * public interface CategoryService {
 *   &#64;POST("/category/{cat}")
 *   Call&lt;List&lt;Item&gt;&gt; categoryList(@Path("cat") String a, @Query("page") int b);
 * }
 * </pre>
 * <p>
 * Calling {@link #create(Class) create()} with {@code CategoryService.class} will validate the
 * annotations and create a new implementation of the service definition.
 *
 * @author Bob Lee (bob@squareup.com)
 * @author Jake Wharton (jw@squareup.com)
 */
public class MagnetRestAdapter implements MaxModule, AuthTokenProvider {

  private static final String TAG = MagnetRestAdapter.class.getSimpleName();
  private static final int DEFAULT_CACHE_SIZE = 100 * 1024 *1024; // 100M
  private static final int DEFAULT_LONG_TIMEOUT = 5; // 5 Minutes

  private final Map<Method, MethodHandler<?>> methodHandlerCache = new LinkedHashMap<>();

  private final OkHttpClient client;
  private final BaseUrl baseUrl;
  private final List<Converter.Factory> converterFactories;
  private final List<CallAdapter.Factory> adapterFactories;
  private final Executor callbackExecutor;

  private final RequestManager requestManager;
  private final RequestInterceptor requestInterceptor;

  private AtomicReference<String> appTokenRef = new AtomicReference<String>(null);
  private AtomicReference<String> userTokenRef = new AtomicReference<String>(null);
  private AtomicReference<String> userNameRef = new AtomicReference<String>(null);
  private AtomicReference<String> deviceIdRef = new AtomicReference<String>(null);

  private Context applicationContext;
  private boolean isAuthRequired = false;

  private MagnetRestAdapter(OkHttpClient client, BaseUrl baseUrl, List<Converter.Factory> converterFactories,
      /*List<CallAdapter.Factory> adapterFactories,*/ Executor callbackExecutor) {
    this.client = client;
    this.baseUrl = baseUrl;
    this.converterFactories = converterFactories;
    //this.adapterFactories = adapterFactories;
    this.requestManager = new RequestManager(client);
    CallAdapter.Factory adapterFactory = new MagnetCallAdapter.Factory(this, requestManager);
    this.adapterFactories = Arrays.asList(adapterFactory);
    this.callbackExecutor = callbackExecutor;

    this.requestInterceptor = new RequestInterceptor(this, requestManager);
    client.interceptors().add(requestInterceptor);

    client.setAuthenticator(new MaxRestAuthenticator());
  }

  @Override public String getName() {
    return getClass().getSimpleName();
  }

  @Override public void onInit(Context context, Map<String, String> map, ApiCallback<Boolean> callback) {
    this.applicationContext = context;
    if(null != map && StringUtil.isNotEmpty(map.get("clientId")) && StringUtil.isNotEmpty(map.get("clientSecret"))) {
      isAuthRequired = true;
    }

    // Set cache
    //if(null == client.getCache()) {
    //  client.setCache(
    //      new Cache(new File(applicationContext.getCacheDir(), applicationContext.getPackageName()),
    //          DEFAULT_CACHE_SIZE));
    //}

    ConnectivityManager.getInstance(applicationContext).registerListener(requestManager);

    if(null != callback) {
      callback.success(true);
    }
  }

  @Override public void onAppTokenUpdate(String appToken, String appId, String deviceId, ApiCallback<Boolean> callback) {
    Log.d(TAG, "MagnetRestAdapter : appToken updated : " + appToken);

    boolean isEmptyBeforeUpdate = null == appTokenRef.get();

    appTokenRef.set(appToken);
    deviceIdRef.set(deviceId);

    if(isEmptyBeforeUpdate) {
      requestManager.resendPendingCallsForToken();
    }
  }

  @Override public void onUserTokenUpdate(String userToken, String userId, String deviceId, ApiCallback<Boolean> callback) {
    Log.d(TAG, "MagnetRestAdapter : userToken updated : " + userToken);
    userNameRef.set(userId);
    userTokenRef.set(userToken);
    deviceIdRef.set(deviceId);

    //requestManager.resendPendingCallsForToken();
  }

  @Override public void onClose(boolean b) {

  }

  @Override public void onUserTokenInvalidate(ApiCallback<Boolean> callback) {
    Log.d(TAG, "MagnetRestAdapter : userToken invalidated");
    userNameRef.set(null);
    userTokenRef.set(null);
    deviceIdRef.set(null);
  }

  @Override public void deInitModule(ApiCallback<Boolean> callback) {
    Log.d(TAG, "MagnetRestAdapter : module deInited");
  }

  /** Create an implementation of the API defined by the {@code service} interface. */
  @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
  public <T> T create(Class<T> service) {
    Utils.validateServiceInterface(service);
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        handler);
  }

  public void resendReliableCalls() {
    requestManager.resendReliableCalls();
  }

  public void clearPendingCalls() {
    requestManager.clearPendingCalls();
  }

  private final InvocationHandler handler = new InvocationHandler() {
    @Override public Object invoke(Object proxy, Method method, Object... args) throws Throwable {
      // If the method is a method from Object then defer to normal invocation.
      if (method.getDeclaringClass() == Object.class) {
        return method.invoke(this, args);
      }
      return loadMethodHandler(method).invoke(args);
    }
  };

  MethodHandler<?> loadMethodHandler(Method method) {
    MethodHandler<?> handler;
    synchronized (methodHandlerCache) {
      handler = methodHandlerCache.get(method);
      if (handler == null) {
        handler = MethodHandler.create(method,
            getClient(method),
            baseUrl, adapterFactories, converterFactories);
        methodHandlerCache.put(method, handler);
      }
    }
    return handler;
  }

  public OkHttpClient client() {
    return client;
  }

  public BaseUrl baseUrl() {
    return baseUrl;
  }

  public List<Converter.Factory> converterFactories() {
    return Collections.unmodifiableList(converterFactories);
  }

  public List<CallAdapter.Factory> callAdapterFactories() {
    return Collections.unmodifiableList(adapterFactories);
  }

  public Executor callbackExecutor() {
    return callbackExecutor;
  }

  @Override public boolean isAuthEnabled() {
    return isAuthRequired;
  }

  @Override public boolean isAuthRequired(Request request) {
    if("POST".equals(request.method()) && (request.urlString().endsWith(RestConstants.APP_LOGIN_URL)
    || request.urlString().endsWith(RestConstants.APP_LOGIN_WITH_DEVICE_URL)
    || request.urlString().endsWith(RestConstants.USER_REFRESH_TOKEN_URL))) {
      return false;
    }
    return true;
  }

  @Override public boolean isAuthReady(Request request) {
    return !isAuthEnabled() || !isAuthRequired(request) || null != getAppToken();
  }

  @Override public String getAppToken() {
    return appTokenRef.get();
  }

  @Override public String getUserToken() {
    return userTokenRef.get();
  }

  /**
   * Get the OkHttpClient with proper timeout settings
   * @param method
   * @return
   */
  private OkHttpClient getClient(Method method) {
    Timeout timeoutAnnotation = method.getAnnotation(Timeout.class);
    if(null != timeoutAnnotation) {
      if(0 != timeoutAnnotation.read() || 0 != timeoutAnnotation.write()) {
        OkHttpClient newClient = client.clone();
        if(isTimeoutValid(timeoutAnnotation.read(), method, timeoutAnnotation)) {
          newClient.setReadTimeout(timeoutAnnotation.read(), TimeUnit.SECONDS);
        }
        if(isTimeoutValid(timeoutAnnotation.write(), method, timeoutAnnotation)) {
          newClient.setWriteTimeout(timeoutAnnotation.write(), TimeUnit.SECONDS);
        }

        Log.d(TAG, "Using new OkHttpClient with new timeout " + timeoutAnnotation + " for method " + method);

        return newClient;
      }
    }

    return client;
  }

  private boolean isTimeoutValid(int value, Method method, Timeout timeout) {
    if (value > Integer.MAX_VALUE) {
      Log.e(TAG, "Timeout " + timeout + " for method " + method + " is too big");
      return false;
    } else if (value < 0) {
      Log.e(TAG, "Timeout " + timeout + " for method " + method + " should not be less than 0");
      return false;
    }

    return true;
  }

  /**
   * Build a new {@link MagnetRestAdapter}.
   * <p>
   * Calling {@link #baseUrl} is required before calling {@link #build()}. All other methods
   * are optional.
   */
  public static final class Builder {
    private OkHttpClient client;
    private BaseUrl baseUrl;
    private Converter.Factory converterFactory;
    private CallAdapter.Factory adapterFactory;
    private Executor callbackExecutor;
    private Context applicationContext;

    public Builder applicationContext(Context context) {
      this.applicationContext = context;
      return this;
    }
    /** The HTTP client used for requests. */
    public Builder client(OkHttpClient client) {
      this.client = checkNotNull(client, "client == null");
      return this;
    }

    /** API base URL. */
    public Builder baseUrl(String baseUrl) {
      checkNotNull(baseUrl, "baseUrl == null");
      if(!baseUrl.endsWith("/")) {
        baseUrl += "/";
      }
      HttpUrl httpUrl = HttpUrl.parse(baseUrl);
      if (httpUrl == null) {
        throw new IllegalArgumentException("Illegal URL: " + baseUrl);
      }
      return baseUrl(httpUrl);
    }

    /** API base URL. */
    public Builder baseUrl(final HttpUrl baseUrl) {
      checkNotNull(baseUrl, "baseUrl == null");
      return baseUrl(new BaseUrl() {
        @Override public HttpUrl url() {
          return baseUrl;
        }
      });
    }

    /** API base URL. */
    public Builder baseUrl(BaseUrl baseUrl) {
      this.baseUrl = checkNotNull(baseUrl, "baseUrl == null");
      return this;
    }

    /** The converter used for serialization and deserialization of objects. */
    //public Builder converterFactory(Converter.Factory converterFactory) {
    //  this.converterFactory = checkNotNull(converterFactory, "converterFactory == null");
    //  return this;
    //}

    /**
     * The executor on which {@link Callback} methods are invoked when returning {@link retrofit.Call} from
     * your service method.
     */
    public Builder callbackExecutor(Executor callbackExecutor) {
      this.callbackExecutor = checkNotNull(callbackExecutor, "callbackExecutor == null");
      return this;
    }

    /** Create the {@link MagnetRestAdapter} instances. */
    public MagnetRestAdapter build() {
      //if (applicationContext == null) {
      //  throw new IllegalStateException("applicationContext required.");
      //}

      if (baseUrl == null) {
        throw new IllegalStateException("Base URL required.");
      }

      // Set any platform-appropriate defaults for unspecified components.
      if (client == null) {
        client = Platform.get().defaultClient();
      }
      //if (adapterFactory == null) {
      //  adapterFactory = new MagnetCallAdapter.Factory();//Platform.get().defaultCallAdapterFactory(callbackExecutor);
      //}
      if (converterFactory == null) {
        converterFactory = MagnetGsonConverterFactory.create();//Platform.get().defaultCallAdapterFactory(callbackExecutor);
      }

      return new MagnetRestAdapter(client, baseUrl, Arrays.asList(converterFactory),/* Arrays.asList(adapterFactory),*/ callbackExecutor);
    }
  }
}