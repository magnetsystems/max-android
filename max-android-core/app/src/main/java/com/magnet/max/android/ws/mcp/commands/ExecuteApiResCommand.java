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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.magnet.max.android.util.MagnetUtils;
import com.magnet.max.android.ws.mcp.MCPCommand;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Command to send the response of a REST API call
 */
public class ExecuteApiResCommand extends MCPCommand<ExecuteApiResCommand.HTTPResponsePayload> {
  public static final String COMMAND_NAME = ExecuteApiCommand.COMMAND_NAME + "Res";

  @Expose
  private Response response;

  /**
   * private, user {@link Builder} instead
   */
  private ExecuteApiResCommand() {
    super();

    this.name = COMMAND_NAME;
  }

  public Response getResponse(Request request) {
    if(null == response) {
      HTTPResponsePayload responsePayload = getPayload();
      if(null != responsePayload) {
        Response.Builder builder = new Response.Builder();
        builder.request(request)
                .protocol(Protocol.SPDY_3)
                .code(payload.getStatus())
                .body(ResponseBody.create(MediaType.parse("application/json"), responsePayload.getBody()));
        if(null != responsePayload.getHeaders()) {
          for(Map.Entry<String, String> entry : responsePayload.getHeaders().entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
          }
        }

        response = builder.build();
      }
    }
    return response;
  }

  /**
   * Payload of response of a REST API call
   */
  public static class HTTPResponsePayload {
    private int status;
    private String reason;
    private Map<String, String> headers;
    private String body;

    public int getStatus() {
      return status;
    }

    public String getReason() {
      return reason;
    }

    public Map<String, String> getHeaders() {
      return headers;
    }

    public String getBody() {
      return body;
    }

    /**
     * Need a custom deserializer because the body should not parsed here
     */
    public static class HTTPResponsePayloadDeserializer implements JsonDeserializer<HTTPResponsePayload> {
      @Override
      public HTTPResponsePayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HTTPResponsePayload result = new HTTPResponsePayload();
        JsonObject jsonObject = json.getAsJsonObject();
        result.status = jsonObject.get("status").getAsInt();
        result.reason = jsonObject.get("reason").getAsString();
        result.headers = context.deserialize(jsonObject.get("headers"), new TypeToken<Map<String, String>>() {}.getType());
        try {
          result.body = MagnetUtils.trimQuotes(jsonObject.get("body").toString());
        } catch (Throwable e) {
          e.printStackTrace();
        }
        return result;
      }
    }
  }
}
