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
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.MaxModule;
import com.magnet.max.android.config.MaxAndroidConfig;

/**
 * This class is the entry point of Magnet Max Android SDK.
 */
public class Max {

  /**
   * Initialize Magnet Max Android with a Android Context and MagnetAndroidConfig
   * @param context
   * @param config
   */
  public static synchronized void init(Context context, MaxAndroidConfig config) {
    MaxCore.init(context, config);
  }

  /**
   * Register and initialize a {@link MaxModule}
   * @param module
   * @param callback
   */
  public static void initModule(MaxModule module, ApiCallback<Boolean> callback) {
    MaxCore.initModule(module, callback);
  }

  /**
   * deregister and de-initialize a {@link MaxModule}
   * @param module
   * @param callback
   */
  public static void deInitModule(MaxModule module, ApiCallback<Boolean> callback) {
    MaxCore.deInitModule(module, callback);
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
