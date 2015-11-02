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

import java.util.Map;

/**
 * This interface defines properties needed to initialize Magnet Max
 */
public interface MaxAndroidConfig {

  /**
   * The base URL of the Magnet Max server
   */
  String getBaseUrl();

  /**
   * The clientId of the app
   */
  String getClientId();

  /**
   * The clientId secret the app
   */
  String getClientSecret();

  /**
   * The scope of the app
   */
  String getScope();

  /**
   * All configuration properties in key-value pair
   */
  Map<String, String> getAllConfigs();
}
