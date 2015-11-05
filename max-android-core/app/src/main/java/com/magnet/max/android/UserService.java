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
package com.magnet.max.android;

import com.magnet.max.android.auth.model.UpdateProfileRequest;
import com.magnet.max.android.auth.model.UserLoginResponse;
import com.magnet.max.android.auth.model.UserRegistrationInfo;
import retrofit.MagnetCall;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;

/**public**/ interface UserService {

  /**
   *
   * POST /api/user/session
   * @param grant_type style:Field optional:false
   * @param username style:Field optional:false
   * @param password style:Field optional:false
   * @param client_id style:Field optional:false
   * @param scope style:Field optional:false
   * @param remember_me style:Field optional:false
   * @param mMSDEVICEID(original name : MMS-DEVICE-ID) style:Header optional:false
   * @param authorization(original name : Authorization) style:Header optional:false
   * @param callback asynchronous callback
   */
  @POST("/api/com.magnet.server/user/session") @FormUrlEncoded
  MagnetCall<UserLoginResponse> userLogin (
      @Field("grant_type") String grant_type,
      @Field("username") String username,
      @Field("password") String password,
      @Field("client_id") String client_id,
      @Field("scope") String scope,
      @Field("remember_me") Boolean remember_me,
      @Header("MMS-DEVICE-ID") String mMSDEVICEID,
      @Header("Authorization") String authorization,
      retrofit.Callback<UserLoginResponse> callback
  );

  /**
   *
   * DELETE /api/user/session
   * @param callback asynchronous callback
   */
  @DELETE("/api/com.magnet.server/user/session")
  MagnetCall<Boolean> userLogout(
      retrofit.Callback<Boolean> callback
  );

  /**
   *
   * DELETE /api/user/{userId}
   * @param body style:Body optional:false
   * @param callback asynchronous callback
   */
  //@DELETE("/api/com.magnet.server/user/{userId}") MagnetCall<Boolean> deleteUser(
  //   @Body String body,
  //   retrofit.Callback<Boolean> callback
  //);


  /**
   *
   * POST /api/user/enrollment
   * @param body style:Body optional:false
   * @param callback asynchronous callback
   */
  @POST("/api/com.magnet.server/user/enrollment")
  MagnetCall<User> register(
     @Body UserRegistrationInfo body,
     retrofit.Callback<User> callback
  );

  /**
   *
   * GET /com.magnet.server/user/query
   * @param q style:Query optional:false
   * @param take style:Query optional:false
   * @param skip style:Query optional:false
   * @param sort style:Query optional:false
   * @param callback asynchronous callback
   */
  @GET("com.magnet.server/user/query")
  MagnetCall<java.util.List<User>> searchUsers(
      @Query("q") String q,
      @Query("take") Integer take,
      @Query("skip") Integer skip,
      @Query("sort") String sort,
      retrofit.Callback<java.util.List<User>> callback
  );

  /**
   *
   * GET /com.magnet.server/user/users
   * @param userNames style:Query optional:false
   * @param callback asynchronous callback
   */
  @GET("com.magnet.server/user/users")
  MagnetCall<java.util.List<User>> getUsersByUserNames(
      @Query("userNames") java.util.List<String> userNames,
      retrofit.Callback<java.util.List<User>> callback
  );

  /**
   *
   * GET /com.magnet.server/user/users/ids
   * @param userIds style:Query optional:false
   * @param callback asynchronous callback
   */
  @GET("com.magnet.server/user/users/ids")
  MagnetCall<java.util.List<User>> getUsersByUserIds(
      @Query("userIds") java.util.List<String> userIds,
      retrofit.Callback<java.util.List<User>> callback
  );

  /**
   *
   * @param updateProfileRequest
   * @param callback
   * @return
   */
  @PUT("com.magnet.server/user/profile")
  MagnetCall<User> updateProfile(UpdateProfileRequest updateProfileRequest,
      retrofit.Callback<User> callback);
}
