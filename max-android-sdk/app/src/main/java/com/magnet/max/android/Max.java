/*   Copyright (c) 2015 Magnet Systems, Inc.
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
 */
package com.magnet.max.android;

import android.content.Context;
import android.util.Log;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.MaxModule;
import com.magnet.max.android.config.MaxAndroidConfig;
import com.magnet.mmx.client.api.MMX;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the entry point of Magnet Max Android SDK.
 */
public class Max {
  private static final String TAG = Max.class.getSimpleName();

  private static final String DEFAULT_BASE_URL = "https://sandbox.magnet.com/mobile/api";

  // Disable default constructor
  private Max() {}

  /**
   * Initialize Magnet Max Android with a Android Context and MagnetAndroidConfig
   * @param context
   * @param config
   */
  public static synchronized void init(Context context, MaxAndroidConfig config) {
    try {
      MaxCore.init(context, config);
    } catch (IllegalStateException ise) {
      if(ise.getMessage().equals("MaxCore has been already inited with same config")) {
        Log.w(TAG, ise.getMessage());
        return;
      }
    }

    MaxCore.register(MMX.getModule());
  }

  /**
   * Init Magnet Max Android with clientId, secret and default Sandbox baseUrl
   * @param context
   * @param clientId
   * @param clientSecret
   */
  public static synchronized void init(Context context, String clientId, String clientSecret) {
    init(context, DEFAULT_BASE_URL, clientId, clientSecret);
  }

  /**
   * Init Magnet Max Android with baseUrl, clientId and secret
   * @param context
   * @param baseUrl
   * @param clientId
   * @param clientSecret
   */
  public static synchronized void init(Context context, final String baseUrl, final String clientId, final String clientSecret) {
    init(context, new MaxAndroidConfig() {
      @Override public String getBaseUrl() {
        return baseUrl;
      }

      @Override public String getClientId() {
        return clientId;
      }

      @Override public String getClientSecret() {
        return clientSecret;
      }
    });
  }

  /**
   * Register and initialize a {@link MaxModule}
   * @param module
   * @param callback
   */
  @Deprecated
  public static void initModule(MaxModule module, ApiCallback<Boolean> callback) {
    //MaxCore.initModule(module, callback);
    if(null != callback) {
      callback.success(true);
    }
  }

  /**
   * deregister and de-initialize a {@link MaxModule}
   * @param module
   * @param callback
   */
  @Deprecated
  public static void deInitModule(MaxModule module, ApiCallback<Boolean> callback) {
    //MaxCore.deInitModule(module, callback);
    if(null != callback) {
      callback.success(true);
    }
  }

  /**
   * Get the MagnetAndroidConfig which is used in Max#init
   * @return
   */
  public static MaxAndroidConfig getConfig() {
    return MaxCore.getConfig();
  }

  /**
   * Get Application Context
   * @return
   */
  public static Context getApplicationContext() {
    return MaxCore.getApplicationContext();
  }

  /**
   * Create a instance of remote service
   * @param service
   * @param <T>
   * @return
   */
  public static <T> T create(Class<T> service) {
    return MaxCore.create(service);
  }
}
