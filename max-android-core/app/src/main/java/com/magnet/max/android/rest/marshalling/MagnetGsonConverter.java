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

  private final TypeToken<T> typeToken;

  private final Gson gson;

  public MagnetGsonConverter(Gson gson, TypeToken<T> typeToken) {
    this.gson = gson;
    this.typeToken = typeToken;
  }


  @Override public T fromBody(ResponseBody responseBody) throws IOException {
    if(isVoidType(typeToken.getType())) {
      return null;
    }

    //Charset charset = UTF_8;
    //if (responseBody.contentType() != null) {
    //  charset = responseBody.contentType().charset(charset);
    //}

    if(isBasicType(typeToken.getType()) || isDateType(typeToken.getType()) || isEnum(typeToken.getType())) {
      return unmarshalBasicType(responseBody, typeToken.getType());
    } else {
      Reader in = responseBody.charStream();
      try {
        return gson.fromJson(responseBody.charStream(), typeToken.getType()); //typeAdapter.fromJson(in);
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
    String json = null;
    MediaType mediaType = null;
    if(isBasicType(typeToken.getType())) {
      json = marshalBasicType(value, typeToken.getType());
      mediaType = MEDIA_TYPE_TEXT;
    } else if(isDateType(typeToken.getType())) {
      json = Iso860DateConverter.toString((Date) value);
      mediaType = MEDIA_TYPE_JSON;
    } else if(isEnum(typeToken.getType())) {
      json = marshalBasicType(value, typeToken.getType());
      mediaType = MEDIA_TYPE_JSON;
    } else if(value instanceof RequestBody) {
      return (RequestBody) value;
    } else {
      mediaType = MEDIA_TYPE_JSON;

      json = gson.toJson(value);

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
    return RequestBody.create(mediaType, json);
  }

  private boolean isBasicType(Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(TypeUtil.isPrimitiveOrWrapper(clazz) ||
         clazz.equals(String.class)) {
        return true;
      }
    }

    return false;
  }

  private boolean isDateType(Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(clazz.equals(Date.class)) {
        return true;
      }
    }

    return false;
  }

  private boolean isEnum(Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      return clazz.isEnum();
    }

    return false;
  }

  private boolean isVoidType(Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(clazz.equals(Void.class)) {
        return true;
      }
    }

    return false;
  }

  private T unmarshalBasicType(ResponseBody responseBody, Type type) {
    //Object result = null;
    String str = null; //readerToString(reader);
    try {
      str = responseBody.string();
    } catch (IOException e) {
      Log.e(TAG, "Failed to parse body to string");
      return null;
    }
    Log.d(TAG, "unmarshalBasicType : " + str);
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(String.class.equals(clazz)) {
        return (T) str;
      } else if(Date.class.equals(clazz)) {
        return (T) Iso860DateConverter.fromString(str);
      } else if(Boolean.class.equals(clazz)) {
        return (T) Boolean.valueOf(str);
      } else if(isEnum(type)) {
        str = MagnetUtils.trimQuotes(str);
        for(Object o : clazz.getEnumConstants()) {
          Log.e(TAG, "enum value " + o.toString() +  " for type + " + type);
          if(str.equals(o.toString())) {
            return (T) o;
          }
        }
        Log.e(TAG, "Failed to unmarshal enum value " + str +  " for type + " + type);
        return null;
      }
    }

    try {
      return gson.fromJson(str, type);
    } catch (Exception e) {
      Log.e(TAG, "Failed to unmarshal type " + type + " due to \n" + e.getMessage());
    }

    return null;
  }

  private String marshalBasicType(Object object, Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(Date.class.equals(clazz)) {
        return Iso860DateConverter.toString((Date) object);
      }
    }
    return null != object ? object.toString() : null;
  }

  private String readerToString(Reader ir) {
    try {
      StringWriter stringWriter = new StringWriter();
      CharBuffer charBuffer = CharBuffer.allocate(1024);
      int length = 0;
      while ((length = ir.read(charBuffer)) != -1) {
        stringWriter.write(charBuffer.array(), 0, length);
      }

      String s = stringWriter.toString();
      return s;
    } catch (IOException e) {
      Log.e(TAG, "Failed to read InputStreamReader to string due to \n" + e.getMessage());
    }

    return null;
  }
}
