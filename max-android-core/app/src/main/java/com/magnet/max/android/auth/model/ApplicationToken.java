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

/**
 * File generated by Magnet Magnet Lang Tool on Jun 16, 2015 10:53:46 AM
 * @see {@link http://developer.magnet.com}
 */

package com.magnet.max.android.auth.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class ApplicationToken extends BaseToken {

  private String scope;

  @SerializedName("mmx_app_id")
  private String mmxAppId;

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getMmxAppId() {
    return mmxAppId;
  }

  public void setMmxAppId(String mmxAppId) {
    this.mmxAppId = mmxAppId;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    writeToParcelInternal(dest, flags);
    dest.writeString(this.scope);
    dest.writeString(this.mmxAppId);
  }

  protected ApplicationToken(Parcel in) {
    parseFromParcel(in);
    this.scope = in.readString();
    this.mmxAppId = in.readString();
  }

  public static final Parcelable.Creator<ApplicationToken> CREATOR = new Parcelable.Creator<ApplicationToken>() {
    public ApplicationToken createFromParcel(Parcel source) {
      return new ApplicationToken(source);
    }

    public ApplicationToken[] newArray(int size) {
      return new ApplicationToken[size];
    }
  };
}
