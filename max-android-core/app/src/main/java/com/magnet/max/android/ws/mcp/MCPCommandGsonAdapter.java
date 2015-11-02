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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.magnet.max.android.ws.mcp.commands.ExecuteApiCommand;
import com.magnet.max.android.ws.mcp.commands.ExecuteApiResCommand;

import java.io.IOException;

public class MCPCommandGsonAdapter implements TypeAdapterFactory {
  @Override
  public <T> TypeAdapter<T> create (final Gson gson, final TypeToken<T> type) {
    if (!MCPCommand.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    final TypeAdapter<T> delegate = gson.getDelegateAdapter (this, type);

    return new TypeAdapter<T> () {
      @Override
      public void write (final JsonWriter jsonWriter, final T t) throws IOException {
        delegate.write (jsonWriter, t);
      }

      @Override
      public T read (final JsonReader jsonReader) throws IOException, JsonParseException {
        JsonElement tree = Streams.parse(jsonReader);
        JsonObject object = tree.getAsJsonObject();
        String commandName = object.get("name").getAsString();
        Class clazz = null;
        if (commandName.equalsIgnoreCase(ExecuteApiCommand.COMMAND_NAME)) {
          clazz = ExecuteApiCommand.class;
        } else if (commandName.equalsIgnoreCase(ExecuteApiResCommand.COMMAND_NAME)) {
          clazz = ExecuteApiResCommand.class;
        }
        if(null != clazz) {
          return (T) gson.getDelegateAdapter(MCPCommandGsonAdapter.this, TypeToken.get(clazz)).fromJsonTree(tree);
        }

        throw new JsonParseException ("Cannot deserialize " + type + ". It is not a valid SuperType JSON.");
      }
    };
  }
}
