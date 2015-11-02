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
package retrofit;

import com.magnet.max.android.auth.AuthTokenProvider;
import com.magnet.max.android.connectivity.ConnectivityManager;
import com.magnet.max.android.rest.CacheOptions;
import com.magnet.max.android.rest.CallOptions;
import com.magnet.max.android.rest.ReliableCallOptions;
import com.magnet.max.android.rest.RequestManager;
import com.magnet.max.android.rest.qos.Condition;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Request;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class MagnetCall<T> {
  private static final String TAG = MagnetCall.class.getSimpleName();

  private final Call<T> call;
  private final AuthTokenProvider authTokenProvider;
  private final RequestManager requestManager;
  private final Executor callbackExecutor;

  private boolean executed;

  public MagnetCall(Call<T> call, AuthTokenProvider authTokenProvider, RequestManager requestManager, Executor callbackExecutor) {
    this.call = call;
    this.authTokenProvider = authTokenProvider;
    this.requestManager = requestManager;
    this.callbackExecutor = callbackExecutor;
  }

  public void executeInBackground() {
    executeInBackground(null);
  }

  public void executeInBackground(CacheOptions options) {
    execute(options, null);
  }

  public void executeEventually() {
    executeEventually(null);
  }

  public void executeEventually(ReliableCallOptions options) {
    execute(null, null != options ? options : ReliableCallOptions.DEFAULT);
  }

  private void execute(CacheOptions cacheOptions, ReliableCallOptions reliableOptions) {
    synchronized(this) {
      if(this.executed) {
        throw new IllegalStateException("Already executed");
      }

      this.executed = true;
    }

    CallOptions options = null;
    if(null != cacheOptions) {
      options = new CallOptions(cacheOptions);
    } else if(null != reliableOptions) {
      options = new CallOptions(reliableOptions);
    }

    OkHttpCall<T> okHttpCall = (OkHttpCall<T>) call;
    Callback<T> callback = extractAndRemoveCallback(okHttpCall);
    //Wrap callback in main thread executor
    Callback<T> callbackInMainThread = new ExecutorCallback<>(callbackExecutor, callback);

    Request request = okHttpCall.getRequest(null);

    if(null != reliableOptions) {
      Set<Condition> failedPrerequisites = reliableOptions.unsatisfiedConditions(true);
      if(!failedPrerequisites.isEmpty()) {
        StringBuilder sb = new StringBuilder("Prerequisite(s) not met : ");
        for(Condition p : failedPrerequisites) {
          sb.append(p.toString()).append(",");
        }
        sb.append(")");

        requestManager.saveReliableRequest(request, call, callbackInMainThread, reliableOptions, sb.toString());
        return;
      }
    }

    if(null != options) {
      requestManager.saveRequestOptions(request, options);
    }

    if(isCallReady(request, options)) {
      call.enqueue(callbackInMainThread);
    } else {
      requestManager.savePendingCall(call, callbackInMainThread, options);
    }
  }

  public void cancel() {
    call.cancel();
  }

  /**
   * Extract the callback in the last argurment and remove it from OkHttpCall
   * @param okHttpCall
   * @return
   */
  private Callback<T> extractAndRemoveCallback(OkHttpCall okHttpCall) {
    Object[] args = okHttpCall.getArgs();
    Callback<T> callback = null;
    if (null != args && args.length > 0 && args[args.length - 1] instanceof Callback) {
      callback = (Callback<T>) args[args.length - 1];
      Object[] actualArgs = new Object[args.length - 1];
      System.arraycopy(args, 0, actualArgs, 0, args.length - 1);
      okHttpCall.setArgs(actualArgs);
    }
    if (null == callback) {
      throw new IllegalArgumentException("Last argument should be Callback");
    }

    return callback;
  }

  private CacheControl getCacheControl(CacheOptions options) {
    if(null != options) {
      if (Boolean.TRUE == options.isAlwaysUseCacheIfOffline() || options.getMaxCacheAge() > 0) {
        CacheControl.Builder builder = new CacheControl.Builder();
        // Always return from cache if offline
        if (Boolean.TRUE == options.isAlwaysUseCacheIfOffline()) {
          if (ConnectivityManager.getInstance().getConnectivityStatus() == ConnectivityManager.TYPE_NOT_CONNECTED) {
            builder.onlyIfCached().maxAge(Integer.MAX_VALUE, TimeUnit.SECONDS).build();
          }
        }
        if (options.getMaxCacheAge() > 0) { // Return from cache if it's not expired
          builder.maxAge(options.getMaxCacheAge(), TimeUnit.SECONDS);
        }

        return builder.build();
      }
    } else {

    }

    return null;
  }

  private boolean isCallReady(Request request, CallOptions options) {
    return authTokenProvider.isAuthReady(request);
  }

  static final class ExecutorCallback<T> implements Callback<T> {
    private final Executor callbackExecutor;
    private final Callback<T> delegate;

    ExecutorCallback(Executor callbackExecutor, Callback<T> delegate) {
      this.callbackExecutor = callbackExecutor;
      this.delegate = delegate;
    }

    @Override public void onResponse(final Response<T> response) {
      callbackExecutor.execute(new Runnable() {
        @Override public void run() {
          delegate.onResponse(response);
        }
      });
    }

    @Override public void onFailure(final Throwable t) {
      callbackExecutor.execute(new Runnable() {
        @Override public void run() {
          delegate.onFailure(t);
        }
      });
    }
  }
}
