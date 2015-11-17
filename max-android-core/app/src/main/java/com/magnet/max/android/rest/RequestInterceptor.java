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
package com.magnet.max.android.rest;

import android.util.Log;
import com.magnet.max.android.auth.AuthTokenProvider;
import com.magnet.max.android.connectivity.ConnectivityManager;
import com.magnet.max.android.rest.qos.internal.CacheManager;
import com.magnet.max.android.util.AuthUtil;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;

/**
 * Interceptor to handle caching and access token
 */
public class RequestInterceptor implements Interceptor {
  public static final String TAG = RequestInterceptor.class.getSimpleName();

  private final AuthTokenProvider authTokenProvider;
  private final RequestManager requestManager;
  private final CacheManager cacheManager;

  public RequestInterceptor(AuthTokenProvider authTokenProvider, RequestManager requestManager) {
    this.authTokenProvider = authTokenProvider;
    this.requestManager = requestManager;

    this.cacheManager = new CacheManager();
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Log.i(TAG, "---------Intercepting url : " + request.method() + " " + request.urlString());

    CallOptions options = requestManager.popRequestOptions(request);
    if(null != options && null != options.getCacheOptions()) {
      if (options.getCacheOptions().isAlwaysUseCacheIfOffline()
          && ConnectivityManager.getInstance().getConnectivityStatus() == ConnectivityManager.TYPE_NOT_CONNECTED) {
        Response cachedResponse = cacheManager.getCachedResponse(request, options.getCacheOptions());
        if(null != cachedResponse) {
          Log.d(TAG, "-------return from cache when isAlwaysUseCacheIfOffline==true and offline");
          return cachedResponse;
        } else {
          throw new IOException("It's offline and no cached response found");
        }
      } else if (options.getCacheOptions().getMaxCacheAge() > 0) { // Return from cache if it's not expired
        Response cachedResponse = cacheManager.getCachedResponse(request, options.getCacheOptions());
        if(null != cachedResponse) {
          Log.d(TAG, "-------return from cache when maxCacheAge = " + options.getCacheOptions().getMaxCacheAge());
          return cachedResponse;
        }
      }
    }

    //Add auth token for network call
    String token = null;
    if(!authTokenProvider.isAuthEnabled() || !authTokenProvider.isAuthRequired(request)
        || null != authTokenProvider.getAppToken() || null != authTokenProvider.getUserToken()) {
      String existingToken = request.header(AuthUtil.AUTHORIZATION_HEADER);
      if(null != existingToken && existingToken.startsWith("Basic")) {
        // Already has Basic Auth header, don't overwrite
      } else {
        token = getToken();
      }
    }

    boolean useMock = false;
    if(null != options) {
      if(null != options.getCacheOptions()) {
        useMock = options.getCacheOptions().useMock();
      } else if(null != options.getReliableCallOptions()) {
        useMock = options.getReliableCallOptions().useMock();
      }
    }

    Response response = null;
    try {
      // Modify request
      if(null != token || useMock) {
        Request.Builder newRequestBuilder = chain.request().newBuilder();

        if(null != token) {
          newRequestBuilder.header(AuthUtil.AUTHORIZATION_HEADER, AuthUtil.generateOAuthToken(token));
        }

        if(useMock) {
          newRequestBuilder.url(request.urlString().replace(RestConstants.REST_BASE_PATH, RestConstants.REST_MOCK_BASE_PATH));
        }

        response = chain.proceed(newRequestBuilder.build());
      } else {
        response = chain.proceed(request);
      }

      if(null != options && options.isReliable()) { // Reliable call
        requestManager.removeReliableRequest(request);
      }
    } catch (IOException e) {
      //if(null != options && options.isReliable()) { // Reliable call
      //  requestManager.saveReliableRequest(request, null, null, options.getReliableCallOptions(), e.getMessage());
      //  //TODO :
      //  return null;  // Swallow exception
      //} else {
      //  throw e;
      //}

      throw e;
    }

    //Save/Update response in cache
    if(response.isSuccessful()
        && null != options && null != options.getCacheOptions()
        && (options.getCacheOptions().getMaxCacheAge() > 0 || options.getCacheOptions().isAlwaysUseCacheIfOffline())) {
      return cacheManager.cacheResponse(request, response, options.getCacheOptions());
    }

    return response;
  }

  private String getToken() {
    String authToken = null;
    if(null != authTokenProvider.getUserToken()) {
      authToken = authTokenProvider.getUserToken();
      Log.i(TAG, "--------using user token : " + authToken);
    } else if(null != authTokenProvider.getAppToken()){
      authToken = authTokenProvider.getAppToken();
      Log.i(TAG, "--------using app token : " + authToken);
    } else {
      Log.i(TAG, "--------no token ready yet");
    }

    //Log.i(TAG, "---------adding AuthHeaders : " + authToken);
    return authToken;
  }

  private boolean shouldNotify401(Request request) {
    String requestPath = request.httpUrl().encodedPath();
    if(requestPath.endsWith("/")) {
      requestPath = requestPath.substring(0, requestPath.length() - 1);
    }

    return !(requestPath.endsWith(RestConstants.APP_LOGIN_URL)
        || requestPath.endsWith(RestConstants.APP_LOGIN_WITH_DEVICE_URL)
        || requestPath.endsWith(RestConstants.USER_LOGIN_URL)
        || requestPath.endsWith(RestConstants.USER_LOGOUT_URL));
  }
}
