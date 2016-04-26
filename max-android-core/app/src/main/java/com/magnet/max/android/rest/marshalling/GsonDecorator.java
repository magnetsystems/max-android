/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.max.android.rest.marshalling;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.magnet.max.android.util.MagnetUtils;
import com.magnet.max.android.util.TypeUtil;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Date;

public class GsonDecorator {
  private static final String TAG = "GsonDecorator";
  private static final TypeToken BYTE_ARRAY_TYPE_TOKEN = new TypeToken<byte[]>() {};

  private static GsonDecorator _instance = new GsonDecorator();
  private final Gson gson;

  private GsonDecorator() {
    gson = new GsonBuilder()
        .setDateFormat(Iso8601DateConverter.ISO8601DateFormat_WITH_MS)
        //.registerTypeAdapter(Date.class, new DateTypeAdapter())
        .create();
  }

  public static GsonDecorator getInstance() {
    return _instance;
  }

  public Object fromJson(String json, TypeToken typeToken) {
    if(null == json) {
      return null;
    }

    if(isVoidType(typeToken.getType())) {
      return null;
    }

    if(isByteArray(typeToken)) {
      return json.getBytes();
    }

    if(isSimpleType(typeToken)) {
      return unmarshalSimpleType(json, typeToken.getType());
    } else {
      return gson.fromJson(json, typeToken.getType());
    }
  }

  public Object fromJson(Reader reader, TypeToken typeToken) {
    try {
      return gson.fromJson(reader, typeToken.getType()); //typeAdapter.fromJson(in);
    } catch (Exception e) {
      Log.e(TAG, "Error in fromBody \n" + e.getMessage());
      throw e;
    }
  }

  public String toJson(Object value) {
    if(null == value) {
      return null;
    }

    TypeToken typeToken = TypeToken.get(value.getClass());
    if(isBasicType(typeToken.getType())) {
      return marshalSimpleType(value, typeToken.getType());
    } else if(isDateType(typeToken.getType())) {
      return Iso8601DateConverter.toString((Date) value);
    } else if(isEnum(typeToken.getType())) {
      return marshalSimpleType(value, typeToken.getType());
    } else {
      return gson.toJson(value);
    }
  }

  public boolean isSimpleType(TypeToken typeToken) {
    return isBasicType(typeToken.getType()) || isDateType(typeToken.getType()) || isEnum(typeToken.getType());
  }

  public Object unmarshalSimpleType(String str, Type type) {
    Log.d(TAG, "unmarshalSimpleType : " + str);
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(String.class.equals(clazz)) {
        return str;
      } else if(Date.class.equals(clazz)) {
        return Iso8601DateConverter.fromString(str);
      } else if(Boolean.class.equals(clazz)) {
        return Boolean.valueOf(str);
      } else if(isEnum(type)) {
        str = MagnetUtils.trimQuotes(str);
        for(Object o : clazz.getEnumConstants()) {
          Log.e(TAG, "enum value " + o.toString() +  " for type + " + type);
          if(str.equals(o.toString())) {
            return o;
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

  public String marshalSimpleType(Object object, Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(Date.class.equals(clazz)) {
        return Iso8601DateConverter.toString((Date) object);
      }
    }
    return null != object ? object.toString() : null;
  }

  public boolean isVoidType(Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(clazz.equals(Void.class)) {
        return true;
      }
    }

    return false;
  }

  public boolean isByteArray(TypeToken typeToken) {
    //if(typeToken.getRawType().isArray()) {
    //  //typeToken.getRawType().
    //  TypeToken tt = new TypeToken<byte[]>() {};
    //}
    //
    //return false;

    return BYTE_ARRAY_TYPE_TOKEN.equals(typeToken);
  }

  public boolean isEnum(Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      return clazz.isEnum();
    }

    return false;
  }

  public boolean isBasicType(Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(TypeUtil.isPrimitiveOrWrapper(clazz) ||
          clazz.equals(String.class)) {
        return true;
      }
    }

    return false;
  }

  public boolean isDateType(Type type) {
    if(type instanceof Class) {
      Class clazz = (Class) type;
      if(clazz.equals(Date.class)) {
        return true;
      }
    }

    return false;
  }
}
