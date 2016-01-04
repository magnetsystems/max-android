/*
 *  Copyright (c) 2016 Magnet Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.magnet.max.android.util;

import android.os.Bundle;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

public class ParcelableHelper {
  public static Bundle toBundle(Map<String, ? extends Parcelable> input) {
    Bundle output = new Bundle();
    for(String key : input.keySet()) {
      output.putParcelable(key, input.get(key));
    }
    return output;
  }

  public static <T extends Parcelable> Map<String, T> fromBundle(Bundle input, Class<T> c) {
    Map<String, T> output = new HashMap<String, T>();
    for(String key : input.keySet()) {
      output.put(key, c.cast(input.getParcelable(key)));
    }
    return output;
  }

  public static Bundle stringMapToBundle(Map<String, String> input) {
    Bundle output = new Bundle();
    if(null != input) {
      for (String key : input.keySet()) {
        output.putString(key, input.get(key));
      }
    }
    return output;
  }

  public static Map<String, String> stringMapfromBundle(Bundle input) {
    Map<String, String> output = new HashMap<String, String>();
    for(String key : input.keySet()) {
      output.put(key, input.getString(key));
    }
    return output;
  }
}
