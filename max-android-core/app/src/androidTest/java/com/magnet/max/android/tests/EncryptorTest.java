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
import android.test.suitebuilder.annotation.Suppress;
import com.magnet.max.android.util.RSAEncryptor;

@Suppress
public class EncryptorTest extends AndroidTestCase {

  public void testEncryptor() {
    String alias = "test";
    RSAEncryptor encryptor = new RSAEncryptor(getContext(), alias);
    String plain = "211rgsFFAHL1zz6KU0XEC_rdNAjufVUV6aILvOkL97hCOpCsfDd8vG0imsZHVEU4nqyqkJhjF7NVSXjz98Kt-tKEAxuFt3guXF5qk-AN2RumNoGvxWPHb2SHejjyM1jjTztE8aO1PynjH28nuhrmL8kUskALFlwx-JK9rNG5LTixDmYx6rFA_wpeKpTgaEXciITXsUfkUEkcmtc3ib1r0A";
    assertEquals(plain, encryptor.decryptString(encryptor.encryptString(plain)));
  }
}
