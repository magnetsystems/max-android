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
package com.magnet.max.android.rest.qos.internal;

import android.util.Log;
import com.activeandroid.query.Select;
import com.magnet.max.android.connectivity.ConnectivityManager;
import com.magnet.max.android.rest.CacheOptions;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.util.List;

public class CacheManager {
  private static final String TAG = CacheManager.class.getSimpleName();
  private static final int DEFAULT_CACHE_AGE = 7 * 24 * 3600 * 1000; //One week

  public Response getCachedResponse(Request request, CacheOptions options) {
    String requestHash = CacheUtils.getRequestHash(request);

    ResponseCacheEntity operation = findLatestCache(requestHash, request, options);
    if (null != operation && null != operation.response) {
      long currentTimestamp = System.currentTimeMillis();
      if(operation.getExpiredAt() >= currentTimestamp) {
        Log.d(TAG, "Cache hited and not expired for request " + request.urlString() + " with CallOptions " + options);
        return operation.response.toResponse(request);
      } else if(options.isAlwaysUseCacheIfOffline()) {
        boolean isOffline = ConnectivityManager.getInstance().getConnectivityStatus() == ConnectivityManager.TYPE_NOT_CONNECTED;
        Log.d(TAG, "Cache hited, expired but isAlwaysUseCacheIfOffline (offline = " + isOffline + ") for request " + request.urlString() + " with CallOptions " + options);
        if(isOffline) {
          return operation.response.toResponse(request);
        }
      } else {
        Log.d(TAG, "Cache hited but expired for request " + request.urlString() + " with CallOptions " + options);
      }
    }

    return null;
  }

  public Response cacheResponse(Request request, Response response, CacheOptions options) {
    String requestHash = CacheUtils.getRequestHash(request);
    ResponseCacheEntity operation = findLatestCache(requestHash, request, options);
    long currentTimestamp = System.currentTimeMillis();
    if(null == operation) {
      operation = new ResponseCacheEntity();
      operation.createdAt = currentTimestamp;
      operation.url = request.urlString();
      operation.httpMethod = request.method();
      operation.requestHash = requestHash;
      operation.response = new CachedResponse(response);
      operation.responseCode = response.code();
      operation.isOffilineCache = options.isAlwaysUseCacheIfOffline();

      Log.d(TAG, "Adding cache for request " + request);
    } else {
      //Update body
      operation.response = new CachedResponse(response);
      Log.d(TAG, "Updating cache for request " + request);
    }
    operation.updatedAt = currentTimestamp;
    long newExpiredTime = 0;
    if(options.getMaxCacheAge() > 0) {
      newExpiredTime = currentTimestamp + options.getMaxCacheAge() * 1000;
    }
    if(null == operation.getExpiredAt() || newExpiredTime > operation.getExpiredAt()) {
      operation.expiredAt = newExpiredTime;
    }
    operation.save();

    if(null != response.body()) {
      return response.newBuilder().body(
          ResponseBody.create(response.body().contentType(), operation.response.body)).build();
    } else {
      return response;
    }
  }

  private ResponseCacheEntity findLatestCache(String requestHash, Request request, CacheOptions options) {
    //Log.d(TAG, "request hash " + requestHash + " for request \n" + request);
    List<ResponseCacheEntity> operations = new Select()
        .from(ResponseCacheEntity.class)
        .where("requestHash = ?", requestHash)
        .orderBy("updatedAt DESC")
        .execute();
    if(null != operations && !operations.isEmpty()) {
      int returnIndex = 0;
      if(!options.isAlwaysUseCacheIfOffline()) {
        for (int i = 0; i < operations.size(); i++) {
          if(!operations.get(i).isOffilineCache) {
            returnIndex = i;
          }
        }
      }
      // Pick the first one and remove all others
      for (int i = 0; i < operations.size(); i++) {
        if(!operations.get(i).isOffilineCache && i != returnIndex) {
          operations.get(i).delete();
        }
      }

      return operations.get(returnIndex);
    }

    return null;
  }
}
