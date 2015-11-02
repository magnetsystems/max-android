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
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import okio.Buffer;

public class CachedResponse extends CachedHttpRepresentation {
  private int code;
  private String protocol;
  private String message;

  public CachedResponse() {
  }

  public CachedResponse(Response response) {
    this.code = response.code();
    this.protocol = response.protocol().toString();
    this.message = response.message();

    parseHeaders(response.headers());

    if(null != response.body()) {
      try {
        Buffer buffer = new Buffer();
        response.body().source().readAll(buffer);
        body = CacheUtils.copyBody(buffer);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Response toResponse(Request request) {
    Headers responseHeaders = Headers.of(headers);
    String contentType = responseHeaders.get("Content-Type");
    Response cachedResponse = new Response.Builder()
        .code(code).protocol(getProtocolEnum()).message(message)
        .headers(responseHeaders)
        .request(request).build();
    return cachedResponse.newBuilder().body(ResponseBody.create(MediaType.parse(contentType), body))
        .cacheResponse(cachedResponse).build();
  }

  public int getCode() {
    return code;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getMessage() {
    return message;
  }

  public Protocol getProtocolEnum() {
    if(null != protocol) {
      try {
        return Protocol.get(protocol);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return Protocol.HTTP_1_1;
  }
}
