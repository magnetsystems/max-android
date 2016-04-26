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
package com.magnet.max.android.rest.marshalling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import retrofit.Converter;

public class MagnetGsonConverterFactory implements Converter.Factory {
  /**
   * Create an instance using a default {@link Gson} instance for conversion. Encoding to JSON and
   * decoding from JSON (when no charset is specified by a header) will use UTF-8.
   */
  public static MagnetGsonConverterFactory create() {
    //return create(null);
    return new MagnetGsonConverterFactory();
  }

  /**
   * Create an instance using {@code gson} for conversion. Encoding to JSON and
   * decoding from JSON (when no charset is specified by a header) will use UTF-8.
   */
  //public static MagnetGsonConverterFactory create(Gson gson) {
  //  return new MagnetGsonConverterFactory(gson);
  //}
  //
  //private final Gson gson;
  //
  //private MagnetGsonConverterFactory(Gson gson) {
  //  if (gson == null) {
  //    gson = new GsonBuilder()
  //        .setDateFormat(Iso8601DateConverter.ISO8601DateFormat_WITH_MS)
  //            //.registerTypeAdapter(Date.class, new DateTypeAdapter())
  //        .create();
  //  };
  //  this.gson = gson;
  //}

  @Override public Converter<?> get(Type type) {
    TypeToken typeToken = TypeToken.get(type);
    return new MagnetGsonConverter<>(typeToken);
  }
}
