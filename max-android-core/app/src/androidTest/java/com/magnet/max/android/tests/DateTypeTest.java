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
import com.magnet.max.android.tests.testsubjects.DateType;
import com.magnet.max.android.tests.utils.MaxAndroidJsonConfig;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import retrofit.MagnetCall;
import retrofit.MagnetRestAdapter;


/**
* This is generated stub to test {@link DateType}
* <p>
* All test cases are suppressed by defaullt. To run the test, you need to fix all the FIXMEs first :
* <ul>
* <li>Set proper value for the parameters
* <li>Remove out the @Suppress annotation
* <li>(optional)Add more asserts for the result
* <ul><p>
*/

public class DateTypeTest extends AndroidTestCase {

  protected static MagnetRestAdapter magnetServiceAdapter;
  protected static DateType dateType;

  protected synchronized DateType getService() {
    if(null == dateType) {
      MaxAndroidConfig config = new MaxAndroidJsonConfig(getContext(), R.raw.keys);
      //magnetServiceAdapter = new MagnetRestAdapter.Builder().endpoint("ws://" + SERVER_HOST_AND_PORT + "/ws").build();
      magnetServiceAdapter = new MagnetRestAdapter.Builder().baseUrl(config.getBaseUrl()).build();

      dateType = magnetServiceAdapter.create(DateType.class);
    }

    return dateType;
  }

  /**
    * Generated unit test for {@link DateType#postDate} using HTTP
    */
  @MediumTest
  public void testPostDate() {
    final java.util.Date input = new Date();
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    MagnetCall<java.util.Date> call = getService().postDate(input, new retrofit.Callback<java.util.Date>() {
             @Override
             public void onResponse(retrofit.Response<Date> response) {
               assertNotNull(response.body());
               assertEquals(input, response.body());
               asyncCallBackSignal.countDown();
             }

             @Override
             public void onFailure(Throwable throwable) {
               fail(throwable.getMessage());
               asyncCallBackSignal.countDown();
             }
           });
    call.executeInBackground();

    try {
      asyncCallBackSignal.await(30, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    assertEquals(0, asyncCallBackSignal.getCount());
  }

  /**
    * Generated unit test for {@link DateType#putDate} using HTTP
    */
  @MediumTest
  public void testPutDate() {
    // FIXME : set proper value for the parameters
    final java.util.Date input = new Date();
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    MagnetCall<java.util.Date> call = getService().putDate(input, new retrofit.Callback<java.util.Date>() {
             @Override
             public void onResponse(retrofit.Response<Date> response) {
               assertNotNull(response.body());
               assertEquals(input, response.body());
               asyncCallBackSignal.countDown();
             }

             @Override
             public void onFailure(Throwable throwable) {
               fail(throwable.getMessage());
               asyncCallBackSignal.countDown();
             }
           });
    call.executeInBackground();

    try {
      asyncCallBackSignal.await(30, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    assertEquals(0, asyncCallBackSignal.getCount());
  }

  /**
    * Generated unit test for {@link DateType#getDate} using HTTP
    */
  //@Suppress //FIXME : set proper parameter value and un-suppress this test case
  @MediumTest
  public void testGetDate() {
    // FIXME : set proper value for the parameters
    final java.util.Date input = new Date();
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    MagnetCall<java.util.Date> call = getService().getDate(input, new retrofit.Callback<java.util.Date>() {
             @Override
             public void onResponse(retrofit.Response<Date> response) {
               assertNotNull(response.body());
               assertEquals(input, response.body());
               asyncCallBackSignal.countDown();
             }

             @Override
             public void onFailure(Throwable throwable) {
               fail(throwable.getMessage());
               asyncCallBackSignal.countDown();
             }
           });
    call.executeInBackground();

    try {
      asyncCallBackSignal.await(30, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    assertEquals(0, asyncCallBackSignal.getCount());
  }

  /**
    * Generated unit test for {@link DateType#deleteDate} using HTTP
    */
  //@Suppress //FIXME : set proper parameter value and un-suppress this test case
  @MediumTest
  public void testDeleteDate() {
    // FIXME : set proper value for the parameters
    final java.util.Date input = new Date();
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    MagnetCall<java.util.Date> call = getService().deleteDate(input, new retrofit.Callback<java.util.Date>() {
             @Override
             public void onResponse(retrofit.Response<Date> response) {
               assertNotNull(response.body());
               assertEquals(input, response.body());
               asyncCallBackSignal.countDown();
             }

             @Override
             public void onFailure(Throwable throwable) {
               fail(throwable.getMessage());
               asyncCallBackSignal.countDown();
             }
           });
    call.executeInBackground();

    try {
      asyncCallBackSignal.await(30, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    assertEquals(0, asyncCallBackSignal.getCount());
  }

  /**
    * Generated unit test for {@link DateType#postJsonDate} using HTTP
    */
  @MediumTest
  public void testPostJsonDate() {
    // FIXME : set proper value for the parameters
    final java.util.Date body = new Date();
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    MagnetCall<java.util.Date> call = getService().postJsonDate(body,
        new retrofit.Callback<java.util.Date>() {

          @Override public void onResponse(retrofit.Response<Date> response) {
            assertNotNull(response.body());
            assertEquals(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
    });
    call.executeInBackground();

    try {
      asyncCallBackSignal.await(30, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    assertEquals(0, asyncCallBackSignal.getCount());
  }

}
