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
package com.magnet.max.android.config;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An implementation of the {@link MaxAndroidConfig} that reads the config from a properties file.
 * This file is expected to have the following values:
 *
 * <P>
 * <pre>
 *  {@literal
 *    baseUrl=<The base url of the server, in format of <protocol>://<host>:<port>[/path], protocol of the server supports HTTP|HTTPS>
 *    client_id=<App client ID>
 *    client_secret=<App client Secret>
 *   }
 * </pre>
 */
public class MaxAndroidPropertiesConfig implements MaxAndroidConfig {

  public static final String PROP_CLIENT_ID = "client_id";
  public static final String PROP_CLIENT_SECRET = "client_secret";
  public static final String PROP_BASE_URL = "baseUrl";
  public static final String PROP_SCOPE = "scope";

  private static final String TAG = MaxAndroidPropertiesConfig.class.getSimpleName();
  private final Properties mProps;
  private Map<String, String> mPropertiesMap;

  public MaxAndroidPropertiesConfig(Context context, int resId) {
    InputStream is = context.getResources().openRawResource(resId);
    mProps = new Properties();
    loadProps(is);
  }

  @Override public String getBaseUrl() {
    return getStringProperty(PROP_BASE_URL);
  }

  @Override public String getClientId() {
    return getStringProperty(PROP_CLIENT_ID);
  }

  @Override public String getClientSecret() {
    return getStringProperty(PROP_CLIENT_SECRET);
  }

  @Override public String getScope() {
    return getStringProperty(PROP_SCOPE);
  }

  @Override public Map<String, String> getAllConfigs() {
    if(null == mPropertiesMap) {
      mPropertiesMap = new HashMap<>();
      for(String key : mProps.stringPropertyNames()) {
        mPropertiesMap.put(key, mProps.getProperty(key));
      }
    }

    return mPropertiesMap;
  }

  private String getStringProperty(String propertyName) {
    return mProps.getProperty(propertyName);
  }

  private int getIntProperty(String propertyName) {
    return Integer.parseInt(mProps.getProperty(propertyName));
  }

  private void loadProps(InputStream is) {
    try {
      mProps.load(is);
    } catch (IOException e) {
      Log.e(TAG, "Exception caught while loading config", e);
      throw new RuntimeException(e);
    }
  }
}
