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
import com.magnet.max.android.MaxCore;

public class RenewTokenRequest {

  @SerializedName("refresh_token")
  protected final String refreshToken;
  @SerializedName("client_id")
  protected final String clientId;
  @SerializedName("grant_type")
  protected final String grantType = "refresh_token";
  @SerializedName("device_id")
  protected final String deviceId;
  private final String scope = "user";

  public RenewTokenRequest(String refreshToken) {
    this.refreshToken = refreshToken;
    this.clientId = MaxCore.getConfig().getClientId();
    this.deviceId = Device.getCurrentDeviceId();
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getClientId() {
    return clientId;
  }

  public String getGrantType() {
    return grantType;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getScope() {
    return scope;
  }
}
