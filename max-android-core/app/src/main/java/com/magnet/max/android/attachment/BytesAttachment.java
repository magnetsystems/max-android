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
package com.magnet.max.android.attachment;

import java.lang.Override;import java.lang.String;public class BytesAttachment extends Attachment<byte[]> {

  public BytesAttachment(String mimeType, byte[] content) {
    super(mimeType, content, null != content ? content.length : -1);
  }

  @Override protected byte[] convertToBytes() {
    return content;
  }
}
