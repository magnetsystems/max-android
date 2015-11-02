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
package com.magnet.max.android.ws.mcp.commands;

import com.google.gson.annotations.Expose;
import com.magnet.max.android.ws.WebSocketRequest;
import com.magnet.max.android.ws.mcp.MCPCommand;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Command to execute a REST API call
 */
public class ExecuteApiCommand extends MCPCommand<ExecuteApiCommand.HttpRequestPayload> {
  public static final String COMMAND_NAME = "executeApiCommand";

  @Expose
  private transient WebSocketRequest webSocketRequest;

  //private HttpPayload payload;

  /**
   * private, user {@link Builder} instead
   */
  private ExecuteApiCommand() {
    super();

    this.name = COMMAND_NAME;
  }

  public WebSocketRequest getWebSocketRequest() {
    return webSocketRequest;
  }

  /**
   * Payload of request of a REST API call
   */
  public static class HttpRequestPayload {
    private String path;
    private String method;
    private Map<String, String> headers;
    private String body;

    public String getPath() {
      return path;
    }

    public String getMethod() {
      return method;
    }

    public Map<String, String> getHeaders() {
      return headers;
    }

    public String getBody() {
      return body;
    }
  }

  public static class Builder extends AbstractBuilder<ExecuteApiCommand, Builder> {
    public Builder() {
      toBeBuilt = new ExecuteApiCommand();
      builder = this;
    }

    public Builder request(WebSocketRequest webSocketRequest, HttpUrl endpoint) {
      toBeBuilt.webSocketRequest = webSocketRequest;

      HttpRequestPayload executeApiPayload = new HttpRequestPayload();

//      try {
//        URI uri = new URI(webSocketRequest.getRequest().urlString());
//        executeApiPayload.path = null == uri.getQuery() ? uri.getPath() : uri.getPath() + "?" + uri.getQuery();
//      } catch (URISyntaxException e) {
//        e.printStackTrace();
//      }
      executeApiPayload.path = webSocketRequest.getRequest().urlString().substring(endpoint.url().toString().length());
      executeApiPayload.method = webSocketRequest.getRequest().method();

      Headers requestHeaders = webSocketRequest.getRequest().headers();
      if(null != requestHeaders) {
        executeApiPayload.headers = new HashMap<String, String>();
        for(int i = 0; i < requestHeaders.size(); i++) {
          executeApiPayload.headers.put(requestHeaders.name(i), requestHeaders.value(i));
        }
      }
      // Set content-type
      if(null != webSocketRequest.getRequest().body() && null != webSocketRequest.getRequest().body().contentType()) {
        executeApiPayload.headers.put("content-type", webSocketRequest.getRequest().body().contentType().toString());
      }

      if(null != webSocketRequest.getRequest().body()) {
        okio.Buffer bufferedSink = new okio.Buffer();
        try {
          webSocketRequest.getRequest().body().writeTo(bufferedSink);
          executeApiPayload.body = bufferedSink.readUtf8();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      toBeBuilt.payload = executeApiPayload;

      return this;
    }
  }
}
