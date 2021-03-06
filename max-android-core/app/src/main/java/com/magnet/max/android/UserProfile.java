/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.max.android;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import com.magnet.max.android.util.EqualityUtil;
import com.magnet.max.android.util.HashCodeBuilder;
import com.magnet.max.android.util.StringUtil;

/**
 * Basic public profile of User
 */
public class UserProfile implements Parcelable {
  @SerializedName("userIdentifier") protected String mUserIdentifier;
  @SerializedName("firstName") protected String mFirstName;
  @SerializedName("lastName") protected String mLastName;
  protected String mDisplayName;
  protected Boolean mHasAvatar;

  /**
   * The unique identifer for the user.
   */
  public String getUserIdentifier() {
    return mUserIdentifier;
  }

  /**
   * The firstName for the user.
   */
  public String getFirstName() {
    return mFirstName;
  }

  /**
   * The lastName for the user.
   */
  public String getLastName() {
    return mLastName;
  }

  /**
   * The URL of the user avatar
   * @return
   */
  public String getAvatarUrl() {
    return (null == mHasAvatar || mHasAvatar) ? Attachment.createDownloadUrl(mUserIdentifier, mUserIdentifier) : null;
  }

  /**
   * The display name : first name + last name
   * @return
   */
  public String getDisplayName() {
    if(null == mDisplayName) {
      StringBuilder sb = new StringBuilder();
      if(StringUtil.isNotEmpty(mFirstName)) {
        sb.append(mFirstName);
      }
      if(StringUtil.isNotEmpty(mLastName)) {
        if(sb.length() > 0) {
          sb.append(" ");
        }
        sb.append(mLastName);
      }
      mDisplayName = sb.toString();
    }
    return mDisplayName;
  }

  /**
   * Compares this User object with the specified object and indicates if they
   * are equal. Following properties are compared :
   * <p><ul>
   * <li>userIdentifier
   * <li>firstName
   * <li>lastName
   * </ul><p>
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if(!EqualityUtil.quickCheck(this, obj)) {
      return false;
    }

    UserProfile theOther = (UserProfile) obj;

    return StringUtil.isStringValueEqual(mUserIdentifier, theOther.getUserIdentifier()) &&
        StringUtil.isStringValueEqual(mFirstName, theOther.getFirstName()) &&
        StringUtil.isStringValueEqual(mLastName, theOther.getLastName());
  }

  /**
   *  Returns an integer hash code for this object.
   *  @see #equals(Object) for the properties used for hash calculation.
   * @return
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().hash(mUserIdentifier)
        .hash(mFirstName).hash(mLastName).hashCode();
  }

  @Override public String toString() {
    return new StringBuilder().append("{")
        .append("userIdentifier = ").append(mUserIdentifier).append(", ")
        .append("firstName = ").append(mFirstName).append(", ")
        .append("lastName = ").append(mLastName).append(", ")
        .append("}")
        .toString();
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.mUserIdentifier);
    dest.writeString(this.mFirstName);
    dest.writeString(this.mLastName);
  }

  public UserProfile() {
  }

  protected UserProfile(Parcel in) {
    this.mUserIdentifier = in.readString();
    this.mFirstName = in.readString();
    this.mLastName = in.readString();
  }

  public static final Parcelable.Creator<UserProfile> CREATOR =
      new Parcelable.Creator<UserProfile>() {
        public UserProfile createFromParcel(Parcel source) {
          return new UserProfile(source);
        }

        public UserProfile[] newArray(int size) {
          return new UserProfile[size];
        }
      };

  /**
   * Builder for UserProfile
   */
  public static class Builder {
    private UserProfile toBuild;

    public Builder() {
      toBuild = new UserProfile();
    }

    public Builder identifier(String value) {
      toBuild.mUserIdentifier = value;
      return this;
    }

    public Builder firstName(String value) {
      toBuild.mFirstName = value;
      return this;
    }

    public Builder lastName(String value) {
      toBuild.mLastName = value;
      return this;
    }

    public Builder displayName(String value) {
      toBuild.mDisplayName = value;
      return this;
    }

    public Builder hasAvatar(Boolean value) {
      toBuild.mHasAvatar = value;
      return this;
    }

    public UserProfile build() {
      if(null == toBuild.mUserIdentifier) {
        throw new IllegalArgumentException("userIdentifier should not be null");
      }
      return toBuild;
    }
  }
}
