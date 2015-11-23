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
package com.magnet.max.android.attachment;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.lang.Void;
import java.util.Map;
import retrofit.MagnetCall;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Streaming;

public interface AttachmentService {
  /**
   *
   * POST
   * @param file style:Query optional:false
   * @param callback asynchronous callback
   */
  @Multipart
  @POST("/api/com.magnet.server/file/save")
  MagnetCall<String> upload(@Part("file") com.squareup.okhttp.RequestBody file,
      retrofit.Callback<String> callback);

  ///**
  // *
  // * POST
  // * @param file style:Query optional:false
  // * @param callback asynchronous callback
  // */
  //@Multipart
  //@POST("/api/com.magnet.server/file/save")
  //MagnetCall<Map<String, String>> upload(@Part("file") com.squareup.okhttp.RequestBody file,
  //    retrofit.Callback<Map<String, String>> callback);

  @GET("/api/com.magnet.server/file/download/{fileId}")
  @Streaming
  MagnetCall<byte[]> download(@Path("fileId") String fileId, retrofit.Callback<byte[]> callback);
}
