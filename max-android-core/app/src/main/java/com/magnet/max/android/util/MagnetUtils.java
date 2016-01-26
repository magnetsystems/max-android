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

import android.util.Log;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MagnetUtils {

  public static boolean isStringNotEmpty(String s) {
    return null != s && s.length() > 0;
  }

  public static boolean isWebSocketEndpoint(String url) {
    String urlLowerCase = url.toLowerCase(Locale.US);
    return urlLowerCase.startsWith("ws://") || urlLowerCase.startsWith("wss://");
  }

  public static String trimQuotes(String s) {
    if(null != s && s.length() > 1) {
      if (s.startsWith("\"\\\"") && s.endsWith("\\\"\"")) {
        Log.d("MagnetUtils", "String " + s + " starting with \\\"");
        s = s.substring(3, s.length() - 3);
      } else if (s.startsWith("\"") && s.endsWith("\"")) {
        s = s.substring(1, s.length() - 1);
      }
    }

    return s;
  }

  public static String setToString(Set<?> set) {
    StringBuilder sb = new StringBuilder("[");
    if(null != set ) {
      for(Object o : set) {
        sb.append(o).append(", ");
      }
      sb.append("]");
    }
    sb.append("]");

    return sb.toString();
  }

  public static boolean isMapEquals(Map<String, String> map1, Map<String, String> map2) {
    if(null == map1) {
      return null == map2;
    }

    if(map1.size() != map2.size()) {
      return false;
    }

    Set<String> keys = map1.keySet();
    for(String k : keys) {
      if(!StringUtil.isStringValueEqual(map1.get(k), map2.get(k))) {
        return false;
      }
    }

    return true;
  }

  public static boolean isMapContainedIn(Map<String, String> map1, Map<String, String> map2) {
    if(null == map1) {
      return true;
    }

    if(null == map2) {
      return false;
    }

    if(map1.size() > map2.size()) {
      return false;
    }

    Set<String> keys = map1.keySet();
    for(String k : keys) {
      if(!StringUtil.isStringValueEqual(map1.get(k), map2.get(k))) {
        return false;
      }
    }

    return true;
  }
}
