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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

  public static long getUtcTimestamp() {
    Calendar c = Calendar.getInstance();
    System.out.println("current: "+c.getTime());

    TimeZone z = c.getTimeZone();
    int offset = z.getRawOffset();
    if(z.inDaylightTime(new Date())){
      offset = offset + z.getDSTSavings();
    }
    int offsetHrs = offset / 1000 / 60 / 60;
    int offsetMins = offset / 1000 / 60 % 60;

    System.out.println("offset: " + offsetHrs);
    System.out.println("offset: " + offsetMins);

    c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
    c.add(Calendar.MINUTE, (-offsetMins));

    return c.getTime().getTime();
  }

}
