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

import android.test.suitebuilder.annotation.SmallTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.magnet.max.android.rest.marshalling.MagnetGsonConverterFactory;
import com.magnet.max.android.util.TypeUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;
import okio.Buffer;
import retrofit.Converter;

public class MagnetGsonConverterTest extends TestCase {

  private MagnetGsonConverterFactory magnetGsonConverterFactory = MagnetGsonConverterFactory.create();

  @SmallTest
  public void testGson() {
    Gson gson = new Gson();
    String hello = "hello";
    String str = gson.fromJson(hello, String.class);
    assertEquals(hello, str);
  }

  @SmallTest
  public void testPrimitiveTypeCheck() {
    assertTrue(TypeUtil.isPrimitiveOrWrapper(Integer.class));
    assertTrue(TestEnumType.class.isEnum());
  }

  @SmallTest
  public void testPrimitiveTypeMarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(Integer.class);
    RequestBody requestBody = magnetGsonConverter.toBody(new Integer(1));
    assertResponseBody(requestBody, "1");
  }

  @SmallTest
  public void testDateTypeMarshalling() {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(0);
    cal.set(2015, 0, 1, 1, 1, 0);
    Date date = cal.getTime();
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(Date.class);
    RequestBody requestBody = magnetGsonConverter.toBody(date);
    assertResponseBody(requestBody, "2015-01-01T01:01:00.000Z");
  }

  @SmallTest
  public void testDateTypeUnmarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(Date.class);
    String str = "2015-01-01T01:01:00.000Z";
    try {
      Date date = (Date) magnetGsonConverter.fromBody(ResponseBody.create(MediaType.parse("application/json"), str));
      assertNotNull(date);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      assertEquals(2015, cal.get(Calendar.YEAR));
      assertEquals(0, cal.get(Calendar.MONTH));
      assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
      assertEquals(1, cal.get(Calendar.HOUR_OF_DAY));
      assertEquals(1, cal.get(Calendar.MINUTE));
      assertEquals(0, cal.get(Calendar.SECOND));
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @SmallTest
  public void testStringTypeMarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(String.class);

    String str = "hello world";
    RequestBody requestBody = magnetGsonConverter.toBody(str);
    assertResponseBody(requestBody, str);
  }

  @SmallTest
  public void testStringTypeUnmarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(String.class);
    String str = "hello world";
    try {
      String value = (String) magnetGsonConverter.fromBody(ResponseBody.create(MediaType.parse("application/json"), str));
      assertEquals(str, value);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  @SmallTest
  public void testEnumTypeMarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(TestEnumType.class);
    RequestBody requestBody = magnetGsonConverter.toBody(TestEnumType.ENUM1);
    assertResponseBody(requestBody, "ENUM1");
  }

  @SmallTest
  public void testEnumTypeUnmarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(TestEnumType.class);
    try {
      TestEnumType enumType = (TestEnumType) magnetGsonConverter.fromBody(ResponseBody.create(MediaType.parse("application/json"), "ENUM1"));
      assertEquals(TestEnumType.ENUM1, enumType);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @SmallTest
  public void testEnumTypeWithDoubleQuoteUnmarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(TestEnumType.class);
    try {
      TestEnumType enumType = (TestEnumType) magnetGsonConverter.fromBody(ResponseBody.create(MediaType.parse("application/json"), "\"ENUM1\""));
      assertEquals(TestEnumType.ENUM1, enumType);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

//  @SmallTest
//  public void testEnumArrayMarshalling() {
//    RequestBody requestBody = magnetGsonConverter.toBody(new TestEnumType[] {TestEnumType.ENUM1, TestEnumType.ENUM2}, TestEnumType.class);
//    assertResponseBody(requestBody, "[{\"value\":ENUM1},{\"value\":ENUM2}]");
//  }

  @SmallTest
  public void testEnumListUnmarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(new TypeToken<ArrayList<TestEnumType>>() {}.getType());
    try {
      List<TestEnumType> enums = (List<TestEnumType>) magnetGsonConverter.fromBody(ResponseBody.create(MediaType.parse("application/json"), "[\"ENUM1\",\"ENUM2\"]"));
      assertEquals(2, enums.size());
      assertEquals(TestEnumType.ENUM1, enums.get(0));
      assertEquals(TestEnumType.ENUM2, enums.get(1));
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

//  @SmallTest
//  public void testEnumListMarshalling() {
//    RequestBody requestBody = magnetGsonConverter.toBody(Arrays.asList(TestEnumType.ENUM1, TestEnumType.ENUM2), new TypeToken<ArrayList<TestEnumType>>() {}.getType());
//    assertResponseBody(requestBody, "[{\"value\":ENUM1},{\"value\":ENUM2}]");
//  }

  @SmallTest
  public void testPrimitiveTypeUnmarshalling() {
    Converter magnetGsonConverter = magnetGsonConverterFactory.get(Integer.class);
    try {
      Integer intValue = (Integer) magnetGsonConverter.fromBody(ResponseBody.create(MediaType.parse("application/json"), "1"));
      assertEquals(1, intValue.intValue());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private void assertResponseBody(RequestBody requestBody, String str) {
    try {
      Buffer buffer = new Buffer();
      requestBody.writeTo(buffer);
      assertEquals(str, buffer.readUtf8());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  private static enum TestEnumType {
    ENUM1,
    ENUM2
  }
}
