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
import android.test.suitebuilder.annotation.Suppress;
import com.magnet.max.android.tests.testsubjects.PrimitiveTypeService;
import com.magnet.max.android.tests.testsubjects.model.EnumAttribute;
import com.magnet.max.android.tests.testsubjects.model.ModelWithAllTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
* This is generated stub to test {@link PrimitiveTypeService}
* <p>
* All test cases are suppressed by defaullt. To run the test, you need to fix all the FIXMEs first :
* <ul>
* <li>Set proper value for the parameters
* <li>Remove out the @Suppress annotation
* <li>(optional)Add more asserts for the response.body()
* <ul><p>
*/

public abstract class PrimitiveTypeServiceTest extends AndroidTestCase {

  public static final String SERVER_HOST_AND_PORT = "10.0.3.2:8443/api";

  protected abstract PrimitiveTypeService getPrimitiveTypeService();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @MediumTest
  public void testReturnListOfBooleans() {
    final java.util.List<Boolean> body = Arrays.asList(Boolean.TRUE, Boolean.FALSE);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfBooleans(body,
        new retrofit.Callback<java.util.List<Boolean>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<Boolean>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  @MediumTest
  public void testReturnShortWithFormParameters() {
    final Short input = 1;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnShortWithFormParameters(input, new retrofit.Callback<Short>() {
      @Override public void onResponse(retrofit.Response<Short> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnShortWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnShortWithBodyParameters() {
    final Short body = 2;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnShortWithBodyParameters(body, new retrofit.Callback<Short>() {
      @Override public void onResponse(retrofit.Response<Short> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(body);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnShort} using HTTP
    */
  @MediumTest
  public void testReturnShort() {
    final Short input = 3;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnShort(input, new retrofit.Callback<Short>() {
      @Override public void onResponse(retrofit.Response<Short> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfBytes} using HTTP
    */
  @MediumTest
  public void testReturnListOfBytes() {
    final java.util.List<Byte> body = Arrays.asList(new Byte((byte) 0xe0), new Byte((byte) 0xe1));
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfBytes(body,
        new retrofit.Callback<java.util.List<Byte>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<Byte>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());

            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfCharacters} using HTTP
    */
  @MediumTest
  public void testReturnListOfCharacters() {
    final java.util.List<Character> body = Arrays.asList('A', '£');
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfCharacters(body,
        new retrofit.Callback<java.util.List<Character>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<Character>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfLongs} using HTTP
    */
  @MediumTest
  public void testReturnListOfLongs() {
    final java.util.List<Long> body = Arrays.asList(Long.MIN_VALUE, 2l, 3l);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfLongs(body,
        new retrofit.Callback<java.util.List<Long>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<Long>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnDoubleWithFormParameters} using HTTP
    */
  @MediumTest
  public void testReturnDoubleWithFormParameters() {
    final Double input = 1.0;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnDoubleWithFormParameters(input,
        new retrofit.Callback<Double>() {
          @Override public void onResponse(retrofit.Response<Double> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(input);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnDoubleWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnDoubleWithBodyParameters() {
    final Double body = 2.0;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnDoubleWithBodyParameters(body, new retrofit.Callback<Double>() {
      @Override public void onResponse(retrofit.Response<Double> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(body);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnDouble} using HTTP
    */
  @MediumTest
  public void testReturnDouble() {
    final Double input = 3.0;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnDouble(input, new retrofit.Callback<Double>() {
      @Override public void onResponse(retrofit.Response<Double> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnIntWithFormParameters} using HTTP
    */
  @MediumTest
  public void testReturnIntWithFormParameters() {
    final Integer input = 1;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnIntWithFormParameters(input, new retrofit.Callback<Integer>() {
      @Override public void onResponse(retrofit.Response<Integer> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnIntWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnIntWithBodyParameters() {
    final Integer body = 2;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnIntWithBodyParameters(body, new retrofit.Callback<Integer>() {
      @Override public void onResponse(retrofit.Response<Integer> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(body);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnInt} using HTTP
    */
  @MediumTest
  public void testReturnInt() {
    final Integer input = 3;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnInt(input, new retrofit.Callback<Integer>() {
      @Override public void onResponse(retrofit.Response<Integer> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnStringWithFormParameters} using HTTP
    */
  @MediumTest
  public void testReturnStringWithFormParameters() {
    final String input = "test ReturnString With Form Parameters";
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnStringWithFormParameters(input,
        new retrofit.Callback<String>() {
          @Override public void onResponse(retrofit.Response<String> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(input);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnStringWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnStringWithBodyParameters() {
    final String body = "test Return String With Body Parameters";
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnStringWithBodyParameters(body, new retrofit.Callback<String>() {
      @Override public void onResponse(retrofit.Response<String> response) {
        System.out.println("-------response.body() : " + response.body());
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(body);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnString} using HTTP
    */
  @MediumTest
  public void testReturnString() {
    final String input = "test Return String";
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnString(input, new retrofit.Callback<String>() {
      @Override public void onResponse(retrofit.Response<String> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfIntegers} using HTTP
    */
  @MediumTest
  public void testReturnListOfIntegers() {
    final java.util.List<Integer> body = Arrays.asList(Integer.MIN_VALUE,2,3);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfIntegers(body,
        new retrofit.Callback<java.util.List<Integer>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<Integer>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfEnums} using HTTP
    */
  @MediumTest
  public void testReturnListOfEnums() {
    final java.util.List<EnumAttribute> body = new ArrayList<EnumAttribute>();
    body.add(EnumAttribute.STARTED);
    body.add(EnumAttribute.INPROGRESS);
    body.add(EnumAttribute.ENDED);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfEnums(body,
        new retrofit.Callback<java.util.List<EnumAttribute>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.List<EnumAttribute>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnByteWithFormParameters} using HTTP
    */
  @MediumTest
  public void testReturnByteWithFormParameters() {
    final Byte input = 1;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnByteWithFormParameters(input, new retrofit.Callback<Byte>() {
      @Override public void onResponse(retrofit.Response<Byte> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnByteWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnByteWithBodyParameters() {
    final Byte body = 2;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnByteWithBodyParameters(body, new retrofit.Callback<Byte>() {
      @Override public void onResponse(retrofit.Response<Byte> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(body);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnByte} using HTTP
    */
  @MediumTest
  public void testReturnByte() {
    final Byte input = 1;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnByte(input, new retrofit.Callback<Byte>() {
      @Override public void onResponse(retrofit.Response<Byte> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  @MediumTest
  public void testReturnEnumLowBoundWithFormParameters(final EnumAttribute input) {
    testReturnEnumWithFormParameters(EnumAttribute.STARTED);
  }

  @MediumTest
  public void testReturnEnumUpBoundWithFormParameters(final EnumAttribute input) {
    testReturnEnumWithFormParameters(EnumAttribute.ENDED);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnEnumWithFormParameters} using HTTP
    */
  private void testReturnEnumWithFormParameters(final EnumAttribute input) {
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnEnumWithFormParameters(input,
        new retrofit.Callback<EnumAttribute>() {
          @Override public void onResponse(retrofit.Response<EnumAttribute> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(input);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnEnumWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnEnumWithBodyParameters() {
    final EnumAttribute body = EnumAttribute.INPROGRESS;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnEnumWithBodyParameters(body,
        new retrofit.Callback<EnumAttribute>() {
          @Override public void onResponse(retrofit.Response<EnumAttribute> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(body);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnEnum} using HTTP
    */
  @MediumTest
  public void testReturnEnum() {
    final EnumAttribute input = EnumAttribute.ENDED;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnEnum(input, new retrofit.Callback<EnumAttribute>() {
      @Override public void onResponse(retrofit.Response<EnumAttribute> response) {
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfDoubles} using HTTP
    */
  @MediumTest
  public void testReturnListOfDoubles() {
    final java.util.List<Double> body = Arrays.asList(1.0, 2.0, Double.MAX_VALUE);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfDoubles(body,
        new retrofit.Callback<java.util.List<Double>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<Double>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnVoid} using HTTP
    */
  @MediumTest
  public void testReturnVoid() {
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnVoid(new retrofit.Callback<Void>() {
      @Override public void onResponse(retrofit.Response<Void> response) {
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfFloats} using HTTP
    */
  @MediumTest
  public void testReturnListOfFloats() {
    final java.util.List<Float> body = Arrays.asList(1.5f, 1.0f, 5f);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfFloats(body,
        new retrofit.Callback<java.util.List<Float>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<Float>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnBooleanWithFormParameters} using HTTP
    */
  @Suppress
  @MediumTest
  public void testReturnBooleanWithFormParameters() {
    final Boolean input = Boolean.TRUE;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnBooleanWithFormParameters(input,
        new retrofit.Callback<Boolean>() {
          @Override public void onResponse(retrofit.Response<Boolean> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(input);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnBooleanWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnBooleanWithBodyParameters() {
    final Boolean body = Boolean.TRUE;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnBooleanWithBodyParameters(body,
        new retrofit.Callback<Boolean>() {
          @Override public void onResponse(retrofit.Response<Boolean> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(body);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnBoolean} using HTTP
    */
  @MediumTest
  public void testReturnBoolean() {
    final Boolean input = Boolean.TRUE;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnBoolean(input, new retrofit.Callback<Boolean>() {
      @Override public void onResponse(retrofit.Response<Boolean> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfModels} using HTTP
    */
  @MediumTest
  public void testReturnListOfModels() {
    final java.util.List<ModelWithAllTypes> body = Arrays.asList(SharedData.model1, SharedData.model2);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfModels(body,
        new retrofit.Callback<java.util.List<ModelWithAllTypes>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.List<ModelWithAllTypes>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    try {
      asyncCallBackSignal.await(5, TimeUnit.MINUTES);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    assertEquals(0, asyncCallBackSignal.getCount());
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnMapOfStrings} using HTTP
    */
  @MediumTest
  public void testReturnMapOfStrings() {
    final java.util.Map<String, String> body = new HashMap<>();
    body.put("key1", "value1");
    body.put("key2", "value2");
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfStrings(body,
        new retrofit.Callback<java.util.Map<String, String>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, String>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnFloatWithFormParameters} using HTTP
    */
  @MediumTest
  public void testReturnFloatWithFormParameters() {
    final Float input = 6.0f;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnFloatWithFormParameters(input, new retrofit.Callback<Float>() {
      @Override public void onResponse(retrofit.Response<Float> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnFloatWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnFloatWithBodyParameters() {
    final Float body = 9.0f;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnFloatWithBodyParameters(body, new retrofit.Callback<Float>() {
      @Override public void onResponse(retrofit.Response<Float> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(body);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnFloat} using HTTP
    */
  @MediumTest
  public void testReturnFloat() {
    final Float input = 11.0f;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnFloat(input, new retrofit.Callback<Float>() {
      @Override public void onResponse(retrofit.Response<Float> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnModelWithAllTypes} using HTTP
    */
  @MediumTest
  public void testReturnModelWithAllTypes() {
    final ModelWithAllTypes body = SharedData.model1;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnModelWithAllTypes(body,
        new retrofit.Callback<ModelWithAllTypes>() {
          @Override public void onResponse(retrofit.Response<ModelWithAllTypes> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(body);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnCharWithFormParameters} using HTTP
   */
  @MediumTest
  public void testReturnCharAsciiWithFormParameters() {
    testReturnCharWithFormParameters('A');
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnCharWithFormParameters} using HTTP
   */
  @MediumTest
  public void testReturnCharNonAsciiWithFormParameters() {
    testReturnCharWithFormParameters('£');
  }

  public void testReturnCharWithFormParameters(final Character input) {
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnCharWithFormParameters(input,
        new retrofit.Callback<Character>() {
          @Override public void onResponse(retrofit.Response<Character> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(input);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnCharWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnCharWithBodyParameters() {
    final Character body = 'D';
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnCharWithBodyParameters(body,
        new retrofit.Callback<Character>() {
          @Override public void onResponse(retrofit.Response<Character> response) {
            assertNotNull(response.body());
            assertThat(response.body()).isEqualTo(body);
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnChar} using HTTP
    */
  @MediumTest
  public void testReturnChar() {
    final Character input = '*';
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnChar(input, new retrofit.Callback<Character>() {
      @Override public void onResponse(retrofit.Response<Character> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfStrings} using HTTP
    */
  @MediumTest
  public void testReturnListOfStrings() {
    final java.util.List<String> body = Arrays.asList("test", "Return", "ListOf", "Strings");
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfStrings(body,
        new retrofit.Callback<java.util.List<String>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<String>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnListOfShorts} using HTTP
    */
  @MediumTest
  public void testReturnListOfShorts() {
    final java.util.List<Short> body = Arrays.asList(Short.MIN_VALUE, (short) 110, (short) 120);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnListOfShorts(body,
        new retrofit.Callback<java.util.List<Short>>() {
          @Override public void onResponse(retrofit.Response<java.util.List<Short>> response) {
            assertNotNull(response.body());
            assertList(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnLongWithFormParameters} using HTTP
    */
  @MediumTest
  public void testReturnLongWithFormParameters() {
    final Long input = Long.MIN_VALUE;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnLongWithFormParameters(input, new retrofit.Callback<Long>() {
      @Override public void onResponse(retrofit.Response<Long> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnLongWithBodyParameters} using HTTP
    */
  @MediumTest
  public void testReturnLongWithBodyParameters() {
    final Long body = Long.MAX_VALUE;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnLongWithBodyParameters(body, new retrofit.Callback<Long>() {
      @Override public void onResponse(retrofit.Response<Long> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(body);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
    * Generated unit test for {@link PrimitiveTypeService#returnLong} using HTTP
    */
  @MediumTest
  public void testReturnLong() {
    final Long input = 111l;
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnLong(input, new retrofit.Callback<Long>() {
      @Override public void onResponse(retrofit.Response<Long> response) {
        assertNotNull(response.body());
        assertThat(response.body()).isEqualTo(input);
        asyncCallBackSignal.countDown();
      }

      @Override public void onFailure(Throwable throwable) {
        fail(throwable.getMessage());
        asyncCallBackSignal.countDown();
      }
    }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnMapOfIntegers} using Web socket
   */
  @MediumTest
  public void testReturnMapOfIntegers() {
    final java.util.Map<String, Integer> body = new HashMap<String, Integer>();
    body.put("one", 1);
    body.put("two", 2);
    body.put("three", 3);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfIntegers(body,
        new retrofit.Callback<java.util.Map<String, Integer>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, Integer>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnMapOfCharacters} using HTTP
   */
  @MediumTest
  public void testReturnMapOfCharacters() {
    final java.util.Map<String, Character> body = new HashMap<>();
    body.put("one", '1');
    body.put("two", '2');
    body.put("three", '3');
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfCharacters(body,
        new retrofit.Callback<java.util.Map<String, Character>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, Character>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnMapOfBytes} using Web socket
   */
  @MediumTest
  public void testReturnMapOfBytes() {
    final java.util.Map<String, Byte> body = new HashMap<>();
    body.put("one", new Byte((byte) 0xe1));
    body.put("two", new Byte((byte) 0xe2));
    body.put("three", new Byte((byte) 0xe3));
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfBytes(body,
        new retrofit.Callback<java.util.Map<String, Byte>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, Byte>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnMapOfLongs} using Web socket
   */
  @MediumTest
  public void testReturnMapOfLongs() {
    final java.util.Map<String, Long> body = new HashMap<>();
    body.put("one", 100l);
    body.put("two", 101l);
    body.put("three", 102l);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfLongs(body,
        new retrofit.Callback<java.util.Map<String, Long>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, Long>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnMapOfShorts} using Web socket
   */
  @MediumTest
  public void testReturnMapOfShorts() {
    final java.util.Map<String, Short> body = new HashMap<>();
    body.put("one", (short) 10);
    body.put("two", (short) 11);
    body.put("three", (short) 12);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfShorts(body,
        new retrofit.Callback<java.util.Map<String, Short>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, Short>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

        /**
         * Generated unit test for {@link PrimitiveTypeService#returnMapOfBooleans} using HTTP
         */ @MediumTest
  public void testReturnMapOfBooleans() {
    final java.util.Map<String, Boolean> body = new HashMap<>();
    body.put("one", Boolean.TRUE);
    body.put("two", Boolean.FALSE);
    body.put("three", Boolean.FALSE);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfBooleans(body,
        new retrofit.Callback<java.util.Map<String, Boolean>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, Boolean>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

          assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnMapOfDoubles} using Web socket
   */ @MediumTest
  public void testReturnMapOfDoubles() {
    final java.util.Map<String, Double> body = new HashMap<>();
    body.put("one", 10.0);
    body.put("two", 10.1);
    body.put("three", 10.2);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfDoubles(body,
        new retrofit.Callback<java.util.Map<String, Double>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, Double>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnMapOfFloats} using Web socket
   */ @MediumTest
  public void testReturnMapOfFloats() {
    final java.util.Map<String, Float> body = new HashMap<>();
    body.put("one", 20.0f);
    body.put("two", 20.1f);
    body.put("three", 20.2f);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfFloats(body,
        new retrofit.Callback<java.util.Map<String, Float>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, Float>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

        /**
         * Generated unit test for {@link PrimitiveTypeService#returnMapOfModels} using Web socket
         */ @MediumTest
  public void testReturnMapOfModels() {
    final java.util.Map<String, ModelWithAllTypes> body = new HashMap<>();
    body.put("one", SharedData.model1);
    body.put("Two", SharedData.model2);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfModels(body,
        new retrofit.Callback<java.util.Map<String, ModelWithAllTypes>>() {
          @Override public void onResponse(
              retrofit.Response<java.util.Map<String, ModelWithAllTypes>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

          assertCallback(asyncCallBackSignal);
  }

  /**
   * Generated unit test for {@link PrimitiveTypeService#returnMapOfEnums} using Web socket
   */
  @MediumTest
  public void testReturnMapOfEnums() {
    final Map<String, EnumAttribute> body = new HashMap<>();
    body.put("one", EnumAttribute.STARTED);
    body.put("two", EnumAttribute.ENDED);
    final CountDownLatch asyncCallBackSignal = new CountDownLatch(1);
    getPrimitiveTypeService().returnMapOfEnums(body,
        new retrofit.Callback<Map<String, EnumAttribute>>() {
          @Override
          public void onResponse(retrofit.Response<java.util.Map<String, EnumAttribute>> response) {
            assertNotNull(response.body());
            assertMap(body, response.body());
            asyncCallBackSignal.countDown();
          }

          @Override public void onFailure(Throwable throwable) {
            fail(throwable.getMessage());
            asyncCallBackSignal.countDown();
          }
        }).executeInBackground();

    assertCallback(asyncCallBackSignal);
  }

  private void assertList(List expected, List actual) {
    assertThat(expected.size()).isEqualTo(actual.size());
    for(int i = 0; i < expected.size(); i++) {
      assertThat(actual.get(i)).isEqualTo(expected.get(i));
    }
  }

  private void assertMap(Map<String, ? extends Object> expected, Map<String, ? extends Object> actual) {
    assertThat(actual.size()).isEqualTo(expected.size());
    for(String key1 : expected.keySet()) {
      assertNotNull(actual.containsKey(key1));
    }
  }

  private void assertCallback(CountDownLatch asyncCallBackSignal) {
    try {
      asyncCallBackSignal.await(5, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    assertEquals(0, asyncCallBackSignal.getCount());
  }

}
