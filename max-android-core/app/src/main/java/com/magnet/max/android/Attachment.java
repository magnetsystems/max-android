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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.magnet.max.android.util.StringUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import retrofit.Callback;
import retrofit.Response;

public class Attachment {
  public enum Status {
    INIT,
    INLINE,
    TRANSFERING,
    COMPLETE,
    ERROR
  }

  public enum ContentSourceType {
    TEXT,
    FILE,
    INPUT_STREAM,
    BYTE_ARRAY
  }

  /**
   * Listener for events uploading a attachment
   */
  public interface UploadListener {
    void onStart(Attachment attachment);
    //void onProgress(Attachment attachment, long processedBytes);
    void onComplete(Attachment attachment);
    void onError(Attachment attachment, Throwable error);
  }

  public static abstract class AbstractDownloadListener<T> {
    public void onStart() {

    }
    //void onProgress(Attachment attachment, long processedBytes);
    public abstract void onComplete(T content);
    public abstract void onError(Throwable error);
  }

  /**
   * Download the content of attachment as byte array
   */
  public static abstract class DownloadAsBytesListener extends AbstractDownloadListener<byte[]> {
  }

  /**
   * Download the content of attachment to a file
   */
  public static abstract class DownloadAsFileListener extends AbstractDownloadListener<File> {
  }

  /**
   * Download the content of attachment as {@link InputStream}
   * The stream should be read in a backgroud thread
   */
  public static abstract class DownloadAsStreamListener extends AbstractDownloadListener<InputStream> {
  }

  /**
   * Download the content of attachment as String
   */
  public static abstract class DownloadAsStringListener extends AbstractDownloadListener<String> {
  }

  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
  private static final String TAG = Attachment.class.getSimpleName();

  public static final String TEXT_PLAIN = "text/plain";
  public static final String TEXT_HTML = "text/html";

  private static File defaultDownloadDir;

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

