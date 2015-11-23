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
import com.magnet.max.android.attachment.Attachment;
import com.magnet.max.android.attachment.BytesAttachment;
import com.magnet.max.android.attachment.FileAttachment;
import com.magnet.max.android.attachment.InputStreamAttachment;
import com.magnet.max.android.attachment.TextAttachment;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

public class AttachmentTest extends AndroidTestCase {

  public void testMarshalling() {
    Attachment attachment1 = new InputStreamAttachment("image/jpeg", getContext().getResources().openRawResource(
        R.raw.test_image));
    Attachment attachment2 = new FileAttachment("image/jpeg", new File("path"));
    Attachment attachment3 = new BytesAttachment("image/jpeg", new byte[] {});
    Attachment attachment4 = new TextAttachment("text/html", "<html></html>");

    List<Attachment> attachments = Arrays.asList(attachment1, attachment2, attachment3, attachment4);

    Gson gson = new GsonBuilder().create();
    String jsonStr = gson.toJson(attachments);

    List<Attachment> attachmentRecovered = gson.fromJson(jsonStr, new TypeToken<List<Attachment>>() {}.getType());
    assertEquals(4, attachmentRecovered.size());
  }
}
