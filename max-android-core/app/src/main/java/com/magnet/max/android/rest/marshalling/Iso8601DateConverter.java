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
import com.magnet.max.android.util.StringUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Iso8601DateConverter {
  private static final String TAG = "Iso8601DateConverter";

  public static final String ISO8601DateFormat_WITH_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  public static final int ISO8601DateFormat_WITH_MS_LENGTH = 24;
  public static final String ISO8601DateFormat_WO_MS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  public static final int ISO8601DateFormat_WO_MS_LENGTH = 20;
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat(ISO8601DateFormat_WITH_MS, Locale.US);
  private static final SimpleDateFormat dateFormat_wo_ms = new SimpleDateFormat(ISO8601DateFormat_WO_MS, Locale.US);

  public static String toString(Date date) {
    return dateFormat.format(date);
  }

  public static Date fromString(String dateStr) {
    if(StringUtil.isNotEmpty(dateStr)) {
      try {
        if(dateStr.length() == ISO8601DateFormat_WITH_MS_LENGTH) {
          return dateFormat.parse(dateStr);
        } else if(dateStr.length() == ISO8601DateFormat_WO_MS_LENGTH) {
          return dateFormat_wo_ms.parse(dateStr);
        } else {
          Log.e(TAG, "Date string format is not supported : " + dateStr);
        }
      } catch (ParseException e) {
        Log.e(TAG, "Failed to parse date " + dateStr + " due to \n" + e.getMessage());
      }
    }

    return null;
  }
}
