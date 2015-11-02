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

public class MMRole {

  
  
  private String roleDescription;

  
  private Boolean systemRequiredRole;

  
  private String roleName;

  
  private String identifier;

  public String getRoleDescription() {
    return roleDescription;
  }

  public Boolean getSystemRequiredRole() {
    return systemRequiredRole;
  }

  public String getRoleName() {
    return roleName;
  }

  public String getIdentifier() {
    return identifier;
  }


  /**
  * Builder for MMRole
  **/
  public static class MMRoleBuilder {
    private MMRole toBuild = new MMRole();

    public MMRoleBuilder() {
    }

    public MMRole build() {
      return toBuild;
    }

    public MMRoleBuilder roleDescription(String value) {
      toBuild.roleDescription = value;
      return this;
    }

    public MMRoleBuilder systemRequiredRole(Boolean value) {
      toBuild.systemRequiredRole = value;
      return this;
    }

    public MMRoleBuilder roleName(String value) {
      toBuild.roleName = value;
      return this;
    }

    public MMRoleBuilder identifier(String value) {
      toBuild.identifier = value;
      return this;
    }
  }
}
