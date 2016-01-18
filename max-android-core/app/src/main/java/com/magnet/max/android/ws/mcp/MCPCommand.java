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
package com.magnet.max.android.ws.mcp;

import com.google.gson.annotations.Expose;
import com.magnet.max.android.ws.WebsocketStatus;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MCPCommand<T> {
  private static final AtomicLong requestIdGenerator = new AtomicLong(0);

  protected String name;
  protected String cid;
  protected MCPPriority priority;
  protected T payload;
  @Expose
  private WebsocketStatus status;

  /**
   * private, user {@link MCPCommand.Builder} instead
   */
  protected MCPCommand() {
    cid = String.valueOf(requestIdGenerator.getAndIncrement());
  }

  public String getName() {
    return name;
  }

  public String getCid() {
    return cid;
  }

  public MCPPriority getPriority() {
    return priority;
  }

  public T getPayload() {
    return payload;
  }

  public WebsocketStatus getStatus() {
    return status;
  }

  public void setStatus(WebsocketStatus status) {
    this.status = status;
  }

  public static class Builder extends AbstractBuilder<MCPCommand, Builder> {

    public Builder() {
      toBeBuilt = new MCPCommand();
      builder = this;
    }

    public Builder name(String cn) {
      toBeBuilt.name = cn;
      return builder;
    }
  }

  protected static class AbstractBuilder<T extends MCPCommand, B extends AbstractBuilder> {
    protected T toBeBuilt;
    protected B builder;

//    public B id(String id) {
//      toBeBuilt.cid = id;
//      return builder;
//    }

    public B priority(MCPPriority priority) {
      toBeBuilt.priority = priority;
      return builder;
    }

    public T build() {
      toBeBuilt.cid = UUID.randomUUID().toString();
      //toBeBuilt.stm = System.currentTimeMillis();
      return toBeBuilt;
    }
  }
}
