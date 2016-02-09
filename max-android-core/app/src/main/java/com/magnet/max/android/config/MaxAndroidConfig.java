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

import com.magnet.max.android.util.StringUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class defines properties needed to initialize Magnet Max
 */
public abstract class MaxAndroidConfig {
  private static final String DEFAULT_BASE_URL = "https://sandbox.magnet.com/mobile/api";

  public static final String PROP_CLIENT_ID = "client_id";
  public static final String PROP_CLIENT_SECRET = "client_secret";
  public static final String PROP_BASE_URL = "baseUrl";
  public static final String PROP_SCOPE = "scope";
  public static final String PROP_GCM_SENDER_ID = "mmx-gcmSenderId";

  private Map<String, String> configMap;

  /**
   * The base URL of the Magnet Max server.
   * If not set, default Magnet Sandbox URL will be used.
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
   * The GCM senderId
   */
  public String getGcmSenderId() {
    return null;
  }

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
    configMap.put(PROP_GCM_SENDER_ID, getGcmSenderId());

    return configMap;
  }

  public static class Builder {
    private String clientId;
    private String clientSecret;
    private String baseUrl;
    private String gcmSenderId;

    public Builder() {

    }

    /**
     * The baseUrl of the Magnet Max server.
     * If not set, default Magnet Sandbox URL will be used.
     * @param value
     * @return
     */
    public Builder baseUrl(String value) {
      baseUrl = value;
      return this;
    }

    /**
     * The clientId of the app
     */
    public Builder clientId(String value) {
      clientId = value;
      return this;
    }

    /**
     * The clientId secret the app
     */
    public Builder clientSecret(String value) {
      clientSecret = value;
      return this;
    }

    /**
     * The GCM senderId to enable GCM (optional)
     */
    public Builder gcmSenderId(String value) {
      gcmSenderId = value;
      return this;
    }

    public MaxAndroidConfig build() {
      if(StringUtil.isEmpty(clientId)) {
        throw new IllegalArgumentException("clientId should not be null");
      }
      if(StringUtil.isEmpty(clientSecret)) {
        throw new IllegalArgumentException("clientSecret should not be null");
      }
      if(StringUtil.isEmpty(baseUrl)){
        baseUrl = DEFAULT_BASE_URL;
      }
      return new MaxAndroidConfig() {
        @Override public String getBaseUrl() {
          return baseUrl;
        }

        @Override public String getClientId() {
          return clientId;
        }

        @Override public String getClientSecret() {
          return clientSecret;
        }

        @Override public String getGcmSenderId() {
          return gcmSenderId;
        }
      };
    }
  }
}
