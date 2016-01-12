/*
 *  Copyright (c) 2015 Magnet Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.magnet.max.android;

import android.util.Log;
import com.magnet.max.android.auth.model.AppLoginResponse;
import com.magnet.max.android.auth.model.ApplicationToken;
import com.magnet.max.android.auth.model.RenewTokenRequest;
import com.magnet.max.android.auth.model.Response401;
import com.magnet.max.android.auth.model.UserLoginResponse;
import com.magnet.max.android.auth.model.UserToken;
import com.magnet.max.android.rest.RestConstants;
import com.magnet.max.android.util.AuthUtil;
import com.magnet.max.android.util.StringUtil;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import retrofit.Callback;

public class MaxRestAuthenticator implements Authenticator {
  private static final String TAG = MaxRestAuthenticator.class.getSimpleName();

  private static final int REFRESH_TOKEN_TIMEOUT = 5;

  private UserService mUserService;
  private ApplicationService mApplicationService;

  @Override public Request authenticate(Proxy proxy, Response response) throws IOException {
    Request request = response.request();
    String originalToken = AuthUtil.extractOAuthToken(request.header(AuthUtil.AUTHORIZATION_HEADER));
    String authError = response.header(Response401.ERROR_HEADER);
    Log.e(TAG, "Got 401 for request : " + request.urlString() + " with token :\n" + originalToken
      + "\n error : " + authError);

    Response401 response401 = null;
    if(StringUtil.isNotEmpty(authError)) {
      response401 = new Response401(authError);
    }

    final AtomicReference<String> refreshedToken = new AtomicReference<>();

    String requestPath = request.httpUrl().encodedPath();
    if(requestPath.endsWith("/")) {
      requestPath = requestPath.substring(0, requestPath.length() - 1);
    }
    if(requestPath.endsWith(RestConstants.APP_LOGIN_URL)
        || requestPath.endsWith(RestConstants.APP_LOGIN_WITH_DEVICE_URL)) {
      // App login failed, handle by callback in MagnetRestAdapter
    } else if(requestPath.endsWith(RestConstants.USER_LOGIN_URL)
        || requestPath.endsWith(RestConstants.USER_LOGOUT_URL)) {
      // User login failed, handle by callback in User.login
    } else if(requestPath.endsWith(RestConstants.USER_REFRESH_TOKEN_URL)) {
      // User token refresh failed
      MaxCore.userTokenInvalid(originalToken, null);
    } else if(null != response401 && response401.getErrorType() == Response401.AuthErrorType.CLIENT_ACCESS_TOKEN) {
      renewAppToken(refreshedToken);
    } else if(null != response401 && response401.getErrorType() == Response401.AuthErrorType.USER_ACCESS_TOKEN) {
      renewUserToken(refreshedToken);
    } else {
      if(null != response401) {
        if(response401.getErrorType() == Response401.AuthErrorType.USER_ACCESS_TOKEN) {
          MaxCore.userTokenInvalid(originalToken, null);
        } else {
          MaxCore.appTokenInvalid(originalToken, null);
        }
      } else {
        MaxCore.tokenInvalid(originalToken, null);
      }
    }

    // Reply the request with refreshed token
    if(null != refreshedToken.get()) {
      Log.d(TAG, "Using refreshed token : " + refreshedToken.get());

      // Replace token
      Headers newHeaders = request.headers().newBuilder().set(AuthUtil.AUTHORIZATION_HEADER,
          AuthUtil.generateOAuthToken(refreshedToken.get())).build();

      return request.newBuilder().headers(newHeaders).build();
    } else {
      Log.w(TAG, "No new token available, won't answer the challenge for " + request.urlString());
    }

    return null;
  }

  @Override public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
    return null;
  }

  private UserService getUserService() {
    if(null == mUserService) {
      mUserService = MaxCore.create(UserService.class);
    }

    return mUserService;
  }

  private ApplicationService getApplicationService() {
    if(null == mApplicationService) {
      mApplicationService = MaxCore.create(ApplicationService.class);
    }

    return mApplicationService;
  }

  private void renewAppToken(final AtomicReference<String> refreshedToken) {
    // Clean up existing token
    ModuleManager.onAppLogout(MaxCore.getConfig().getClientId());

    String authHeader = AuthUtil.generateBasicAuthToken(MaxCore.getConfig().getClientId(), MaxCore.getConfig().getClientSecret());
    final CountDownLatch latch = new CountDownLatch(1);
    getApplicationService().appCheckin(Device.getCurrentDeviceId(), authHeader, new Callback<AppLoginResponse>() {
      @Override public void onResponse(retrofit.Response<AppLoginResponse> response) {
        if(response.isSuccess()) {
          Log.i(TAG, "appCheckin success : ");
        } else {
          handleError(response.message());
          return;
        }
        AppLoginResponse appCheckinResponse = response.body();
        refreshedToken.set(appCheckinResponse.getAccessToken());
        ModuleManager.onAppLogin(MaxCore.getConfig().getClientId(),
            new ApplicationToken(appCheckinResponse.getExpiresIn(), appCheckinResponse.getAccessToken(), appCheckinResponse.getTokenType(),
                appCheckinResponse.getScope(), appCheckinResponse.getMmxAppId()),
            appCheckinResponse.getServerConfig());

        latch.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        handleError(throwable.getMessage());
        latch.countDown();
      }

      private void handleError(String errorMessage) {
        Log.e(TAG, "appCheckin failed due to : " + errorMessage);
        ModuleManager.onAppTokenInvalid();
      }
    }).executeInBackground();

    try {
      latch.await(REFRESH_TOKEN_TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Log.d(TAG, "refresh app token timeout");
    }
  }

  private void renewUserToken(final AtomicReference<String> refreshedToken) {
    // Trying to auto recover to renew user token
    final UserToken userToken = ModuleManager.getUserToken();
    if(null != userToken && StringUtil.isNotEmpty(userToken.getRefreshToken())) {
      final CountDownLatch latch = new CountDownLatch(1);
      getUserService().renewToken(new RenewTokenRequest(userToken.getRefreshToken()),
          AuthUtil.generateOAuthToken(userToken.getRefreshToken()),
          new Callback<UserLoginResponse>() {
        @Override public void onResponse(retrofit.Response<UserLoginResponse> response) {
          if(response.isSuccess()) {
            Log.i(TAG, "renewToken success : ");
          } else {
            Log.e(TAG, "renewToken failed due to : " + response.message());
            handleUserTokenRefreshFailure();
            latch.countDown();
            return;
          }

          UserLoginResponse userLoginResponse = response.body();

          if (null != userLoginResponse.getAccessToken()) {
            ModuleManager.onUserTokenRefresh(userLoginResponse.getUser().getUserIdentifier(),
                new UserToken(userLoginResponse.getExpiresIn(), userLoginResponse.getAccessToken(),
                    userToken.getRefreshToken(), userLoginResponse.getTokenType()));

            refreshedToken.set(userLoginResponse.getAccessToken());
          } else {
            handleUserTokenRefreshFailure();
          }

          latch.countDown();
        }

        @Override public void onFailure(Throwable throwable) {
          Log.e(TAG, "renewToken failed due to : " + throwable.getMessage());
          handleUserTokenRefreshFailure();
          latch.countDown();
        }
      }).executeInBackground();

      try {
        latch.await(REFRESH_TOKEN_TIMEOUT, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Log.d(TAG, "refresh user token timeout");
      }
    } else {
      Log.w(TAG, "Refresh token is not available, won't renew");
      handleUserTokenRefreshFailure();
    }
  }

  private void handleUserTokenRefreshFailure() {
    ModuleManager.onUserTokenInvalid();
  }
}
