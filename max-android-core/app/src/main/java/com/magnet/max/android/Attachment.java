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
package com.magnet.max.android;

import android.util.Log;
import com.magnet.max.android.util.StringUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import retrofit.Callback;
import retrofit.Response;

public class Attachment {
  public enum Status {
    INIT,
    INLINE,
    TRANSFERING,
    COMPLETE
  }

  public enum ContentSourceType {
    TEXT,
    FILE,
    INPUT_STREAM,
    BYTE_ARRAY
  }

  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  private static final String TAG = Attachment.class.getSimpleName();

  public static final String TEXT_PLAIN = "text/plain";
  public static final String TEXT_HTML = "text/html";

  public interface AttachmentTransferLister {
    void onStart(Attachment attachment);
    //void onProgress(Attachment attachment, long processedBytes);
    void onComplete(Attachment attachment);
    void onError(Attachment attachment, Throwable error);
  }

  protected transient Status status = Status.INIT;
  protected ContentSourceType sourceType;
  protected String name;
  protected String summary;
  protected String mimeType;
  protected long length = -1;
  private String charsetName;

  protected transient Object content;

  protected transient byte[] data;
  /** The id to retrieve the attachement from server */
  protected String attachmentId;

  protected transient String downloadUrl;

  protected transient AttachmentService attachmentService;

  public Attachment(File content, String mimeType) {
    this(content, mimeType, null, null);
  }

  public Attachment(File content, String mimeType, String name, String description) {
    if(null == content) {
      throw new IllegalArgumentException("content shouldn't be null");
    }
    if(!content.exists()) {
      throw new IllegalArgumentException("content file doesn't exist");
    }
    this.length = content.length();
    sourceType = ContentSourceType.FILE;
    create(content, mimeType, name, description);
  }

  public Attachment(byte[] content, String mimeType) {
    this(content, mimeType, null, null);
  }

  public Attachment(byte[] content, String mimeType, String name, String description) {
    if(null == content || content.length == 0) {
      throw new IllegalArgumentException("content shouldn't be empty");
    }
    this.length = content.length;
    sourceType = ContentSourceType.BYTE_ARRAY;
    data = content;
    create(content, mimeType, name, description);
  }

  public Attachment(InputStream content, String mimeType) {
    this(content, mimeType, null, null);
  }

  public Attachment(InputStream content, String mimeType, String name, String description) {
    if(null == content) {
      throw new IllegalArgumentException("content shouldn't be null");
    }
    sourceType = ContentSourceType.INPUT_STREAM;
    create(content, mimeType, name, description);
  }

  public Attachment(String content, String mimeType) {
    this(content, mimeType, null, null, null);
  }

  public Attachment(String content, String mimeType, String charsetName, String name, String description) {
    if(StringUtil.isEmpty(content)) {
      throw new IllegalArgumentException("content shouldn't be empty");
    }
    this.length = content.length();
    sourceType = ContentSourceType.TEXT;
    this.charsetName = charsetName;
    create(content, mimeType, name, description);
  }

  protected void create(Object content, String mimeType, String name, String description) {
    this.content = content;
    this.name = name;
    this.summary = description;
    this.mimeType = mimeType;
    this.status = Status.INIT;
  }

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

  public Object getContent() {
    return content;
  }

  public String getAttachmentId() {
    return attachmentId;
  }

  public String getDownloadUrl() {
    if(null == downloadUrl) {
      if(StringUtil.isEmpty(attachmentId)) {
        throw new IllegalStateException("Attachment hasn't been uploaded yet");
      }
      if(null == ModuleManager.getUserToken()) {
        throw new IllegalStateException("User hasn't login");
      }

      String baseUrl = MaxCore.getConfig().getBaseUrl();
      StringBuilder urlBuilder = new StringBuilder(baseUrl);
      if(!baseUrl.endsWith("/")) {
        urlBuilder.append("/");
      }
      urlBuilder.append("com.magnet.server/file/download/").append(attachmentId)
          .append("?access_token=").append(ModuleManager.getUserToken().getAccessToken());

      downloadUrl = urlBuilder.toString();
    }

    return downloadUrl;
  }

