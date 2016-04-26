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

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.magnet.max.android.util.MagnetUtils;
import com.magnet.max.android.util.TypeUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import retrofit.Converter;

public class MagnetGsonConverter<T> implements Converter<T> {
  private static final String TAG = "MagnetGsonConverter";

  private static final MediaType MEDIA_TYPE_JSON = MediaType.parse(
      "application/json; charset=UTF-8");
  private final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain; charset=UTF-8");
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private static final TypeToken BYTE_ARRAY_TYPE_TOKEN = new TypeToken<byte[]>() {};

  private final TypeToken<T> typeToken;

  public MagnetGsonConverter(TypeToken<T> typeToken) {
    this.typeToken = typeToken;
  }


  @Override public T fromBody(ResponseBody responseBody) throws IOException {
    if(GsonDecorator.getInstance().isVoidType(typeToken.getType())) {
      return null;
    }

    if(typeToken.getRawType().equals(ResponseBody.class)) {
      return (T) responseBody;
    }

    if(GsonDecorator.getInstance().isByteArray(typeToken)) {
      return (T) responseBody.bytes();
    }

    //Charset charset = UTF_8;
    //if (responseBody.contentType() != null) {
    //  charset = responseBody.contentType().charset(charset);
    //}

    if(GsonDecorator.getInstance().isSimpleType(typeToken)) {
      return (T) GsonDecorator.getInstance().unmarshalSimpleType(responseBody.string(), typeToken.getType());
    } else {
      Reader in = responseBody.charStream();
      try {
        return (T) GsonDecorator.getInstance().fromJson(responseBody.charStream(), typeToken); //typeAdapter.fromJson(in);
      } catch (Exception e) {
        Log.e(TAG, "Error in fromBody \n" + e.getMessage());
        throw e;
      } finally {
        try {
          in.close();
        } catch (IOException ignored) {
        }
      }
    }
  }

  @Override public RequestBody toBody(T value) {
    MediaType mediaType = null;
    if(GsonDecorator.getInstance().isBasicType(typeToken.getType())) {
      mediaType = MEDIA_TYPE_TEXT;
    } else if(GsonDecorator.getInstance().isDateType(typeToken.getType()) ||
        GsonDecorator.getInstance().isEnum(typeToken.getType())) {
      mediaType = MEDIA_TYPE_JSON;
    } else if(value instanceof RequestBody) {
      return (RequestBody) value;
    } else {
      mediaType = MEDIA_TYPE_JSON;
      //Buffer buffer = new Buffer();
      //Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
      //try {
      //  typeAdapter.toJson(writer, value);
      //  writer.flush();
      //  json = buffer.readUtf8();
      //} catch (IOException e) {
      //  throw new AssertionError(e); // Writing to Buffer does no I/O.
      //}
    }
    return RequestBody.create(mediaType, GsonDecorator.getInstance().toJson(value));
  }


  private String readerToString(Reader ir) {
    try {
      StringWriter stringWriter = new StringWriter();
      CharBuffer charBuffer = CharBuffer.allocate(1024);
      int length = 0;
      while ((length = ir.read(charBuffer)) != -1) {
        stringWriter.write(charBuffer.array(), 0, length);
      }

      return stringWriter.toString();
    } catch (IOException e) {
      Log.e(TAG, "Failed to read InputStreamReader to string due to \n" + e.getMessage());
    }

    return null;
  }
}
