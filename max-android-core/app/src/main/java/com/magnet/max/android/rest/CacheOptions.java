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
package com.magnet.max.android.rest;

public class CacheOptions {
  private static final String TAG = CacheOptions.class.getSimpleName();

  // Caching controll
  private int maxCacheAge;
  private boolean alwaysUseCacheIfOffline;

  // General
  private int responseTimeout;

  private boolean useMock;

  /**
   * private constructor, alway use builder
   */
  private CacheOptions() {
  }

  public int getMaxCacheAge() {
    return maxCacheAge;
  }

  public boolean isAlwaysUseCacheIfOffline() {
    return alwaysUseCacheIfOffline;
  }

  public int getResponseTimeout() {
    return responseTimeout;
  }

  public boolean useMock() {
    return useMock;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CacheOptions(");
    sb.append("maxCacheAge:").append(maxCacheAge).append(", ");
    sb.append("alwaysUseCacheIfOffline:").append(alwaysUseCacheIfOffline);
    sb.append(")");

    return sb.toString();
  }

  public static class Builder {
    private CacheOptions toBuild = new CacheOptions();

    public Builder maxCacheAge(int value) {
      toBuild.maxCacheAge = value;
      return this;
    }

    public Builder alwaysUseCacheIfOffline(Boolean value) {
      toBuild.alwaysUseCacheIfOffline = value;
      return this;
    }

    public Builder useMock(Boolean value) {
      toBuild.useMock = value;
      return this;
    }

    public Builder responseTimeout(int value) {
      toBuild.responseTimeout = value;
      return this;
    }

    public CacheOptions build() {
      return toBuild;
    }
  }
}
