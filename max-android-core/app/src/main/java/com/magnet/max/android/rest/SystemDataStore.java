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

import android.content.Context;
import android.util.Log;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.magnet.max.android.rest.qos.internal.CachedRequestSerilizer;
import com.magnet.max.android.rest.qos.internal.CachedResponseSerilizer;
import com.magnet.max.android.rest.qos.internal.ReliableRequestEntity;
import com.magnet.max.android.rest.qos.internal.ResponseCacheEntity;

public class SystemDataStore {
  private static final String TAG = SystemDataStore.class.getSimpleName();
  private static final String DB_NAME = "magnet_android.db";

  private static SystemDataStore _instance;

  private final Context applicationContext;

  private boolean toRecreate;

  private SystemDataStore(Context applicationContext, boolean toRecreate) {
    this.applicationContext = applicationContext;
    this.toRecreate = toRecreate;

    Configuration.Builder configurationBuilder = new Configuration.Builder(applicationContext);
    configurationBuilder.setDatabaseName(DB_NAME).setDatabaseVersion(4);
    configurationBuilder.addModelClass(ReliableRequestEntity.class);
    configurationBuilder.addTypeSerializer(CachedRequestSerilizer.class);
    configurationBuilder.addModelClass(ResponseCacheEntity.class);
    configurationBuilder.addTypeSerializer(CachedResponseSerilizer.class);
    ActiveAndroid.initialize(configurationBuilder.create());

    Log.d(TAG, "---------system db initialized ....");
  }

  public static synchronized void initialize(Context applicationContext, boolean toRecreate) {
    if(null != _instance) {
      // Clean up
    }

    _instance = new SystemDataStore(applicationContext, toRecreate);
  }

  public static SystemDataStore getInstance() {
    if(null == _instance) {
      throw new IllegalStateException("SystemDataStore hasn't been initialized.");
    }

    return _instance;
  }

  public void drop() {
    applicationContext.deleteDatabase(DB_NAME);
  }

}
