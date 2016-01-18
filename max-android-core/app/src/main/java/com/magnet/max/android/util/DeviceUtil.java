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
package com.magnet.max.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceUtil {
  private static final String SHARED_PREF_FILENAME = "com.magnet.max.android.device";
  private static final String KEY_DEVICE_ID = "DEVICE_ID";

  static AtomicReference<String> uniqueIdRef = new AtomicReference<String>(null);

  public static String getDeviceId(Context context) {
    String result = uniqueIdRef.get();
    if (result == null) {
      result = getOrGenerateSimpleDeviceId(context);
    }
    return result;
  }

  private static synchronized String getOrGenerateDeviceId(Context context) {
    if (null == context) {
      throw new IllegalArgumentException("Context may not be null");
    }

    if(uniqueIdRef.get() != null) {
      return uniqueIdRef.get();
    }

    SharedPreferences shared =
        context.getApplicationContext().
            getSharedPreferences(SHARED_PREF_FILENAME, Context.MODE_PRIVATE);

    String result = shared.getString(KEY_DEVICE_ID, null);
    if (StringUtil.isEmpty(result)) {
      String devId = generateDeviceId(context);
      // emulator or rogue prototype devices that don't have dev ID
      if (isAndroidEmulator() || StringUtil.isEmpty(devId) ) {
        UUID uuid = UUID.randomUUID();
        result = Long.toString(uuid.getMostSignificantBits(), 36) + Long.toString(uuid.getLeastSignificantBits(), 36);
      } else {
        try {
          MessageDigest md = MessageDigest.getInstance("SHA-1");
          byte[] hash = md.digest(devId.getBytes());
          result = toBaseNString(hash, 36);
        } catch (NoSuchAlgorithmException e) {
          // convert to base36 string
          result = toBaseNString(devId.getBytes(), 36);
        }
      }
      // save it to shared preferences
      shared.edit()
          .putString(KEY_DEVICE_ID, result)
          .apply();
    }
    uniqueIdRef.set(result);
    return result;
  }

  private static synchronized String getOrGenerateSimpleDeviceId(Context context) {
    if (null == context) {
      throw new IllegalArgumentException("Context may not be null");
    }

    if(uniqueIdRef.get() != null) {
      return uniqueIdRef.get();
    }

    SharedPreferences shared =
        context.getApplicationContext().
            getSharedPreferences(SHARED_PREF_FILENAME, Context.MODE_PRIVATE);

    String result = shared.getString(KEY_DEVICE_ID, null);
    if (StringUtil.isEmpty(result)) {
      result = UUID.randomUUID().toString();
      // save it to shared preferences
      shared.edit()
          .putString(KEY_DEVICE_ID, result)
          .apply();
    }
    uniqueIdRef.set(result);
    return result;
  }

  private static String generateDeviceId(Context context) {
    final TelephonyManager tm = (TelephonyManager) context.getSystemService(
        Context.TELEPHONY_SERVICE);

    final String tmDevice, tmSerial, androidId;
    tmDevice = "" + tm.getDeviceId();
    tmSerial = "" + tm.getSimSerialNumber();
    androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
    return deviceUuid.toString();
  }

  /**
   * Return true if running in emulator
   */
  private static boolean isAndroidEmulator() {
    return Build.FINGERPRINT.contains("generic");
  }

  private static String toBaseNString(byte[] bytes, int base) {
    //pad the byte array
    int remaining = bytes.length % 8;
    byte[] paddedArray = bytes;
    if (remaining != 0) {
      paddedArray = new byte[bytes.length + (8 - remaining)];
      System.arraycopy(bytes, 0, paddedArray, 0, bytes.length);
    }

    ByteBuffer bb = ByteBuffer.wrap(paddedArray);
    StringBuffer result = new StringBuffer();
    long curLong;
    while (bb.position() != bb.limit()) {
      curLong = bb.getLong();
      result.append(Long.toString(curLong & Long.MAX_VALUE, base));
    }
    return result.toString();
  }

}
