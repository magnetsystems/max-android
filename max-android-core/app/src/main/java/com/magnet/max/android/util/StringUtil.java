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

public class StringUtil {
  public static boolean isNotEmpty(String s) {
    return null != s && s.length() > 0;
  }
  public static boolean isEmpty(String s) {
    return null == s || s.length() == 0;
  }

  public static boolean isStringValueEqual(String s1, String s2) {
    if(null == s1) {
      return null == s2;
    } else {
      return s1.equals(s2);
    }
  }

  public static int getHash(String s) {
    return isNotEmpty(s) ? s.hashCode() : 0;
  }
}
