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
package com.magnet.max.android.auth.model;

import com.google.gson.annotations.SerializedName;
import com.magnet.max.android.Device;
import java.util.Map;

public class AppLoginResponse {

  @SerializedName("expires_in")
  private Long expiresIn;

  @SerializedName("access_token")
  private String accessToken;

  @SerializedName("token_type")
  private String tokenType;

  @SerializedName("mmx_app_id")
  private String mmxAppId;

  private String scope;

  @SerializedName("config")
  private Map<String, String> serverConfig;

  public Long getExpiresIn() {
    return expiresIn;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getMmxAppId() {
    return mmxAppId;
  }

  public String getScope() {
    return scope;
  }

  public Map<String, String> getServerConfig() {
    return serverConfig;
  }

}
