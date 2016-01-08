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
package com.magnet.max.android.tests;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;
import com.google.gson.Gson;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.config.MaxAndroidConfig;
import com.magnet.max.android.logging.EventLog;
import com.magnet.max.android.logging.remote.LogEventsCollectionService;
import com.magnet.max.android.tests.utils.MaxAndroidJsonConfig;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EventsCollectionTestLog extends AndroidTestCase {
  private static final String TAG = EventsCollectionTestLog.class.getSimpleName();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //MagnetAndroidConfig config = new MagnetAndroidConfig() {
    //  @Override public String getBaseUrl() {
    //    return "http://10.0.3.2:8443";
    //  }
    //
    //  @Override public String getClientId() {
    //    return "73728e03-c3ee-457a-a06a-4e59c765eedd";
    //  }
    //
    //  @Override public String getClientSecret() {
    //    return "Wu0vLi0YzkurLj8mVng7S2f8kJXSaC3Z5En8J6M3kM8";
    //  }
    //
    //  @Override public String getScope() {
    //    return null;
    //  }
    //
    //  @Override public Map<String, String> getAllConfigs() {
    //    return null;
    //  }
    //};

    MaxAndroidConfig config = new MaxAndroidJsonConfig(getContext(), R.raw.keys);
    MaxCore.init(getContext(), config);
  }

  //@MediumTest
  ////@Suppress
  //public void testUserAPis() {
  //  final CountDownLatch signal = new CountDownLatch(3);
  //
  //  final String userName = "jim" + System.currentTimeMillis();
  //  UserRegistrationInfo newUser = new UserRegistrationInfo.Builder().userName(userName).password("test").email(
  //      userName + "@magnet.com")
  //      .firstName("JimTest").lastName("Liu").userRealm(UserRealm.DB).build();
  //  User.register(newUser, new ApiCallback<User>() {
  //    @Override public void success(User user) {
  //      signal.countDown();
  //      User.login(userName, "test", false, new ApiCallback<Boolean>() {
  //        @Override public void success(Boolean aBoolean) {
  //          Log.i(TAG, "------------user login " + userName);
  //          signal.countDown();
  //          User.getCurrentUser();
  //        }
  //
  //        @Override public void failure(ApiError apiError) {
  //          signal.countDown();
  //          fail(apiError.getMessage());
  //        }
  //      });
  //    }
  //
  //    @Override public void failure(ApiError apiError) {
  //      signal.countDown();
  //      fail(apiError.getMessage());
  //    }
  //  });
  //
  //  try {
  //    signal.await(5, TimeUnit.SECONDS);
  //  } catch (Throwable e) {
  //    e.printStackTrace();
  //  }
  //}

  @MediumTest
  //@Suppress
  public void testUploadLogFileControllerHttp() {
    testUploadLogFile(MaxCore.create(LogEventsCollectionService.class));
  }

  private void testUploadLogFile(LogEventsCollectionService myService) {
    EventLog log = new EventLog.LogBuilder().name("logtest").category("test").message("test").build();
    Gson gson = new Gson();
    final RequestBody fileBody = RequestBody.create(MediaType.parse("text/plain"), gson.toJson(log));
    final CountDownLatch signal = new CountDownLatch(1);
    myService.addEventsFromFile(fileBody, new retrofit.Callback<Void>() {

      @Override public void onResponse(retrofit.Response<Void> response) {
        signal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        signal.countDown();
      }
    }).executeInBackground();

    try {
      signal.await(10, TimeUnit.SECONDS);
    } catch (Throwable e) {
      Log.e(TAG, "addEventsFromFile timeout");
      fail("addEventsFromFile timeout");
    }

    assertEquals(0, signal.getCount());
  }
}
