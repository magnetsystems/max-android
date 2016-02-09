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

import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.config.MaxAndroidConfig;
import com.magnet.max.android.config.MaxAndroidPropertiesConfig;
import com.magnet.max.android.rest.CacheOptions;
import com.magnet.max.android.tests.testsubjects.PrimitiveTypeService;
import com.magnet.max.android.tests.testsubjects.model.ModelWithAllTypes;
import com.magnet.max.android.tests.utils.MaxAndroidJsonConfig;
import com.magnet.max.android.tests.utils.MaxHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import retrofit.MagnetCall;
import retrofit.MagnetRestAdapter;

public class QoSTest extends ApplicationTestCase<TestApplication> {
  private static final String TAG = QoSTest.class.getSimpleName();

  private static MagnetRestAdapter magnetServiceHttpAdapter;
  private static PrimitiveTypeService myService;

  private TestApplication application;

  public QoSTest() {
    super(TestApplication.class);
  }

  protected void setUpApplication() throws Exception {
    super.setUp();
    createApplication();
    application = getApplication();

  }

  @Suppress // because of SQLiteReadOnlyDatabaseException attempt to write a readonly database
  @MediumTest
  public void testIntQueryParam() throws InterruptedException {
    final Integer param = 101;
    final Integer expectedResult = param;

    final Integer param2 = 102;
    final Integer expectedResult2 = param2;

    final CountDownLatch signal1 = new CountDownLatch(1);
    final CountDownLatch signal2 = new CountDownLatch(1);
    final CountDownLatch signal3 = new CountDownLatch(1);
    final CountDownLatch signal4 = new CountDownLatch(1);
    final CountDownLatch signal5 = new CountDownLatch(1);
    final CountDownLatch signal6 = new CountDownLatch(1);
    CacheOptions option60Age = new CacheOptions.Builder().maxCacheAge(10).build();
    CacheOptions option0Age = new CacheOptions.Builder().maxCacheAge(0).build();
    runQoSTest(new CallUnit(getIntQueryParamCall(param, expectedResult, signal1, false), option60Age, signal1),
               new CallUnit(getIntQueryParamCall(param, expectedResult, signal2, true), option60Age, signal2),
               new CallUnit(getIntQueryParamCall(param, expectedResult, signal3, false), null, signal3),   // null options
               new CallUnit(getIntQueryParamCall(param, expectedResult, signal4, false), option0Age, signal4), // 0 max age
               new CallUnit(getIntQueryParamCall(param, expectedResult, signal5, true), option60Age, signal5),
               new CallUnit(getIntQueryParamCall(param2, expectedResult2, signal6, false), option0Age, signal6) // different request
    );

    Thread.sleep(10 * 1000);

    //Cache should expire
    final CountDownLatch signal7 = new CountDownLatch(1);
    runQoSTest(
        new CallUnit(getIntQueryParamCall(param, expectedResult, signal7, false), option60Age,
            signal7));
  }

  @MediumTest
  public void testModelBodyParam() {
    final ModelWithAllTypes param = SharedData.model1;
    final ModelWithAllTypes expectedResult = param;

    final CountDownLatch signal1 = new CountDownLatch(1);
    final CountDownLatch signal2 = new CountDownLatch(1);
    CacheOptions option1 = new CacheOptions.Builder().maxCacheAge(60).build();
    runQoSTest(new CallUnit(getModelBodyParamCall(param, expectedResult, signal1, false), option1, signal1),
        new CallUnit(getModelBodyParamCall(param, expectedResult, signal2, true), option1, signal2));
  }

  private void runQoSTest(CallUnit... calls) {
    for(CallUnit callUnit : calls) {
      callUnit.getCall().executeInBackground(callUnit.getOptions());
      try {
        callUnit.getSignal().await(500, TimeUnit.SECONDS);
      } catch (Throwable e) {
        e.printStackTrace();
      }
      assertEquals(0, callUnit.getSignal().getCount());
    }
  }

