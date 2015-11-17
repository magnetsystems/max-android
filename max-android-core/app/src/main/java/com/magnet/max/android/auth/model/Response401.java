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

import com.magnet.max.android.util.MagnetUtils;
import com.magnet.max.android.util.StringUtil;

/**
 * see <a href="http://self-issued.info/docs/draft-ietf-oauth-v2-bearer.html">The OAuth 2.0 Authorization Framework: Bearer Token Usage</a>
 */
public class Response401 {
  public static final String ERROR_HEADER = "WWW-Authenticate";

  public enum AuthErrorType {
    REFRESH_TOKEN("refresh_token"),
    GRANT_TYPE("grant_type"),
    USER_CREDENTIALS("user_credentials"),
    CLIENT_CREDENTIALS("client_credentials"),
    USER_ACCESS_TOKEN("user_access_token"),
    CLIENT_ACCESS_TOEKN("client_access_token");

    private final String name;

    private static final String ERROR_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String ERROR_TYPE_CLIENT_CREDENTIAL = "client_credentials";
    private static final String ERROR_TYPE_USER_CREDENTIAL = "user_credentials";
    private static final String ERROR_TYPE_GRANT_TYPE = "grant_type";
    private static final String ERROR_TYPE_CLIENT_ACCESS_TOKEN = "client_access_token";
    private static final String ERROR_TYPE_USER_ACCESS_TOKEN = "user_access_token";

    private AuthErrorType(String name) {
      this.name = name;
    }

    public boolean equals(String otherName) {
      return (otherName == null) ? false : name.equals(otherName);
    }

    public static AuthErrorType fromString(String s) {
      if(ERROR_TYPE_CLIENT_ACCESS_TOKEN.equals(s)) {
        return CLIENT_ACCESS_TOEKN;
      } else if(ERROR_TYPE_CLIENT_CREDENTIAL.equals(s)) {
        return CLIENT_CREDENTIALS;
      } else if(ERROR_TYPE_GRANT_TYPE.equals(s)) {
        return GRANT_TYPE;
      } else if(ERROR_TYPE_REFRESH_TOKEN.equals(s)) {
        return REFRESH_TOKEN;
      } else if(ERROR_TYPE_USER_ACCESS_TOKEN.equals(s)) {
        return USER_ACCESS_TOKEN;
      } else if(ERROR_TYPE_USER_CREDENTIAL.equals(s)) {
        return USER_CREDENTIALS;
      }

      return null;
    }

    public String toString() {
      return this.name;
    }
  }

  private static final String KEY_REALM = "Bearer realm";
  private static final String KEY_ERROR = "error";
  private static final String KEY_DESCRIPTION= "error_description";

  private String realm;
  private String error;
  private AuthErrorType errorType;
  private String description;

  public Response401(String keyValuePairs) {
    if(StringUtil.isNotEmpty(keyValuePairs)) {
      String[] pairs = keyValuePairs.split(",");
      for(String pair : pairs) {
        String[] kv = pair.split("=");
        if(kv.length == 2) {
          String key = kv[0].trim();
          String value = MagnetUtils.trimQuotes(kv[1].trim());
          if(KEY_ERROR.equals(key)) {
            this.error = value;
            errorType = AuthErrorType.fromString(error);
          } else if(KEY_REALM.equals(key)) {
            this.realm = value;
          } else if(KEY_DESCRIPTION.equals(key)) {
            this.description = value;
          }
        }
      }
    }
  }

  public String getRealm() {
    return realm;
  }

  public String getError() {
    return error;
  }

  public AuthErrorType getErrorType() {
    return errorType;
  }

  public String getDescription() {
    return description;
  }
}
