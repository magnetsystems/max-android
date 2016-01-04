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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.magnet.max.android.auth.model.DeviceInfo;
import com.magnet.max.android.auth.model.DeviceStatus;
import com.magnet.max.android.auth.model.OsType;
import com.magnet.max.android.auth.model.PushAuthorityType;
import com.magnet.max.android.util.DeviceUtil;
import com.magnet.max.android.util.EqualityUtil;
import com.magnet.max.android.util.HashCodeBuilder;
import com.magnet.max.android.util.StringUtil;
import retrofit.Callback;
import retrofit.MagnetCall;
import retrofit.Response;

/**
 * The Device class is a local representation of a device in the MagnetMax platform.
 * This class provides various device specific methods, like registering a the device with GCM registration id.
 */
public class Device implements Parcelable {
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
        ApiCallbackHelper.executeCallback(callback, response);

        sCurrentDevice = response.body();
      }

      @Override public void onFailure(Throwable throwable) {
        Log.e(TAG, "registerDevice error : " + throwable.getMessage());
        ApiCallbackHelper.executeCallback(callback, throwable);
      }
    });
    call.executeInBackground();
  }

  public static void unRegister(final ApiCallback<Boolean> callback) {
    getDeviceService().unRegisterDevice(getCurrentDeviceId(), new Callback<Boolean>() {
      @Override public void onResponse(Response<Boolean> response) {
        ApiCallbackHelper.executeCallback(callback, response);
      }

      @Override public void onFailure(Throwable throwable) {
        ApiCallbackHelper.executeCallback(callback, throwable);
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

  /**
   * Compares this Device object with the specified object and indicates if they
   * are equal. Following properties are compared :
   * <p><ul>
   * <li>deviceId
   * <li>deviceToken
   * <li>os
   * <li>pushAuthority
   * <li>deviceStatus
   * </ul><p>
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if(!EqualityUtil.quickCheck(this, obj)) {
      return false;
    }

    Device theOther = (Device) obj;
    return StringUtil.isStringValueEqual(deviceId, theOther.getDeviceId()) &&
        StringUtil.isStringValueEqual(deviceToken, theOther.getDeviceToken()) &&
        os == theOther.getOs() &&
        pushAuthority == theOther.getPushAuthority() &&
        deviceStatus == theOther.getDeviceStatus();
  }

  /**
   *  Returns an integer hash code for this object.
   *  @see #equals(Object) for the properties used for hash calculation.
   * @return
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().hash(deviceId).hash(deviceToken).hash(os)
        .hash(pushAuthority).hash(deviceStatus).hashCode();
  }

  @Override public String toString() {
    return new StringBuilder().append("deviceId = ").append(deviceId).append(", ")
        .append("deviceToken = ").append(deviceToken).append(", ")
        .append("os = ").append(os).append(", ")
        .append("pushAuthority = ").append(pushAuthority).append(", ")
        .append("deviceStatus = ").append(deviceStatus)
        .toString();
  }

  //----------------Parcelable Methods----------------

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeStringArray(this.tags);
    dest.writeInt(this.os == null ? -1 : this.os.ordinal());
    dest.writeString(this.osVersion);
    dest.writeString(this.deviceToken);
    dest.writeString(this.userId);
    dest.writeInt(this.deviceStatus == null ? -1 : this.deviceStatus.ordinal());
    dest.writeString(this.label);
    dest.writeInt(this.pushAuthority == null ? -1 : this.pushAuthority.ordinal());
    dest.writeString(this.deviceId);
  }

  protected Device() {
  }

  protected Device(Parcel in) {
    this.tags = in.createStringArray();
    int tmpOs = in.readInt();
    this.os = tmpOs == -1 ? null : OsType.values()[tmpOs];
    this.osVersion = in.readString();
    this.deviceToken = in.readString();
    this.userId = in.readString();
    int tmpDeviceStatus = in.readInt();
    this.deviceStatus = tmpDeviceStatus == -1 ? null : DeviceStatus.values()[tmpDeviceStatus];
    this.label = in.readString();
    int tmpPushAuthority = in.readInt();
    this.pushAuthority =
        tmpPushAuthority == -1 ? null : PushAuthorityType.values()[tmpPushAuthority];
    this.deviceId = in.readString();
  }

  public static final Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>() {
    public Device createFromParcel(Parcel source) {
      return new Device(source);
    }

    public Device[] newArray(int size) {
      return new Device[size];
    }
  };
}
