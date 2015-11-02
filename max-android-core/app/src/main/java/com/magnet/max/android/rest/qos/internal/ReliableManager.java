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
import com.magnet.max.android.rest.qos.Condition;
import com.magnet.max.android.rest.qos.conditions.WifiCondition;
import com.magnet.max.android.rest.CallOptions;
import com.magnet.max.android.rest.ReliableCallOptions;
import com.magnet.max.android.util.StringUtil;
import com.squareup.okhttp.Request;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReliableManager {
  private static final String TAG = ReliableManager.class.getSimpleName();

  public void saveRequest(Request request, ReliableCallOptions options, String reason) {
    String requestHash = CacheUtils.getRequestHash(request);
    ReliableRequestEntity operation = null; //findRequest(requestHash, request, options);
    long currentTimestamp = System.currentTimeMillis();
    if(null == operation) {
      operation = new ReliableRequestEntity();
      operation.createdAt = currentTimestamp;
      operation.url = request.urlString();
      operation.httpMethod = request.method();
      operation.requestHash = requestHash;
      operation.request = new CachedRequest(request);
      operation.options = options;
      operation.wifiPreq = hasPrerequisite(options.getConditions(), WifiCondition.class);

      Log.d(TAG, "Saving reliable request " + request);
    } else {
      //Update body
      Log.d(TAG, "Updating reliable request " + request);
      operation.retries = operation.retries + 1;
    }
    operation.updatedAt = currentTimestamp;
    long newExpiredTime = currentTimestamp + options.getExpiresIn() * 1000;
    if(null == operation.getExpiredAt() || newExpiredTime > operation.getExpiredAt()) {
      operation.expiredAt = newExpiredTime;
    }
    if(StringUtil.isNotEmpty(reason)) {
      operation.lastFailureReason = reason;
    }
    operation.lastFailureTime = currentTimestamp;
    operation.save();
  }

  public void removeRequest(Request request) {
    String requestHash = CacheUtils.getRequestHash(request);
    ReliableRequestEntity operation = findRequest(requestHash, null, null);
    if(null != operation) {
      operation.delete();
    }
  }

  public List<ReliableRequestEntity> getAllCachedRequestEntities() {
    List<ReliableRequestEntity> operations = new Select()
        .from(ReliableRequestEntity.class)
        .orderBy("updatedAt DESC")
        .execute();

    List<Integer> expiredIndexes = new ArrayList<>();
    long currentTimestamp = System.currentTimeMillis();
    for(int i = 0; i < operations.size(); i++) {
      if(operations.get(i).expiredAt < currentTimestamp) {
        operations.get(i).delete();
        expiredIndexes.add(i);
      }
    }

    return operations;
  }

  public void clearPendingCalls() {
    List<ReliableRequestEntity> operations = new Select()
        .from(ReliableRequestEntity.class)
        .execute();
    for(int i = 0; i < operations.size(); i++) {
      operations.get(i).delete();
    }
  }

  private boolean hasPrerequisite(Set<Condition> prerequisites, Class<? extends Condition> prerequisite) {
    if(null != prerequisites && !prerequisites.isEmpty()) {
      for(Condition p : prerequisites) {
        if(p.getClass().equals(prerequisite)) {
          return true;
        }
      }
    }

    return false;
  }

  private ReliableRequestEntity findRequest(String requestHash, Request request, CallOptions options) {
    //Log.d(TAG, "request hash " + requestHash + " for request \n" + request);
    List<ReliableRequestEntity> operations = new Select()
        .from(ReliableRequestEntity.class)
        .where("requestHash = ?", requestHash)
        .orderBy("updatedAt DESC")
        .execute();
    if(null != operations && !operations.isEmpty()) {
      // Pick the first one and remove all others
      if (operations.size() > 1) {
        for (int i = 1; i < operations.size(); i++) {
          operations.get(i).delete();
        }
      }
      return operations.get(0);
    }

    return null;
  }

}
