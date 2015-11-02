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

import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;

import com.magnet.max.android.tests.testsubjects.PrimitiveTypeService;

import retrofit.MagnetRestAdapter;

@Suppress
public class PrimitiveTypeServiceWebsocketTest extends PrimitiveTypeServiceTest {

  private static PrimitiveTypeService primitiveTypeService;

  protected synchronized PrimitiveTypeService getPrimitiveTypeService() {
    if(null == primitiveTypeService) {
      Log.i("PrimitiveWS", "------------------creating primitiveTypeService");
      MagnetRestAdapter magnetServiceAdapter = new MagnetRestAdapter.Builder().baseUrl(
          "ws://" + SERVER_HOST_AND_PORT + "/ws").build();
      primitiveTypeService =  magnetServiceAdapter.create(PrimitiveTypeService.class);
    }
    assertNotNull(primitiveTypeService);
    return primitiveTypeService;
  }

}
