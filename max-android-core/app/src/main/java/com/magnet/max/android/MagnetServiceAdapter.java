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
package com.magnet.max.android;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.magnet.max.android.auth.model.AppLoginResponse;
import com.magnet.max.android.auth.model.DeviceInfo;
import com.magnet.max.android.config.MaxAndroidConfig;
import com.magnet.max.android.util.AuthUtil;
import com.magnet.max.android.util.StringUtil;
import com.squareup.okhttp.OkHttpClient;
import java.util.HashMap;
import java.util.Map;
import retrofit.MagnetCall;
import retrofit.MagnetRestAdapter;

/**public**/ class MagnetServiceAdapter {
  private static final String TAG = MagnetServiceAdapter.class.getSimpleName();

  private MaxAndroidConfig config;
  private MagnetRestAdapter restAdapter;
  private Context applicationContext;
  private Map<String, String> configMap;

  // builit-in services
  private ApplicationService applicationService;

  private MagnetServiceAdapter(Context context, MaxAndroidConfig config, MagnetRestAdapter restAdapter) {
    this.config = config;
    configMap = config.getAllConfigs();
    if(null == configMap) {
      configMap = new HashMap<>();
    }
    this.restAdapter = restAdapter;
    this.applicationContext = context;

    ModuleManager.register(restAdapter, null);

    init();
  }

  /** Create an implementation of the API defined by the specified {@code service} interface. */
  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> service) {
    return restAdapter.create(service);
  }

  public void resendReliableCalls() {
    restAdapter.resendReliableCalls();
  }

  public void clearPendingCalls() {
    restAdapter.clearPendingCalls();
  }

  private void init() {
    if(null == applicationService) {
      applicationService = restAdapter.create(ApplicationService.class);
    }

    if(TextUtils.isEmpty(config.getClientId())) {
      Log.e(TAG, "ClientId is not set");
      return;
    }

    // Reset app token
    ModuleManager.onAppLogout(config.getClientId());

    String authHeader = AuthUtil.generateBasicAuthToken(config.getClientId(), config.getClientSecret());
    MagnetCall<AppLoginResponse> call = applicationService.checkInWithDevice(authHeader, new DeviceInfo.Builder().build(), new retrofit.Callback<AppLoginResponse>() {
          @Override public void onResponse(retrofit.Response<AppLoginResponse> response) {
            Log.i(TAG, "appCheckin success : ");
            AppLoginResponse appCheckinResponse = response.body();


            if(null != appCheckinResponse.getDevice()) {
              //TODO : save as current device?
            }

            Map<String, String> serverConfig = new HashMap<String, String>();
            serverConfig.put("mmx-appId", appCheckinResponse.getApplicationToken().getMmxAppId());
            if(null != appCheckinResponse.getServerConfig()) {
              serverConfig.putAll(appCheckinResponse.getServerConfig());
            }
            ModuleManager.onAppLogin(config.getClientId(), appCheckinResponse.getApplicationToken(),
                serverConfig);
          }

          @Override public void onFailure(Throwable throwable) {
            Log.e(TAG, "appCheckin error : " + throwable.getMessage());
          }
        });
    call.executeInBackground();
  }

  /**
   * Build a new {@link MagnetRestAdapter}.
   * <p>
   * Calling {@link #config} is required before calling {@link #build()}. All other methods
   * are optional.
   */
  public static class Builder {
    private OkHttpClient client;
    private MaxAndroidConfig config;
    private MagnetRestAdapter restAdapter;
    private Context applicationContext;

    public Builder applicationContext(Context context) {
      this.applicationContext = context;
      return this;
    }

    /** The HTTP client used for requests. */
    public Builder client(OkHttpClient client) {
      if (client == null) {
        throw new NullPointerException("Client may not be null.");
      }
      this.client = client;
      return this;
    }

    public Builder config(MaxAndroidConfig config) {
      this.config = config;
      return this;
    }

    /** Create the {@link MagnetRestAdapter} instances. */
    public MagnetServiceAdapter build() {
      if (config == null) {
        throw new IllegalArgumentException("config may not be null.");
      } else {
        if(StringUtil.isEmpty(config.getBaseUrl())) {
          throw new IllegalArgumentException("baseUrl may not be null in config.");
        }
      }
      if (applicationContext == null) {
        throw new IllegalArgumentException("applicationContext may not be null.");
      }

      String baseUrl = config.getBaseUrl();
      if(null != baseUrl && !baseUrl.endsWith("/")) {
        baseUrl += "/";
      }
      MagnetRestAdapter.Builder builder = new MagnetRestAdapter.Builder().baseUrl(baseUrl);
      if(null != client) {
        builder.client(client);
      }

      //RequestInterceptor finalReqestInterceptor = new AuthRequestInterceptor(requestInterceptor);
      //requestInterceptor(finalReqestInterceptor);

      restAdapter = builder.build();
      Map<String, String> sharedConfig = new HashMap<>();
      sharedConfig.put("clientId", config.getClientId());
      sharedConfig.put("clientSecret", config.getClientSecret());
      ((MaxModule) restAdapter).onInit(applicationContext, sharedConfig, null);

      //ensureSaneDefaults();
      return new MagnetServiceAdapter(applicationContext, config, restAdapter);
    }
  }
}
