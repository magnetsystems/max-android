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

import com.magnet.max.android.tests.testsubjects.model.EnumAttribute;
import com.magnet.max.android.tests.testsubjects.model.ModelWithAllTypes;
import com.magnet.max.android.tests.testsubjects.model.SubModel;
import java.util.Arrays;
import java.util.Date;

public class SharedData {
  // Test data
  public static ModelWithAllTypes
      model1 = new ModelWithAllTypes.ModelWithAllTypesBuilder().subModelAttribute(new SubModel.SubModelBuilder().intAttribute(11).stringAttribute("sub").build())
      .booleanAttribute(true)
      .intAttribute(11)
      .shortAttribute((short) (Short.MIN_VALUE + 1))
      .integerAttribute(12)
      .byteAttribute((byte) 0x11)
      .charAttribute('A')
      .enumAttribute(EnumAttribute.INPROGRESS)
      .doubleAttribute(3.3)
      .floatAttribute(4.4f)
      .listOfShortsAttribute(
          Arrays.asList((short) (Short.MIN_VALUE + 1), (short) 100, Short.MAX_VALUE))
      .createdAt(new Date())
      .build();
  public static ModelWithAllTypes model2 = new ModelWithAllTypes.ModelWithAllTypesBuilder()
      .listOfFloatsAttribute(Arrays.asList(1.0f, 3.0f, 5.0f))
      .listOfShortsAttribute(Arrays.asList(Short.MIN_VALUE, (short) 100, Short.MAX_VALUE))
      .listOfBooleansAttribute(Arrays.asList(Boolean.FALSE, Boolean.TRUE))
      .listOfStringsAttribute(Arrays.asList("Hello", "world"))
      .build();

}
