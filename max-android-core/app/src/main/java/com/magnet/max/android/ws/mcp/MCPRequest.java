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

import java.util.ArrayList;
import java.util.UUID;

public class MCPRequest extends MCPCommandsEnvelope {

  public enum MCPExecutionType {
    PARALLEL,
    SEQUENCED,
    PIPELINED
  }

  private MCPExecutionType execution;
  private MCPPriority priority;
  @Expose
  private WebsocketStatus status;

  /**
   * private, user {@link MCPRequest.Builder} instead
   */
  private MCPRequest() {
    super();
    this.op = MCPOperationType.REQUEST;
  }

  public MCPExecutionType getExecution() {
    return execution;
  }

  public MCPPriority getPriority() {
    return priority;
  }

  public WebsocketStatus getStatus() {
    return status;
  }

  public void setStatus(WebsocketStatus status) {
    this.status = status;
    for(MCPCommand command : commands) {
      command.setStatus(status);
    }
  }

  public boolean isCancelled() {
    if(status == WebsocketStatus.CANCELLED) {
      return true;
    }

    if(getCommands().size() == 1 && getCommands().get(0).getStatus() == WebsocketStatus.CANCELLED) {
      return true;
    }

    return false;
  }

  public static class Builder {
    private MCPRequest toBeBuilt = new MCPRequest();

    public Builder sid(String sid) {
      toBeBuilt.sid = sid;
      return this;
    }

    public Builder sender(String sender) {
      toBeBuilt.sender = sender;
      return this;
    }

    public Builder executionType(MCPExecutionType executionType) {
      toBeBuilt.execution = executionType;
      return this;
    }

    public Builder priority(MCPPriority priority) {
      toBeBuilt.priority = priority;
      return this;
    }

    public Builder command(MCPCommand command) {
      if(null == toBeBuilt.getCommands()) {
        toBeBuilt.commands = new ArrayList<MCPCommand>();
      }
      toBeBuilt.commands.add(command);
      return this;
    }

    public MCPRequest build() {
      toBeBuilt.id = UUID.randomUUID().toString();
      return toBeBuilt;
    }
  }
}
