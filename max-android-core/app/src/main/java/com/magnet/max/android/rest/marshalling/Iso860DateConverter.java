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
package com.magnet.max.android.rest.marshalling;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Iso860DateConverter {
  public static final String ISO8601DateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  private static SimpleDateFormat dateFormat = new SimpleDateFormat(ISO8601DateFormat, Locale.US);

  public static String toString(Date date) {
    return dateFormat.format(date);
  }

  public static Date fromString(String dateStr) {
    try {
      return dateFormat.parse(dateStr);
    } catch (ParseException e) {
      Log.e("Iso860DateConverter", "Failed to parse date " + dateStr + " due to \n" + e.getMessage());
    }

    return null;
  }
}
