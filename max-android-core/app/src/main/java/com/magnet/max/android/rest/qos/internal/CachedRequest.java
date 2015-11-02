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

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import okio.Buffer;

public class CachedRequest extends CachedHttpRepresentation {
  private String url;
  private String method;

  public CachedRequest() {
  }

  public CachedRequest(Request request) {
    url = request.urlString();
    method = request.method();

    parseHeaders(request.headers());

    if(null != request.body()) {
      try {
        Buffer buffer = new Buffer();
        request.body().writeTo(buffer);
        body = CacheUtils.copyBody(buffer);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Request toRequest() {
    Headers responseHeaders = Headers.of(headers);
    String contentType = responseHeaders.get("Content-Type");
    return new Request.Builder()
        .url(url)
        .method(method, RequestBody.create(MediaType.parse(contentType), body))
        .headers(responseHeaders)
        .build();
  }

  public String getUrl() {
    return url;
  }

  public String getMethod() {
    return method;
  }
}
