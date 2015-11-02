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
package com.magnet.max.android.auth.model;

import android.os.Build;
import com.magnet.max.android.Device;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.User;
import com.magnet.max.android.util.DeviceUtil;

public class DeviceInfo {
  private String[] tags;
  private OsType os;
  private String osVersion;
  private String deviceToken;
  private String userId;
  private DeviceStatus deviceStatus;
  private String label;
  private PushAuthorityType pushAuthority;
  private String deviceId;

  /**
   * The tags associated with the device.
   */
  public String[] getTags() {
    return tags;
  }

  /**
   * The OS type {@link OsType} for the device.
   */
  public OsType getOs() {
    return os;
  }

  /**
   * The OS version for the device.
   */
  public String getOsVersion() {
    return osVersion;
  }

  /**
   The token (GCM registration id for Android) for the device.
   */
  public String getDeviceToken() {
    return deviceToken;
  }

  /**
   * The owner for the device.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * The status {@link DeviceStatus} for the device.
   */
  public DeviceStatus getDeviceStatus() {
    return deviceStatus;
  }

  /**
   * The label for the device.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The push authority {@link PushAuthorityType} for the device.
   */
  public PushAuthorityType getPushAuthority() {
    return pushAuthority;
  }

  /**
   * The unique identifer for the device.
   */
  public String getDeviceId() {
    return deviceId;
  }


  /**
   * Builder for Device
   **/
  public static class Builder {
    private DeviceInfo toBuild = new DeviceInfo();

    public Builder() {
    }

    public DeviceInfo build() {
      toBuild.deviceId = DeviceUtil.getDeviceId(MaxCore.getApplicationContext());
      toBuild.os = OsType.ANDROID;
      toBuild.osVersion = android.os.Build.VERSION.RELEASE;
      toBuild.pushAuthority = PushAuthorityType.GCM;
      toBuild.label = Build.MANUFACTURER + " "+ Build.MODEL;
      toBuild.deviceStatus = DeviceStatus.ACTIVE;
      toBuild.userId = User.getCurrentUserId();
      return toBuild;
    }

    /**
     * The tags associated with the device.
     */
    public Builder tags(String[] value) {
      toBuild.tags = value;
      return this;
    }

    /**
     The token (GCM registration id for Android) for the device.
     */
    public Builder deviceToken(String value) {
      toBuild.deviceToken = value;
      return this;
    }

    /**
     * The label for the device.
     */
    public Builder label(String value) {
      toBuild.label = value;
      return this;
    }
  }
}
