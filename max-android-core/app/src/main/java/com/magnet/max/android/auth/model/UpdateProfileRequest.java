/**
 * Copyright (c) 2012-2015 Magnet Systems. All rights reserved.
 */
package com.magnet.max.android.auth.model;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to update profile of current user
 */
public class UpdateProfileRequest {
  private String password;
  private String email;
  private String firstName;
  private String lastName;
  private String[] tags;
  @SerializedName("userAccountData")
  private java.util.Map<String, String> extras;

  private UpdateProfileRequest() {
  }

  /**
   * The password for the user.
   */
  public String getPassword() {
    return password;
  }

  /**
   * The email for the user.
   */
  public String getEmail() {
    return email;
  }

  /**
   * The firstName for the user.
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * The lastName for the user.
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * The tags associated with the user.
   */
  public String[] getTags() {
    return tags;
  }

  /**
   * The additional key-value pairs associated with the user.
   */
  public Map<String, String> getExtras() {
    return extras;
  }

  /**
   * Builder to build a {@link UpdateProfileRequest}
   **/
  public static class Builder {
    private final UpdateProfileRequest toBuild = new UpdateProfileRequest();

    public Builder() {
    }

    /**
     * Build the UpdateProfileRequest object
     * @return
     */
    public UpdateProfileRequest build() {
      return toBuild;
    }

    /**
     * The firstName for the user.
     */
    public Builder firstName(String value) {
      toBuild.firstName = value;
      return this;
    }

    /**
     * The lastName for the user.
     */
    public Builder lastName(String value) {
      toBuild.lastName = value;
      return this;
    }

    /**
     * The email for the user.
     */
    public Builder email(String value) {
      toBuild.email = value;
      return this;
    }

    /**
     * The tags associated with the user.
     */
    public Builder tags(String[] value) {
      toBuild.tags = value;
      return this;
    }

    /**
     * The password for the user.
     */
    public Builder password(String value) {
      toBuild.password = value;
      return this;
    }

    /**
     * The additional key-value pairs associated with the user.
     */
    public Builder extras(java.util.Map<String, String> value) {
      toBuild.extras = value;
      return this;
    }

    /**
     * The additional key-value pairs associated with the user.
     */
    public Builder extra(String key, String value) {
      if(null == toBuild.extras) {
        toBuild.extras = new HashMap<>();
      }
      toBuild.extras.put(key, value);
      return this;
    }
  }
}