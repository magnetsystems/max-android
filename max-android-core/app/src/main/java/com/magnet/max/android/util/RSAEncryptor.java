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

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

public class RSAEncryptor {
  private static final String TAG = RSAEncryptor.class.getSimpleName();

  private final String alias;
  private KeyStore keyStore;

  public RSAEncryptor(Context context, String alias) {
    this.alias = alias;

    try {
      keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);

      if (!keyStore.containsAlias(alias)) {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
          Calendar start = Calendar.getInstance();
          Calendar end = Calendar.getInstance();
          end.add(Calendar.YEAR, 99);
          generator.initialize(new KeyPairGeneratorSpec.Builder(context).setAlias(alias)
              .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
              .setSerialNumber(BigInteger.ONE)
              .setStartDate(start.getTime())
              .setEndDate(end.getTime())
              .build());
        } else {
          generator.initialize(1024);
        }

        KeyPair keyPair = generator.generateKeyPair();
      }
    } catch(Exception e) {}
  }

  public String encryptString(String plain) {
    if(StringUtil.isEmpty(plain)) {
      return null;
    }

    CipherOutputStream cipherOutputStream = null;
    try {
      KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias,
          null);
      RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

      Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
      inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      cipherOutputStream = new CipherOutputStream(
          outputStream, inCipher);
      cipherOutputStream.write(plain.getBytes("UTF-8"));

      byte [] vals = outputStream.toByteArray();
      return Base64.encodeToString(vals, Base64.DEFAULT);
    } catch (Exception e) {
      Log.e(TAG, Log.getStackTraceString(e));
    } finally {
      try {
        cipherOutputStream.close();
      } catch (IOException e) {
        //e.printStackTrace();
      }
    }

    return null;
  }

  public String decryptString(String cipherText) {
    if(StringUtil.isEmpty(cipherText)) {
      return null;
    }

    CipherInputStream cipherInputStream = null;
    try {
      KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias,
          null);
      RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

      Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
      output.init(Cipher.DECRYPT_MODE, privateKey);

      cipherInputStream = new CipherInputStream(
          new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
      ArrayList<Byte> values = new ArrayList<>();
      int nextByte;
      while ((nextByte = cipherInputStream.read()) != -1) {
        values.add((byte)nextByte);
      }

      byte[] bytes = new byte[values.size()];
      for(int i = 0; i < bytes.length; i++) {
        bytes[i] = values.get(i).byteValue();
      }

      String finalText = new String(bytes, 0, bytes.length, "UTF-8");
      return finalText;
    } catch (Exception e) {
      Log.e(TAG, Log.getStackTraceString(e));
    } finally {
      try {
        cipherInputStream.close();
      } catch (IOException e) {
        //e.printStackTrace();
      }
    }

    return null;
  }
}