  public Status getStatus() {
    if(null == status) {
      status = Status.INIT;
    }
    return status;
  }

  public String getName() {
    return name;
  }

  public String getSummary() {
    return summary;
  }

  public ContentSourceType getSourceType() {
    return sourceType;
  }

  public String getCharsetName() {
    return charsetName;
  }

  public long getLength() {
    return length;
  }

  public void upload(final AttachmentTransferLister listener) {
    if(StringUtil.isNotEmpty(attachmentId)) {
      // Already uploaded
      Log.d(TAG, "Aready uploaded");
      if(null != listener) {
        listener.onComplete(this);
      }
      return;
    }

    if(status == Status.INIT) {
      if(null != listener) {
        listener.onStart(this);
      }

      Callback<String> uploadCallback = new Callback<String>() {
        @Override public void onResponse(Response<String> response) {
          if (response.isSuccess()) {
            String result = response.body();
            if (StringUtil.isNotEmpty(result)) {
              attachmentId = result;
              status = Status.COMPLETE;
              if (null != listener) {
                listener.onComplete(Attachment.this);
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
          if (null != listener) {
            listener.onError(Attachment.this, throwable);
          }
        }
      };

      if(sourceType == ContentSourceType.FILE) {
        getAttachmentService().upload(RequestBody.create(MediaType.parse(getMimeType()), (File) content), uploadCallback)
            .executeInBackground();
      } else {
        getAttachmentService().upload(RequestBody.create(MediaType.parse(getMimeType()), getAsBytes()), uploadCallback)
            .executeInBackground();
      }

      status = Status.TRANSFERING;
    } else if(status == Status.TRANSFERING) {
      throw new IllegalStateException("Attachment is being uploading");
    }
  }

  public void download(final AttachmentTransferLister listener) {
    if(StringUtil.isEmpty(attachmentId)) {
      throw new IllegalStateException("AttachmentId is not available");
    }

    if(status == Status.COMPLETE || status == Status.INLINE) {
      // Already downloaded
      if (null != listener) {
        listener.onComplete(this);
      }
    } else if(status == Status.INIT) {
      if (null != listener) {
        listener.onStart(this);
      }
      getAttachmentService().download(attachmentId, new Callback<byte[]>() {
        @Override public void onResponse(Response<byte[]> response) {
          if (response.isSuccess()) {
            data = response.body();
            length = data.length;
            status = Status.COMPLETE;
            if (null != listener) {
              listener.onComplete(Attachment.this);
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
              "Failed to download attachment " + name, throwable);
          if (null != listener) {
            listener.onError(Attachment.this, throwable);
          }
        }
      }).executeInBackground();
      status = Status.TRANSFERING;
    } else if(status == Status.TRANSFERING) {
      throw new IllegalStateException("Attachment is being downloading");
    }
  }

  protected AttachmentService getAttachmentService() {
    if(null == attachmentService) {
      attachmentService = MaxCore.create(AttachmentService.class);
    }

    return attachmentService;
  }

  protected byte[] convertToBytes() {
    if(sourceType == ContentSourceType.TEXT) {
      if(null != charsetName) {
        try {
          return ((String) content).getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      } else {
        return ((String) content).getBytes();
      }
    } else if(sourceType == ContentSourceType.INPUT_STREAM) {
      return convertInputStreamToBytes((InputStream) content);
    } else if(sourceType == ContentSourceType.FILE) {
      InputStream in = null;
      try {
        in = new FileInputStream((File)content);
        return convertInputStreamToBytes(new FileInputStream((File)content));
      } catch (FileNotFoundException e) {
        Log.e(TAG, e.getLocalizedMessage());
      } finally {
        if(null != in) {
          try {
            in.close();
          } catch (IOException e) {

          }
        }
      }
    }

    return null;
  }

  private byte[] convertInputStreamToBytes(InputStream in) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      long count = 0;
      int n = 0;
      while (-1 != (n = in.read(buffer))) {
        output.write(buffer, 0, n);
        count += n;
      }
      if (count <= Integer.MAX_VALUE) {
        return output.toByteArray();
      }
    } catch (IOException e) {
      Log.d(TAG, "Failed to convert input stream to bytes", e);
    }

    return null;
  }
}