  @MediumTest
  public void testReliableInMemory() throws InterruptedException {
    final Integer param = 101;
    final Integer expectedResult = param;
    final CountDownLatch signal1 = new CountDownLatch(1);
    MagnetCall call = getIntQueryParamCall(param, expectedResult, signal1, false);
    call.executeEventually();
    try {
      signal1.await(500, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Suppress
  @MediumTest
  public void testMock() {
    final Integer param = 101;
    final Integer expectedResult = param;
    final CountDownLatch signal1 = new CountDownLatch(1);
    MagnetCall call = getIntQueryParamCall(param, expectedResult, signal1, false);
    call.executeInBackground(new CacheOptions.Builder().useMock(true).build());
    try {
      signal1.await(500, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private PrimitiveTypeService getMyService() {
    if(null == myService) {
      try {
        setUpApplication();
      } catch (Exception e) {
        e.printStackTrace();
        fail(e.getMessage());
      }

      final MaxAndroidConfig config = new MaxAndroidJsonConfig(getContext(), R.raw.keys);
      MaxHelper.initMax(getContext(), new MaxAndroidConfig() {

        @Override public String getBaseUrl() {
          return config.getBaseUrl();
        }

        @Override public String getClientId() {
          return null;
        }

        @Override public String getClientSecret() {
          return null;
        }

        @Override public String getScope() {
          return null;
        }

      });
      //magnetServiceHttpAdapter = new MagnetRestAdapter.Builder().baseUrl("http://" + SERVER_PORT).build();
      myService = MaxCore.create(PrimitiveTypeService.class);
    }

    return myService;
  }

  //private static abstract class CallFactory<T> {
  //  protected final Object param;
  //  protected final T expectedResult;
  //  protected final CountDownLatch signal;
  //  protected final boolean shouldHitCache;
  //
  //  public CallFactory(Object param, T expectedResult, CountDownLatch signal,
  //      boolean shouldHitCache) {
  //    this.param = param;
  //    this.expectedResult = expectedResult;
  //    this.signal = signal;
  //    this.shouldHitCache = shouldHitCache;
  //  }
  //
  //  abstract public MagnetCall get();
  //}


  private MagnetCall getIntQueryParamCall(int param, final int expectedResult, final CountDownLatch signal, boolean shouldHitCache) {
   return getMyService().returnInt(param, new TestCallback<Integer>(expectedResult, signal, shouldHitCache));
  }

  private MagnetCall getModelBodyParamCall(ModelWithAllTypes param, final ModelWithAllTypes expectedResult, final CountDownLatch signal, boolean shouldHitCache) {
    return getMyService().returnModelWithAllTypes(param, new TestCallback<ModelWithAllTypes>(expectedResult, signal, shouldHitCache));
  }

  private static class CallUnit {
    private final MagnetCall call;
    private final CacheOptions options;
    private final CountDownLatch signal;

    public CallUnit(MagnetCall call, CacheOptions options, CountDownLatch signal) {
      this.call = call;
      this.options = options;
      this.signal = signal;
    }

    public MagnetCall getCall() {
      return call;
    }

    public CacheOptions getOptions() {
      return options;
    }

    public CountDownLatch getSignal() {
      return signal;
    }
  }

  private static class TestCallback<T> implements retrofit.Callback {
    private final CountDownLatch signal;
    private final boolean shouldHitCache;
    private final T expectedResult;

    public TestCallback(T expectedResult, CountDownLatch signal, boolean shouldHitCache) {
      this.expectedResult = expectedResult;
      this.signal = signal;
      this.shouldHitCache = shouldHitCache;
    }

    @Override public void onResponse(retrofit.Response response) {
      assertNotNull(response.body());
      Log.d(TAG, "------get from Cache : " + (response.raw().cacheResponse() != null));
      assertEquals(shouldHitCache, (response.raw().cacheResponse() != null));
      assertNotNull(response.body());
      assertEquals(expectedResult, response.body());
      signal.countDown();
    }

    @Override public void onFailure(Throwable throwable) {
      fail(throwable.getMessage());
      signal.countDown();
    }
  }
}
