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

import android.util.Base64;
import java.io.UnsupportedEncodingException;

public class AuthUtil {

  public static final String AUTH_PREFIX_BASIC = "Basic ";
  public static final String AUTH_PREFIX_BEARER = "Bearer ";

  public static String generateBasicAuthToken(String name, String secret) {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append(":").append(secret);
    try {
      return AUTH_PREFIX_BASIC + Base64.encodeToString(sb.toString().getBytes("UTF-8"), Base64.URL_SAFE|Base64.NO_WRAP);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static String generateOAuthToken(String token) {
    return AUTH_PREFIX_BEARER + token;
  }
}
