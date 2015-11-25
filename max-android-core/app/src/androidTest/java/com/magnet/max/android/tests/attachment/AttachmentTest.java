/*
 *  Copyright (c) 2015 Magnet Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.magnet.max.android.tests.attachment;

import android.test.AndroidTestCase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.magnet.max.android.tests.R;
import com.magnet.max.android.Attachment;
import java.util.Arrays;
import java.util.List;

public class AttachmentTest extends AndroidTestCase {

  public void testMarshalling() {
    Attachment attachment1 = new Attachment(getContext().getResources().openRawResource(
        R.raw.test_image), "image/jpeg", null, null);
    //Attachment attachment2 = new Attachment(new File("path"), "image/jpeg", null, null);
    Attachment attachment3 = new Attachment(new byte[] {0,1,2}, "image/jpeg", null, null);
    Attachment attachment4 = new Attachment("<html></html>", "text/html", null, null, null);

    List<Attachment> attachments = Arrays.asList(attachment1, attachment3, attachment4);

    Gson gson = new GsonBuilder().create();
    String jsonStr = gson.toJson(attachments);

    List<Attachment> attachmentRecovered = gson.fromJson(jsonStr, new TypeToken<List<Attachment>>() {}.getType());
    assertEquals(3, attachmentRecovered.size());
    assertEquals("image/jpeg", attachmentRecovered.get(0).getMimeType());
  }
}
