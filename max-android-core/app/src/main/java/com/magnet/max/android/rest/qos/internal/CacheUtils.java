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
package com.magnet.max.android.rest.qos.internal;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.internal.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import okio.Buffer;

public class CacheUtils {
  public static String getRequestHash(Request request) {
    StringBuilder sb = new StringBuilder();
    //Method and URL
    sb.append(request.method()).append(request.urlString());
    //Headers
    if(null != request.headers()) {

    }
    //Body
    if(null != request.body()) {
      try {
        Buffer buffer = new Buffer();
        Request copy = request.newBuilder().build();
        copy.body().writeTo(buffer);
        sb.append(buffer.readUtf8());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return Util.md5Hex(sb.toString());
  }

  public static byte[] copyBody(Buffer buffer) {
    ByteArrayOutputStream os = null;
    try {
      os = new ByteArrayOutputStream();
      buffer.copyTo(os);
      return os.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if(null != os) {
        try {
          os.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return null;
  }
}
