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
package com.magnet.max.android.ws;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;

import retrofit.WebsocketTransport;

public class WebSocketRequest {

  private final Request request;
  private final com.squareup.okhttp.Callback callback;
  private final WebsocketTransport websocketTransport;
  private String requestId;
  private String commandId;
  //private CancelStatus cancelStatus;

  public WebSocketRequest(Request request, com.squareup.okhttp.Callback callback, WebsocketTransport websocketTransport) {
    this.request = request;
    this.callback = callback;
    this.websocketTransport = websocketTransport;
  }

  public Request getRequest() {
    return request;
  }

  public Callback getCallback() {
    return callback;
  }

  public WebsocketTransport getWebsocketTransport() {
    return websocketTransport;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getCommandId() {
    return commandId;
  }

  public void setCommandId(String commandId) {
    this.commandId = commandId;
  }

  //public CancelStatus cancel() {
  //  if(null != cancelStatus) {
  //    cancelStatus = websocketTransport.cancel(requestId, commandId);
  //  }
  //
  //  return cancelStatus;
  //}
}
