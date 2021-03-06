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

public class CallOptions {
  private final CacheOptions cacheOptions;
  private final ReliableCallOptions reliableCallOptions;

  public CallOptions(CacheOptions cacheOptions) {
    this(cacheOptions, null);
  }

  public CallOptions(ReliableCallOptions reliableCallOptions) {
    this(null, reliableCallOptions);
  }

  private CallOptions(CacheOptions cacheOptions, ReliableCallOptions reliableCallOptions) {
    this.cacheOptions = cacheOptions;
    this.reliableCallOptions = reliableCallOptions;
  }

  public boolean isReliable() {
    return null != reliableCallOptions;
  }

  public CacheOptions getCacheOptions() {
    return cacheOptions;
  }

  public ReliableCallOptions getReliableCallOptions() {
    return reliableCallOptions;
  }

  @Override
  public String toString() {
    return new StringBuilder("CallOptions{").append("cacheOptions = ").append(cacheOptions).append(",")
        .append("reliableCallOptions = ").append(reliableCallOptions).append(" }").toString();
  }
}
