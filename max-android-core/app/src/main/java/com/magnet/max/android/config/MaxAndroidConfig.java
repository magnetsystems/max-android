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

import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class defines properties needed to initialize Magnet Max
 */
public abstract class MaxAndroidConfig {
  public static final String PROP_CLIENT_ID = "client_id";
  public static final String PROP_CLIENT_SECRET = "client_secret";
  public static final String PROP_BASE_URL = "baseUrl";
  public static final String PROP_SCOPE = "scope";

  private Map<String, String> configMap;

  /**
   * The base URL of the Magnet Max server
   */
  abstract public String getBaseUrl();

  /**
   * The clientId of the app
   */
  abstract public String getClientId();

  /**
   * The clientId secret the app
   */
  abstract public String getClientSecret();

  /**
   * The scope of the app
   */
  public String getScope() {
    return null;
  }

  /**
   * All configuration properties in key-value pair
   */
  public Map<String, String> getAllConfigs() {
    if(null == configMap) {
      configMap = new HashMap<>();
    }

    configMap.put(PROP_BASE_URL, getBaseUrl());
    configMap.put(PROP_CLIENT_ID, getClientId());
    configMap.put(PROP_CLIENT_SECRET, getClientSecret());
    configMap.put(PROP_SCOPE, getScope());

    return configMap;
  }
}
