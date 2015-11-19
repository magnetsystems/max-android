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

package com.magnet.max.android.auth.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public abstract class BaseToken implements Parcelable {
  @SerializedName("expires_in")
  protected Long expiresIn;

  @SerializedName("access_token")
  protected String accessToken;

  @SerializedName("token_type")
  protected String tokenType;

  protected Long createdAt;

  protected Long expireAt;

  public BaseToken() {
  }

  public BaseToken(Long expiresIn, String accessToken, String tokenType, Long createdAt,
      Long expireAt) {
    this.expiresIn = expiresIn;
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.createdAt = createdAt;
    this.expireAt = expireAt;
  }

  public Long getExpiresIn() {
    return null != expiresIn ? expiresIn : 0;
  }

  public void setExpiresIn(Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public Long getCreatedAt() {
    if(null == createdAt) {
      createdAt = System.currentTimeMillis();
    }
    return createdAt;
  }

  public Long getExpireAt() {
    if(null == expireAt) {
      expireAt = getCreatedAt() + getExpiresIn() * 1000;
    }

    return expireAt;
  }

  public boolean isExpired() {
    return isAboutToExpireInMinutes(0);
  }

  public boolean isAboutToExpireInMinutes(int minutes) {
    return getExpireAt() < System.currentTimeMillis() + minutes * 60000;
  }

  @Override public int describeContents() {
    return 0;
  }

  protected void writeToParcelInternal(Parcel dest, int flags) {
    dest.writeValue(this.expiresIn);
    dest.writeString(this.accessToken);
    dest.writeString(this.tokenType);
    dest.writeValue(this.getCreatedAt());
    dest.writeValue(this.getExpireAt());
  }

  protected void parseFromParcel(Parcel in) {
    this.expiresIn = (Long) in.readValue(Long.class.getClassLoader());
    this.accessToken = in.readString();
    this.tokenType = in.readString();
    this.createdAt = (Long) in.readValue(Long.class.getClassLoader());
    this.expireAt = (Long) in.readValue(Long.class.getClassLoader());
  }
}
