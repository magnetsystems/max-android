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

import java.util.ArrayList;

public class MCPResponse extends MCPCommandsEnvelope {

  private MCPResponse() {
    super();
    this.op = MCPOperationType.RESPONSE;
  }

  public static class Builder {
    private MCPResponse toBeBuilt = new MCPResponse();

    public Builder id(String id) {
      toBeBuilt.id = id;
      return this;
    }

    public Builder sid(String sid) {
      toBeBuilt.sid = sid;
      return this;
    }

    public Builder sender(String sender) {
      toBeBuilt.sender = sender;
      return this;
    }

    public Builder command(MCPCommand command) {
      if(null == toBeBuilt.getCommands()) {
        toBeBuilt.commands = new ArrayList<MCPCommand>();
      }
      toBeBuilt.commands.add(command);
      return this;
    }

    public MCPResponse build() {
      return toBeBuilt;
    }
  }
}
