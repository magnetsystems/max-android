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

/**
 * File generated by Magnet Magnet Lang Tool on Jun 16, 2015 10:53:47 AM
 * @see {@link http://developer.magnet.com}
 */
package com.magnet.max.android;

import com.magnet.max.android.auth.model.AppLoginResponse;
import com.magnet.max.android.auth.model.AppLoginWithDeviceResponse;
import com.magnet.max.android.auth.model.DeviceInfo;
import java.util.Map;
import retrofit.MagnetCall;
import retrofit.http.*;

/** public : used in adapter only, hide fro developer m**/ interface ApplicationService {

  /**
   * 
   * POST /api/applications/session
   * @param callback asynchronous callback
   */
  @POST("/api/com.magnet.server/applications/session")
  MagnetCall<AppLoginResponse> appCheckin(@Header("MMS-DEVICE-ID") String deviceId,
      @Header("Authorization") String authorization,
      retrofit.Callback<AppLoginResponse> callback);

  /**
   *
   * POST /com.magnet.server/applications/session-device
   * @param deviceInfo style:Body optional:false
   * @param callback asynchronous callback
   */
  @POST("/api/com.magnet.server/applications/session-device")
  MagnetCall<AppLoginWithDeviceResponse> checkInWithDevice(
      @Header("Authorization") String authorization,
      @Body DeviceInfo deviceInfo,
      retrofit.Callback<AppLoginWithDeviceResponse> callback
  );

  /**
   *
   * GET /com.magnet.server/config/mobile
   * @param callback asynchronous callback
   */
  @GET("com.magnet.server/config/mobile")
  MagnetCall<Map<String, String>> getMobileConfig(
      retrofit.Callback<Map<String, String>> callback
  );
}
