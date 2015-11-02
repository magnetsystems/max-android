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

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "ResponseCacheEntity")
public class ResponseCacheEntity extends Model {

  @Column(name="requestHash", index = true)
  public String requestHash;

  @Column(name="method")
  public String httpMethod;

  @Column(name="url")
  public String url;

  @Column(name="isOffilineCache")
  public boolean isOffilineCache;

  /**
   * JSON
   */
  @Column(name="response")
  public CachedResponse response;

  @Column(name="responseCode")
  public int responseCode;

  @Column(name="createdAt")
  public Long createdAt;

  @Column(name="updatedAt")
  public Long updatedAt;

  @Column(name="expiredAt")
  public Long expiredAt;

  public String getRequestHash() {
    return requestHash;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getUrl() {
    return url;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public CachedResponse getResponse() {
    return response;
  }

  public boolean isOffilineCache() {
    return isOffilineCache;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public Long getUpdatedAt() {
    return updatedAt;
  }

  public Long getExpiredAt() {
    return expiredAt;
  }
}
