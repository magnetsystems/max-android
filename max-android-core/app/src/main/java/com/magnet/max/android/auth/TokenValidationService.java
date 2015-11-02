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
 * File generated by Magnet Magnet Lang Tool on Jun 16, 2015 10:53:47 AM
 * @see {@link http://developer.magnet.com}
 */
package com.magnet.max.android.auth;

import com.magnet.max.android.auth.model.AuthToken;
import retrofit.http.*;

public interface TokenValidationService {

  /**
   * 
   * GET /api/tokens/token
   * @param callback asynchronous callback
   */
  @GET("/api/com.magnet.server/tokens/token") @FormUrlEncoded
  void validate(
     retrofit.Callback<AuthToken> callback
  );

}
