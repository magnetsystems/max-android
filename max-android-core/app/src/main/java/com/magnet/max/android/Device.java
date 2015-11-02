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

import android.util.Log;
import com.magnet.max.android.auth.model.DeviceInfo;
import com.magnet.max.android.auth.model.DeviceStatus;
import com.magnet.max.android.auth.model.OsType;
import com.magnet.max.android.auth.model.PushAuthorityType;
import com.magnet.max.android.auth.model.UserStatus;
import com.magnet.max.android.util.DeviceUtil;
import retrofit.Callback;
import retrofit.MagnetCall;
import retrofit.Response;

/**
 * The Device class is a local representation of a device in the MagnetMax platform.
 * This class provides various device specific methods, like registering a the device with GCM registration id.
 */
public class Device {
  private static final String TAG = "Device";

  private String[] tags;
  private OsType os;
  private String osVersion;
  private String deviceToken;
  private String userId;
  private DeviceStatus deviceStatus;
  private String label;
  private PushAuthorityType pushAuthority;
  private String deviceId;

  private static Device sCurrentDevice;
  private static DeviceService sDeviceService;
  private static String sCurrentDeviceId;

  public static String getCurrentDeviceId() {
    if(null == sCurrentDeviceId) {
      sCurrentDeviceId = DeviceUtil.getDeviceId(MaxCore.getApplicationContext());
    }

    return sCurrentDeviceId;
  }

  public static Device getCurrentDevice() {
    return sCurrentDevice;
  }

  public static void register(DeviceInfo deviceInfo, final ApiCallback<Device> callback) {
    MagnetCall call = getDeviceService().registerDevice(deviceInfo, new Callback<Device>() {
      @Override public void onResponse(retrofit.Response<Device> response) {
        Log.i(TAG, "registerDevice success : ");
        if (null != callback) {
          if (response.isSuccess()) {
            callback.success(response.body());
          } else {
            callback.failure(new ApiError(response.message(), response.code()));
          }
        }

        sCurrentDevice = response.body();
      }

      @Override public void onFailure(Throwable throwable) {
        Log.e(TAG, "registerDevice error : " + throwable.getMessage());
        if (null != callback) {
          callback.failure(new ApiError(throwable.getMessage()));
        }
      }
    });
    call.executeInBackground();
  }

  public static void unRegister(final ApiCallback<Boolean> callback) {
    getDeviceService().unRegisterDevice(getCurrentDeviceId(), new Callback<Boolean>() {
      @Override public void onResponse(Response<Boolean> response) {
        if(null != callback) {
          if (response.isSuccess()) {
            callback.success(response.body());
          } else {
            callback.failure(new ApiError(response.message(), response.code()));
          }
        }
      }

      @Override public void onFailure(Throwable throwable) {
        if(null != callback) {
          callback.failure(new ApiError(throwable.getMessage()));
        }
      }
    }).executeInBackground();

    sCurrentDevice = null;
  }

  private static DeviceService getDeviceService() {
    if(null == sDeviceService) {
      sDeviceService = MaxCore.create(DeviceService.class);
    }
    return sDeviceService;
  }

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

}
