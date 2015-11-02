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

import android.os.Handler;
import android.os.Looper;
import com.magnet.max.android.auth.AuthTokenProvider;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import retrofit.Call;
import retrofit.CallAdapter;
import retrofit.MagnetCall;
import retrofit.Utils;

final public class MagnetCallAdapter {

  public static class Factory implements CallAdapter.Factory {

    private final AuthTokenProvider authTokenProvider;
    private final RequestManager requestManager;
    private final MainThreadExecutor callbackExecutor;

    public Factory(AuthTokenProvider authTokenProvider, RequestManager requestManager) {
      this.authTokenProvider = authTokenProvider;
      this.requestManager = requestManager;
      this.callbackExecutor = new MainThreadExecutor();
    }

    @Override public CallAdapter<?> get(Type returnType) {
      if (Utils.getRawType(returnType) != MagnetCall.class) {
        return null;
      }
      final Type responseType = Utils.getCallResponseType(returnType);

      return new CallAdapter<Object>() {

        @Override public Type responseType() {
          return responseType;
        }

        @Override public MagnetCall adapt(Call<Object> call) {
          return new MagnetCall(call, authTokenProvider, requestManager, callbackExecutor);
        }
      };
    }
  }

  static class MainThreadExecutor implements Executor {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void execute(Runnable r) {
      handler.post(r);
    }

    @Override public String toString() {
      return "MainThreadExecutor";
    }
  }
}
