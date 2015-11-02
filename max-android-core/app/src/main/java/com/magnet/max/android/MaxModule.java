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

import android.content.Context;
import java.util.Map;

/**
 * MaxModule defines lifecycle events in Max.
 */
public interface MaxModule {

  /**
   * The name of the service, only used for decription
   * @return
   */
  String getName();

  /**
   * First method will be called after it's instantiated
   * @param context
   * @param configs
   * @param callback
   */
  void onInit(Context context, Map<String, String> configs, ApiCallback<Boolean> callback);


  /**
   * Called when application access token is obtained or updated
   * @param appToken
   * @param appId
   * @param deviceId
   */
  void onAppTokenUpdate(String appToken, String appId, String deviceId);

  /**
   * Called when user access token is obtained or updated
   * @param userToken
   * @param userId
   * @param deviceId
   */
  void onUserTokenUpdate(String userToken, String userId, String deviceId);

  /**
   * Called when MagnetServiceAdapter is closed
   * @param gracefully
   */
  void onClose(boolean gracefully);

  /**
   * Called when user logout @see User.logout
   */
  void onUserTokenInvalidate();

  /**
   *
   */
  void deInitModule();
}
