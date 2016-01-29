/**
 * Copyright (c) 2012-2015 Magnet Systems. All rights reserved.
 */
package com.magnet.max.android.rest;

public interface RestConstants {
  String APP_LOGIN_URL = "api/com.magnet.server/applications/session";
  String APP_LOGIN_WITH_DEVICE_URL = "api/com.magnet.server/applications/session-device";
  String USER_LOGIN_URL = "api/com.magnet.server/user/session";
  String USER_REGISTER_URL = "api/com.magnet.server/user/enrollment";
  String USER_LOGOUT_URL = "api/com.magnet.server/user/session";
  String USER_REFRESH_TOKEN_URL = "api/com.magnet.server/user/newtoken";

  String REST_BASE_PATH = "/api/";
  String REST_MOCK_BASE_PATH = "/mock/api/";
}
