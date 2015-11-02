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
import com.magnet.max.android.config.MaxAndroidConfig;
import com.magnet.max.android.tests.testsubjects.MyService;
import com.magnet.max.android.tests.utils.MaxAndroidJsonConfig;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import retrofit.MagnetCall;
import retrofit.MagnetRestAdapter;

public class MagnetServiceAdapterTest extends AndroidTestCase {
  //private static final String SERVER_PORT = "192.168.101.135:8443";

  //GenyMotion
  private static final String SERVER_PORT = "10.0.3.2:8443/api";

  //private MagnetRestAdapter magnetServiceWebsocketAdapter;
  private MagnetRestAdapter magnetServiceHttpAdapter;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //magnetServiceWebsocketAdapter = new MagnetRestAdapter.Builder().baseUrl("ws://" + SERVER_PORT + "/ws").build();
    MaxAndroidConfig config = new MaxAndroidJsonConfig(getContext(), R.raw.keys);
    magnetServiceHttpAdapter = new MagnetRestAdapter.Builder().baseUrl(config.getBaseUrl()).build();
  }

  //@Suppress
  //@MediumTest
  //public void testMyControllerWebsocket() {
  //  testSayHello(magnetServiceWebsocketAdapter.create(MyService.class));
  //}

  @MediumTest
  public void testMyControllerHttp() {
    testSayHello(magnetServiceHttpAdapter.create(MyService.class));
  }

  private void testSayHello(MyService myService) {
    final String param = "iOS";
    final String expectedResult = "Hello iOS";
    final CountDownLatch signal = new CountDownLatch(1);
    MagnetCall<String> call = myService.sayHello(param, new retrofit.Callback<String>() {

      @Override public void onResponse(retrofit.Response<String> response) {
        assertEquals(expectedResult, response.body());
        signal.countDown();
      }

      @Override
      public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        signal.countDown();
      }
    });
    call.executeInBackground();

    try {
      signal.await(5, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    assertEquals(0, signal.getCount());
  }
}