  /**
   * Create from a {@link File}
   * @param content
   * @param mimeType
   */
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
    this(content, mimeType, null, null);
  }

  public Attachment(String content, String mimeType, /**String charsetName,**/ String name, String description) {
    if(StringUtil.isEmpty(content)) {
      throw new IllegalArgumentException("content shouldn't be empty");
    }
    this.length = content.length();
    sourceType = ContentSourceType.TEXT;
    //this.charsetName = charsetName;
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

  /**
   * The URL to download the content directly
   * @return
   */
  public String getDownloadUrl() {
    if(null == downloadUrl) {
      checkIfContentAvailable();

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

  public void upload(final UploadListener listener) {
    if(StringUtil.isNotEmpty(attachmentId)) {
      // Already uploaded
      Log.d(TAG, "Aready uploaded");
      if(null != listener) {
        listener.onComplete(this);
      }
      return;
    }

    if(status == Status.INIT || status == Status.ERROR) {
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
          status = Status.ERROR;
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

  /**
   * Download the attachment as byte array
   * @param listener
   */
  public void download(DownloadAsBytesListener listener) {
    checkIfContentAvailable();

    AbstractDownloader downloader = new BytesDownloader(listener);
    downloader.download();
  }

  /**
   * Download the attachment to a specific file path
   * @param destinationFilePath
   * @param listener
   */
  public void download(String destinationFilePath, DownloadAsFileListener listener) {
    download(new File(destinationFilePath), listener);
  }

  /**
   * Download the attachment to a specific file
   * @param destinationFile
   * @param listener
   */
  public void download(File destinationFile, DownloadAsFileListener listener) {
    checkIfContentAvailable();

    AbstractDownloader downloader = new FileDownloader(destinationFile, listener);
    downloader.download();
  }

  /**
   * Download the attachment to a temp file under messageAttachments in app data directory
   * @param listener
   */
  public void download(DownloadAsFileListener listener) {
    File dir = getDefaultDownloadDir();
    if(null != dir) {
      File destinationFile = new File(dir, UUID.randomUUID().toString());
      download(destinationFile, listener);
    } else {
      throw new IllegalStateException("Can't get local dir to download attachment");
    }
  }

  /**
   * Download the attachment as {@link InputStream}
   * @param listener
   */
  public void download(DownloadAsStreamListener listener) {
    checkIfContentAvailable();

    AbstractDownloader downloader = new StreamDownloader(listener);
    downloader.download();
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

  private void checkIfContentAvailable() {
    if(StringUtil.isEmpty(attachmentId)) {
      throw new IllegalStateException("AttachmentId is not available");
    }
  }

  private static File getDefaultDownloadDir() {
    if(null == defaultDownloadDir) {
      Context theContext = MaxCore.getApplicationContext();
      if(null != theContext) {
        defaultDownloadDir = new File(theContext.getFilesDir() + "/messageAttachments/");
        if (!defaultDownloadDir.exists()) {
          defaultDownloadDir.mkdirs();
        }
      }
    }

    return defaultDownloadDir;
  }

  private abstract class AbstractDownloader {

    protected final AbstractDownloadListener listener;
    protected final ContentSourceType sourceType;

    abstract protected void doDownload();

    public AbstractDownloader(AbstractDownloadListener listener, ContentSourceType sourceType) {
      this.listener = listener;
      this.sourceType = sourceType;
    }

    public void download() {
      Status currentStatus = getStatus();
      if (currentStatus == Status.COMPLETE) {
        //TODO : content is not cached right now, alway re-download
        //// Already downloaded
        //if (null != listener) {
        //  listener.onComplete(this);
        //}
        status = Status.INIT;
        if (null != listener) {
          listener.onStart();
        }

        doDownload();

        status = Status.TRANSFERING;
      } else if (currentStatus == Status.INIT) {
        if (null != listener) {
          listener.onStart();
        }

        doDownload();

        status = Status.TRANSFERING;
      } else if(currentStatus == Status.INLINE) {

      } else if (currentStatus == Status.TRANSFERING) {
        throw new IllegalStateException("Attachment is being downloading");
      }
    }
  }

  private class BytesDownloader extends AbstractDownloader {

    public BytesDownloader(AbstractDownloadListener listener) {
      super(listener, ContentSourceType.BYTE_ARRAY);
    }

    @Override protected void doDownload() {
      getAttachmentService().downloadAsBytes(attachmentId, new Callback<byte[]>() {
        @Override public void onResponse(Response<byte[]> response) {
          if (response.isSuccess()) {
            data = response.body();
            length = data.length;
            status = Status.COMPLETE;
            if (null != listener) {
              listener.onComplete(data);
            }
          } else {
            handleError(new Exception(response.message()));
          }
        }

        @Override public void onFailure(Throwable throwable) {
          handleError(throwable);
        }

        private void handleError(Throwable throwable) {
          status = Status.ERROR;
          Log.d(TAG, "Failed to download attachment " + name, throwable);
          if (null != listener) {
            listener.onError(throwable);
          }
        }
      }).executeInBackground();
    }
  }

  private class FileDownloader extends AbstractDownloader {

    private final File destinationFile;

    public FileDownloader(File destinationFile, AbstractDownloadListener listener) {
      super(listener, ContentSourceType.FILE);
      this.destinationFile = destinationFile;
    }

    @Override protected void doDownload() {
      getAttachmentService().downloadAsStream(attachmentId, new Callback<ResponseBody>() {
        @Override public void onResponse(final Response<ResponseBody> response) {
          if (response.isSuccess()) {
            // Read the InputStream in AsyncTask
            new AsyncTask<Void, Void, Exception>() {

              @Override protected Exception doInBackground(Void... params) {
                try {
                  writeInputStreamToFile(response.body().byteStream(), destinationFile);
                  status = Attachment.Status.COMPLETE;
                  length = destinationFile.length();
                } catch (IOException e) {
                  return e;
                }

                return null;
              }

              @Override protected void onPostExecute(Exception exception) {
                if(null == exception) {
                  if (null != listener) {
                    listener.onComplete(destinationFile);
                  }
                } else {
                  handleError(exception);
                }
              }

            }.execute();
          } else {
            handleError(new Exception(response.message()));
          }
        }

        @Override public void onFailure(Throwable throwable) {
          handleError(throwable);
        }

        private void writeInputStreamToFile(InputStream is, File destinationFile) {
          OutputStream outputStream = null;
          try {
            outputStream = new FileOutputStream(destinationFile);

            int read = 0;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];

            while ((read = is.read(bytes)) != -1) {
              outputStream.write(bytes, 0, read);
            }
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            if (is != null) {
              try {
                is.close();
              } catch (IOException e) {
              }
            }
            if (outputStream != null) {
              try {
                outputStream.close();
              } catch (IOException e) {
              }
            }
          }
        }

        private void handleError(Throwable throwable) {
          status = Status.ERROR;
          Log.d(TAG, "Failed to download attachment " + name + " to file " + destinationFile.getAbsolutePath(), throwable);
          if (null != listener) {
            listener.onError(throwable);
          }
        }
      }).executeInBackground();
    }
  }

  private class StreamDownloader extends AbstractDownloader {

    public StreamDownloader(AbstractDownloadListener listener) {
      super(listener, ContentSourceType.INPUT_STREAM);
    }

    @Override protected void doDownload() {
      getAttachmentService().downloadAsStream(attachmentId, new Callback<ResponseBody>() {
        @Override public void onResponse(Response<ResponseBody> response) {
          if (response.isSuccess()) {
            status = Status.COMPLETE;
            try {
              length = response.body().contentLength();
              if (null != listener) {
                listener.onComplete(response.body().byteStream());
              }
            } catch (IOException e) {
              handleError(e);
            }
          } else {
            handleError(new Exception(response.message()));
          }
        }


        @Override public void onFailure(Throwable throwable) {
          handleError(throwable);
        }

        private void handleError(Throwable throwable) {
          status = Status.ERROR;
          Log.d(TAG, "Failed to download attachment " + name + " to stream ", throwable);
          if (null != listener) {
            listener.onError(throwable);
          }
        }
      }).executeInBackground();
    }
  }
}
