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

import java.util.Map;
import java.util.UUID;

/**
 * This class is used to register a new user
 */
public class UserRegistrationInfo {
  private String userIdentifier;
  private String password;
  private String email;
  private String[] roles;
  private UserStatus userStatus;
  private String userName;
  private UserRealm userRealm;
  private String firstName;
  private String lastName;
  private String[] tags;
  private java.util.Map<String, String> userAccountData;

  private UserRegistrationInfo() {

  }

  /**
   * The unique identifer for the user.
   */
  public String getUserIdentifier() {
    return userIdentifier;
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
   * The roles assigned to the user.
   */
  public String[] getRoles() {
    return roles;
  }

  /**
   * The status {@link UserStatus} for the user.
   */
  public UserStatus getUserStatus() {
    return userStatus;
  }

  /**
   * The username for the user.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * The realm {@link UserRealm} for the user.
   */
  public UserRealm getUserRealm() {
    return userRealm;
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
  public Map<String, String> getUserAccountData() {
    return userAccountData;
  }

  /**
   * Builder to build a ${link UserRegistrationInfo}
   **/
  public static class Builder {
    private UserRegistrationInfo toBuild = new UserRegistrationInfo();

    public Builder() {
    }

    /**
     * Build the UserRegistrationInfo object
     * @return
     */
    public UserRegistrationInfo build() {
      toBuild.userIdentifier = UUID.randomUUID().toString();
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
     * The roles assigned to the user.
     */
    public Builder roles(String[] value) {
      toBuild.roles = value;
      return this;
    }

    /**
     * The status {@link UserStatus} for the user.
     */
    public Builder userStatus(UserStatus value) {
      toBuild.userStatus = value;
      return this;
    }

    /**
     * The username for the user.
     */
    public Builder userName(String value) {
      toBuild.userName = value;
      return this;
    }

    /**
     * The realm {@link UserRealm} for the user.
     */
    public Builder userRealm(UserRealm value) {
      toBuild.userRealm = value;
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
    public Builder userAccountData(java.util.Map<String, String> value) {
      toBuild.userAccountData = value;
      return this;
    }
  }
}