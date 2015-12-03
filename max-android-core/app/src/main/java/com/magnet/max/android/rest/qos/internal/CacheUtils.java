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

import android.util.Log;
import com.magnet.max.android.util.StringUtil;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.internal.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import okio.Buffer;

public class CacheUtils {
  private static final String TAG = CacheUtils.class.getSimpleName();

  public static final int MAX_CONTENT_LENGTH_TO_HASH = 4 * 1024;

  public static String getRequestHash(Request request) {
    StringBuilder sb = new StringBuilder();
    //Method and URL
    sb.append(request.method()).append(request.urlString());
    //Headers
    if(null != request.headers()) {

    }
    //Body
    //Don't include body when it's multipart
    if(toHashBody(request)) {
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

  private static boolean toHashBody(Request request) {
    if(null == request.body()) {
      return false;
    }

    if(null != request.body().contentType()) {
      String mediaType = request.body().contentType().type();
      if(StringUtil.isNotEmpty(mediaType)) {
        mediaType = mediaType.trim().toLowerCase();
        if ((mediaType.startsWith("multipart") || mediaType.startsWith("image") ||
            mediaType.startsWith("video") || mediaType.startsWith("audio"))) {
          return false;
        }
      }
    }

    try {
      long length = request.body().contentLength();
      if(length > MAX_CONTENT_LENGTH_TO_HASH) {
        return false;
      }
    } catch (IOException e) {
      Log.e(TAG, "Failed to get body length", e);
      return false;
    }

    return true;
  }
}
