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
import android.util.Log;
import com.magnet.max.android.config.MaxAndroidConfig;
import com.magnet.max.android.connectivity.ConnectivityManager;
import com.magnet.max.android.rest.SystemDataStore;
import com.magnet.max.android.util.StringUtil;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

final public class MaxCore {
  private static final String TAG = MaxCore.class.getSimpleName();

  private static AtomicBoolean mIsInited = new AtomicBoolean(false);

  private static MaxAndroidConfig mConfig;

  private static Context mApplicationContext;

  private static MagnetServiceAdapter mServiceAdapter;

  /**
   * Initialize Max Core with given configuration
   * @param context
   * @param config
   */
  public static synchronized void init(Context context, MaxAndroidConfig config) {
    if(null == context) {
      throw new IllegalArgumentException("applicationContext shouldn't be null");
    }

    if(mIsInited.get() && context.getApplicationContext().equals(mApplicationContext)&& isConfigEqual(config)) {
      String message = "MaxCore has been already inited with same config";
      Log.w(TAG, "------" + message);
      throw new IllegalStateException(message);
    }

    mIsInited.set(false);

    mApplicationContext = context.getApplicationContext();
    mConfig = config;

    // Init other utils
    ConnectivityManager.initialize(mApplicationContext);
    SystemDataStore.initialize(mApplicationContext, true);
    ModuleManager.init();

    // Init service adapter
    mServiceAdapter = new MagnetServiceAdapter.Builder().applicationContext(mApplicationContext)
        .config(config).build();

    mIsInited.set(true);
    Log.d(TAG, "---------MaxCore is inited");
  }

  /**
   * Get current configuration
   * @return
   */
  public static MaxAndroidConfig getConfig() {
    return mConfig;
  }

  /**
   * Get current application context
   * @return
   */
  public static Context getApplicationContext() {
    return mApplicationContext;
  }

  /**
   * Register a MAX module {@link MaxModule}
   * @param module
   */
  public static void register(MaxModule module) {
    ModuleManager.register(module, null);
  }

  /**
   * Register a MAX module {@link MaxModule} and call onInit
   * @param module
   */
  public static void initModule(MaxModule module, ApiCallback<Boolean> callback) {
    if(!mIsInited.get()) {
      throw new IllegalArgumentException("init is not finished yet");
    }
    if(null != module) {
      Log.d(TAG, "--------init module " + module.getName() + " : " + module);
      ModuleManager.register(module, callback);
      //mServiceAdapter.register(module, callback);
    }
  }

  /**
   * De-initialized a module
   * @param module
   * @param callback
   */
  public static void deInitModule(MaxModule module, ApiCallback<Boolean> callback) {
    if(!mIsInited.get()) {
      throw new IllegalArgumentException("init is not finished yet");
    }
    if(null != module) {
      //mServiceAdapter.deRegister(module, callback);
      Log.d(TAG, "--------deInit module " + module.getName() + " : " + module);
      ModuleManager.deRegister(module, callback);
    }
  }

  public synchronized static void deInit() {
    if(!mIsInited.get()) {
      throw new IllegalArgumentException("init is not finished yet");
    }

    ModuleManager.deInit();
  }

  public static void userTokenInvalid(String token, MaxModule source) {
    ModuleManager.onUserTokenInvalid();
  }

  public static void appTokenInvalid(String token, MaxModule source) {
    ModuleManager.onAppTokenInvalid();
  }

  public static void tokenInvalid(String token, MaxModule source) {
    ModuleManager.onTokenInvalid(token);
  }

  /**
   * Create a instance of remote service
   * @param service
   * @param <T>
   * @return
   */
  public static <T> T create(Class<T> service) {
    return mServiceAdapter.create(service);
  }

  private static boolean isConfigEqual(MaxAndroidConfig config) {
    if(mConfig.getAllConfigs().size() != config.getAllConfigs().size()) {
      return false;
    }

    Set<String> keys = mConfig.getAllConfigs().keySet();
    for(String k : keys) {
      if(!StringUtil.isStringValueEqual(mConfig.getAllConfigs().get(k),
          config.getAllConfigs().get(k))) {
        return false;
      }
    }

    return true;
  }
}
