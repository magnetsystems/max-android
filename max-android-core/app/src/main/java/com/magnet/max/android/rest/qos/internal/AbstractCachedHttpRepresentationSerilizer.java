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

import android.util.Base64;
import com.activeandroid.serializer.TypeSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

abstract class AbstractCachedHttpRepresentationSerilizer extends TypeSerializer {
  private static Gson gson;

  @Override public String serialize(Object data) {
    if(null == data) {
      return null;
    }
    return getGson().toJson(data);
  }

  protected Gson getGson() {
    if(null == gson) {
      gson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
          new ByteArrayToBase64TypeAdapter()).create();
    }

    return gson;
  }


  // Using Android's base64 libraries. This can be replaced with any base64 library.
  private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>,
      JsonDeserializer<byte[]> {
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
        JsonParseException {
      return Base64.decode(json.getAsString(), Base64.NO_WRAP);
    }


    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(Base64.encodeToString(src, Base64.NO_WRAP));
    }
  }
}
