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

package com.magnet.max.android.rest;

import android.util.Log;
import com.magnet.max.android.connectivity.ConnectivityListener;
import com.magnet.max.android.connectivity.ConnectivityManager;
import com.magnet.max.android.rest.qos.internal.CacheUtils;
import com.magnet.max.android.rest.qos.internal.ReliableManager;
import com.magnet.max.android.rest.qos.internal.ReliableRequestEntity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import retrofit.Response;

public class RequestManager implements ConnectivityListener {
  private static final String TAG = RequestManager.class.getSimpleName();
  private static final String COMMON_RETROFIT_CALLBACK_TAG = "CommonRetrofitCallback";
  private static final String COMMON_OKHTTP_CALLBACK_TAG = "CommonOkHttpCallback";

  private final ConcurrentHashMap<String, CallOptions> callOptionsMap = new ConcurrentHashMap<>();
  private final Deque<RequestInfo> pendingCallsWaitingForToken = new ArrayDeque();
  private final ConcurrentHashMap<String, RequestInfo> pendingCallsWaitingForPrerequisite = new ConcurrentHashMap<>();

  private final OkHttpClient client;
  private final ReliableManager reliableManager;

  private final retrofit.Callback commonRetrofitCallback;
  private final com.squareup.okhttp.Callback commonOkHttpCallback;

  //private final Context applicationContext;

  public RequestManager(OkHttpClient client) {
    Log.d(TAG, "RequestManager is created, pendingCallsWaitingForToken : " + System.identityHashCode(pendingCallsWaitingForToken));

    this.client = client;
    //this.applicationContext = applicationContext;

    this.reliableManager = new ReliableManager();

    //ConnectivityManager.getInstance().registerListener(this);

    commonRetrofitCallback = new retrofit.Callback() {
      @Override public void onResponse(Response response) {
        Log.d(COMMON_RETROFIT_CALLBACK_TAG, "------Reliable Call onResponse : \n" + response);
      }

      @Override public void onFailure(Throwable throwable) {
        Log.d(COMMON_RETROFIT_CALLBACK_TAG, "------Reliable Call onFailure : \n" + throwable);
      }
    };

    commonOkHttpCallback = new com.squareup.okhttp.Callback() {
      @Override public void onFailure(Request request, IOException e) {
        Log.d(COMMON_OKHTTP_CALLBACK_TAG, "------Reliable Call onFailure : " + e + " for request : \n" + request);
      }

      @Override public void onResponse(com.squareup.okhttp.Response response) throws IOException {
        Log.d(COMMON_OKHTTP_CALLBACK_TAG, "------Reliable Call onResponse : \n" + response);
      }
    };
  }

  public void saveRequestOptions(Request request, CallOptions options) {
    callOptionsMap.put(CacheUtils.getRequestHash(request), options);
  }

  public void saveReliableRequest(Request request, retrofit.Call call, retrofit.Callback callback,
      ReliableCallOptions reliableCallOptions, String reason) {
    pendingCallsWaitingForPrerequisite.put(CacheUtils.getRequestHash(request),
        new RequestInfo(request, call, callback, new CallOptions(reliableCallOptions)));

    //Persist it
    reliableManager.saveRequest(request, reliableCallOptions, reason);
  }

  public void removeReliableRequest(Request request) {
    pendingCallsWaitingForPrerequisite.remove(CacheUtils.getRequestHash(request));
    //Remove from DB
    reliableManager.removeRequest(request);
  }

  public CallOptions getRequestOptions(Request request) {
    return callOptionsMap.get(CacheUtils.getRequestHash(request));
  }

  public CallOptions popRequestOptions(Request request) {
    return callOptionsMap.remove(CacheUtils.getRequestHash(request));
  }

  public void savePendingCall(retrofit.Call call, retrofit.Callback callback,
      CallOptions options) {
    pendingCallsWaitingForToken.add(new RequestInfo(call, callback, options));
    logQueueSize();
  }

  public synchronized void resendPendingCallsForToken() {
    logQueueSize();
    while(pendingCallsWaitingForToken.size() > 0) {
      RequestInfo ri = pendingCallsWaitingForToken.pop();
      Log.d(TAG, "Sending pending request : " + ri);
      ri.getCall().enqueue(ri.getCallback());
    }
  }

  public synchronized void resendReliableCalls() {
    Set<Request> expiredRequests = new HashSet<>();
    for(ReliableRequestEntity e : reliableManager.getAllCachedRequestEntities()) {
      ReliableCallOptions options = e.getOptions();
      Request request = e.getRequest().toRequest();
      if(!options.isExpired()) {
        if(options.evaluateConditions(true)) {
          Log.d(TAG, "-----Resending reliable call " + request);
          client.newCall(request).enqueue(commonOkHttpCallback);
        }
      } else {
        Log.d(TAG, "-----Reliable request " + request + " expired");
        expiredRequests.add(request);
      }
    }

    //Clean up expired requests
    for(Request r : expiredRequests) {
      removeReliableRequest(r);
    }
  }

  public void clearPendingCalls() {
    reliableManager.clearPendingCalls();
  }

  @Override public synchronized void onConnectivityStatusChanged(int lastKnowStatus, int newStatus) {
    if(ConnectivityManager.TYPE_WIFI == newStatus) {
      if(pendingCallsWaitingForPrerequisite.size() > 0) {
        Log.d(TAG, "-------sending reliable call when WIFI is back ");
        Iterator<RequestInfo> it = pendingCallsWaitingForPrerequisite.values().iterator();
        while (it.hasNext()) {
          RequestInfo ri = it.next();
          if (ri.getOptions().getReliableCallOptions().evaluateConditions(true)) {
            if (null != ri.getCall()) {
              ri.getCall()
                  .enqueue(null != ri.getCallback() ? ri.getCallback() : commonRetrofitCallback);
            } else {
              client.newCall(ri.getRequest()).enqueue(commonOkHttpCallback);
            }

            //pendingCallsWaitingForPrerequisite.pop();
          }
        }
      }
    }
  }

  private void logQueueSize() {
    Log.d(TAG, "There is " + pendingCallsWaitingForToken.size() + " pending requests in the queue "
        +  System.identityHashCode(pendingCallsWaitingForToken) + " : "
        + pendingCallsWaitingForToken);
  }

  private static class RequestInfo {
    private final Request request;
    private final retrofit.Call call;
    private final CallOptions options;
    private final retrofit.Callback callback;

    public RequestInfo(Request request, retrofit.Call call, retrofit.Callback callback, CallOptions options) {
      this.request = request;
      this.call = call;
      this.callback = callback;
      this.options = options;
    }

    public RequestInfo(retrofit.Call call, retrofit.Callback callback, CallOptions options) {
      this(null, call, callback, options);
    }

    public Request getRequest() {
      return request;
    }

    public retrofit.Call getCall() {
      return call;
    }

    public retrofit.Callback getCallback() {
      return callback;
    }

    public CallOptions getOptions() {
      return options;
    }

    @Override
    public String toString() {
      return new StringBuilder("RequestInfo {")
          .append("request = ").append(request).append(",")
          .append("call = ").append(call).append(",")
          .append("callOptions = ").append(options).append(",")
          .append("callback = ").append(callback).append(",").toString();
    }
  }
}
