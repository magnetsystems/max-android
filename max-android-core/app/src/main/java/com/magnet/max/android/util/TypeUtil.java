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
package com.magnet.max.android.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Code from org.apache.commons:commons-lang3:3.3.2
 */
public class TypeUtil {

  /**
   * Maps primitive {@code Class}es to their corresponding wrapper {@code Class}.
   */
  private static final Map<Class<?>, Class<?>>
      primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();
  static {
    primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
    primitiveWrapperMap.put(Byte.TYPE, Byte.class);
    primitiveWrapperMap.put(Character.TYPE, Character.class);
    primitiveWrapperMap.put(Short.TYPE, Short.class);
    primitiveWrapperMap.put(Integer.TYPE, Integer.class);
    primitiveWrapperMap.put(Long.TYPE, Long.class);
    primitiveWrapperMap.put(Double.TYPE, Double.class);
    primitiveWrapperMap.put(Float.TYPE, Float.class);
    primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
  }

  /**
   * Maps wrapper {@code Class}es to their corresponding primitive types.
   */
  private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<Class<?>, Class<?>>();
  static {
    for (final Class<?> primitiveClass : primitiveWrapperMap.keySet()) {
      final Class<?> wrapperClass = primitiveWrapperMap.get(primitiveClass);
      if (!primitiveClass.equals(wrapperClass)) {
        wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
      }
    }
  }

  /**
   * Returns whether the given {@code type} is a primitive or primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character},
   * {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
   *
   * @param type
   *            The class to query or null.
   * @return true if the given {@code type} is a primitive or primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character},
   *         {@link Short}, {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
   * @since 3.1
   */
  public static boolean isPrimitiveOrWrapper(final Class<?> type) {
    if (type == null) {
      return false;
    }
    return type.isPrimitive() || isPrimitiveWrapper(type);
  }

  /**
   * Returns whether the given {@code type} is a primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character}, {@link Short},
   * {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
   *
   * @param type
   *            The class to query or null.
   * @return true if the given {@code type} is a primitive wrapper ({@link Boolean}, {@link Byte}, {@link Character}, {@link Short},
   *         {@link Integer}, {@link Long}, {@link Double}, {@link Float}).
   * @since 3.1
   */
  public static boolean isPrimitiveWrapper(final Class<?> type) {
    return wrapperPrimitiveMap.containsKey(type);
  }
}
