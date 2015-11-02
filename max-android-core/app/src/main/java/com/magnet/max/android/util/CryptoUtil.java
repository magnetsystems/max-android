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
package com.magnet.max.android.util;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
  private static final String HMAC_MD5 = "MD5";

  public static String generateHmacSha1(String data, String key) throws SignatureException {
    String result;
    try {

      SecretKeySpec secretKeySpecKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

      Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
      mac.init(secretKeySpecKey);

      // compute the hmac on data
      byte[] bHmac = mac.doFinal(data.getBytes());

      // stringify it
      result = Base64.encodeToString(bHmac, Base64.DEFAULT);

    } catch (Exception e) {
      throw new SignatureException("Failed to generate HMAC-SHA1 : " + e.getMessage());
    }
    return result;

  }

  /**
   * Hash the value with MD5.
   * @param value
   * @return A base64 encoded value.
   */
  public static String generateMd5(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance(HMAC_MD5);
      digest.update(value.getBytes());
      byte[] msgDigest = digest.digest();
      return Base64.encodeToString(msgDigest, Base64.DEFAULT);
    } catch (NoSuchAlgorithmException e) {
      return value;
    }
  }
}
