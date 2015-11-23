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

import android.util.Log;
import com.google.gson.annotations.Expose;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.util.StringUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import java.lang.String;import java.lang.Throwable;
import java.util.Map;
import retrofit.Callback;
import retrofit.Response;

public abstract class Attachment<T> {
  public enum Status {
    INIT,
    INLINE,
    TRANSFERING,
    COMPLETE
  }

  private static final String TAG = Attachment.class.getSimpleName();

  public static final String TEXT_PLAIN = "text/plain";
  public static final String TEXT_HTML = "text/html";

  public interface AttachmentTrasferLister {
    void onStart(Attachment attachment);
    void onProgress(Attachment attachment, long processedBytes);
    void onComplete(Attachment attachment);
    void onError(Attachment attachment, Throwable error);
  }

  protected transient Status status = Status.INIT;
  protected String name;
  protected String summary;
  protected String mimeType;
  protected long length = -1;

  protected transient T content;

  protected transient byte[] data;
  /** The id to retrieve the attachement from server */
  protected String attachmentId;

  protected transient AttachmentService attachmentService;

  public Attachment(String mimeType, T content, long length) {
    this(null, null, mimeType, length, content);
  }

  public Attachment(String name, String description, String mimeType, long length, T content) {
    this.name = name;
    this.summary = description;
    this.mimeType = mimeType;
    this.length = length;
    this.content = content;
  }

  abstract protected byte[] convertToBytes();

  public byte[] getAsBytes() {
    if(null == data && null != content) {
      data = convertToBytes();
      if(null != data) {
        length = data.length;
      }
    }

    return data;
  }

  public String getMimeType() {
    return mimeType;
  }

  public T getContent() {
    return content;
  }

  public String getAttachmentId() {
    return attachmentId;
  }

  public Status getStatus() {
    return status;
  }

  public String getName() {
    return name;
  }

  public String getSummary() {
    return summary;
  }

  public long getLength() {
    return length;
  }

  public void upload(final AttachmentTrasferLister lister) {
    if(StringUtil.isNotEmpty(attachmentId)) {
      // Already uploaded
      Log.d(TAG, "Aready uploaded");
      lister.onComplete(this);
      return;
    }

    if(status == Status.INIT) {
      getAttachmentService().upload(
          RequestBody.create(MediaType.parse(getMimeType()), getAsBytes()),
          new Callback<String>() {
            @Override public void onResponse(Response<String> response) {
              if (response.isSuccess()) {
                String result = response.body();
                if (StringUtil.isNotEmpty(result)) {
                  attachmentId = result;
                  status = Status.COMPLETE;
                  if (null != lister) {
                    lister.onComplete(Attachment.this);
                  }
                } else {
                  handleError(new Exception("Can't attachmentId from response"));
                }
              } else {
                handleError(new Exception(response.message()));
              }
            }

            @Override public void onFailure(Throwable throwable) {
              handleError(throwable);
            }

            private void handleError(Throwable throwable) {
              Log.d(TAG,
                  "Failed to upload attachment " + name, throwable);
              if (null != lister) {
                lister.onError(Attachment.this, throwable);
              }
            }
          }).executeInBackground();

      status = Status.TRANSFERING;
    } else if(status == Status.TRANSFERING) {
      throw new IllegalStateException("Attachment is being uploading");
    }
  }

  public void download(final AttachmentTrasferLister lister) {
    if(StringUtil.isEmpty(attachmentId)) {
      throw new IllegalStateException("AttachmentId is not available");
    }

    if(status == Status.COMPLETE || status == Status.INLINE) {
      // Already downloaded
      lister.onComplete(this);
    } else if(status == Status.INIT) {
      getAttachmentService().download(attachmentId, new Callback<byte[]>() {
        @Override public void onResponse(Response<byte[]> response) {
          if (response.isSuccess()) {
            data = response.body();
            length = data.length;
            status = Status.COMPLETE;
            if (null != lister) {
              lister.onComplete(Attachment.this);
            }
          } else {
            handleError(new Exception(response.message()));
          }
        }

        @Override public void onFailure(Throwable throwable) {

        }

        private void handleError(Throwable throwable) {
          Log.d(TAG,
              "Failed to download attachment " + name, throwable);
          if (null != lister) {
            lister.onError(Attachment.this, throwable);
          }
        }
      }).executeInBackground();
      status = Status.TRANSFERING;
    } else if(status == Status.TRANSFERING) {
      throw new IllegalStateException("Attachment is being downloading");
    }
  }

  private AttachmentService getAttachmentService() {
    if(null == attachmentService) {
      attachmentService = MaxCore.create(AttachmentService.class);
    }

    return attachmentService;
  }
}
