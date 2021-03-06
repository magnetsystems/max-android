/*
 *  Copyright (c) 2015 Magnet Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.magnet.max.android;

import com.magnet.max.android.rest.annotation.Timeout;
import com.squareup.okhttp.ResponseBody;
import java.util.Map;
import retrofit.MagnetCall;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.HeaderMap;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Streaming;

/**
 * The service to save/download large content to server
 */
public interface AttachmentService {

  /**
   * Upload
   * POST
   * @param body style:Query optional:false
   * @param callback asynchronous callback
   */
  @Timeout(write = 5 * 60)
  @Multipart
  @POST("/api/com.magnet.server/file/save")
  MagnetCall<String> upload(@HeaderMap Map<String, String> metaData,
      @Part("file") com.squareup.okhttp.RequestBody body,
      retrofit.Callback<String> callback);

  /**
   *
   * POST
   * @param file style:Query optional:false
   * @param callback asynchronous callback
   */
  @Timeout(write = 5 * 60)
  @POST("/api/com.magnet.server/file/save/multiple")
  MagnetCall<Map<String, String>> uploadMultiple(@HeaderMap Map<String, String> metaData,
      @Body com.squareup.okhttp.RequestBody file,
      retrofit.Callback<Map<String, String>> callback);

  @Timeout(read = 5 * 60)
  @GET("/api/com.magnet.server/file/download/{fileId}")
  MagnetCall<byte[]> downloadAsBytes(@Path("fileId") String fileId,
      @Query("user_id") String userId,
      retrofit.Callback<byte[]> callback);

  @Timeout(read = 5 * 60)
  @GET("/api/com.magnet.server/file/download/{fileId}")
  @Streaming
  MagnetCall<ResponseBody> downloadAsStream(@Path("fileId") String fileId,
    @Query("user_id") String userId,
    retrofit.Callback<ResponseBody> callback);
}
