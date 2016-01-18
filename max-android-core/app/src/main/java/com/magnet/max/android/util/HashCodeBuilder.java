/*
 *  Copyright (c) 2015 Magnet Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.magnet.max.android.util;

import java.lang.reflect.Array;

public class HashCodeBuilder {
  /**
   * An initial value for a <tt>hashCode</tt>, to which is added contributions
   * from fields. Using a non-zero value decreases collisons of <tt>hashCode</tt>
   * values.
   */
  public static final int SEED = 23;

  private static final int ODD_PRIME_NUMBER = 37;

  private int result;

  public HashCodeBuilder() {
    this(SEED);
  }

  public HashCodeBuilder(int seed) {
    result = seed * ODD_PRIME_NUMBER;
  }

  /** booleans.  */
  public HashCodeBuilder hash(boolean aBoolean) {
    result += aBoolean ? 1 : 0;
    return this;
  }

  public HashCodeBuilder hash(char aChar) {
    result +=  (int) aChar;
    return this;
  }

  /** ints.  */
  public HashCodeBuilder hash(int aInt) {
    result +=  aInt;
    return this;
  }

  /** longs.  */
  public HashCodeBuilder hash(long aLong) {
    result +=  (int)(aLong ^ (aLong >>> 32));
    return this;
  }

  /** floats.  */
  public HashCodeBuilder hash(int aSeed , float aFloat) {
    return hash(Float.floatToIntBits(aFloat));
  }

  /** doubles. */
  public HashCodeBuilder hash(double aDouble) {
    return hash(Double.doubleToLongBits(aDouble) );
  }

  /**
   * <tt>aObject</tt> is a possibly-null object field, and possibly an array.
   *
   * If <tt>aObject</tt> is an array, then each element may be a primitive
   * or a possibly-null object.
   */
  public HashCodeBuilder hash(Object aObject) {
    result += hashObject(aObject);
    return this;
  }

  @Override
  public int hashCode() {
    return result;
  }

  private int hashObject(Object aObject) {
    int objectHash = 0;
    if (aObject == null){
      objectHash = 0;
    } else if (!isArray(aObject)){
      objectHash = aObject.hashCode();
    } else {
      int length = Array.getLength(aObject);
      for (int idx = 0; idx < length; ++idx) {
        Object item = Array.get(aObject, idx);
        //if an item in the array references the array itself, prevent infinite looping
        if(! (item == aObject))
          //recursive call!
          objectHash += hashObject(item);
      }
    }
    return objectHash;
  }

  private static boolean isArray(Object aObject){
    return aObject.getClass().isArray();
  }
}
