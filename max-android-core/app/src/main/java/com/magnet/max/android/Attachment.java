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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.magnet.max.android.util.EqualityUtil;
import com.magnet.max.android.util.HashCodeBuilder;
import com.magnet.max.android.util.ParcelableHelper;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import retrofit.Callback;
import retrofit.Response;

/**
 * Attachment is used to save/download large content (such as file) to/from Max server.
 */
final public class Attachment implements Parcelable {

  /**
   * Status of the attachment
   */
  public enum Status {
    INIT,
    INLINE,
    TRANSFERING,
    COMPLETE,
    ERROR
  }

  /**
   * Type of the source content
   */
  public enum ContentSourceType {
    TEXT,
    FILE,
    INPUT_STREAM,
    BYTE_ARRAY,
    BITMAP
  }

  /**
   * Listener for events uploading a attachment
   */
  public interface UploadListener {
    /**
     * Start to upload the attachment
     * @param attachment
     */
    void onStart(Attachment attachment);
    //void onProgress(Attachment attachment, long processedBytes);

    /**
     * The uploading is completed successfully
     * @param attachment
     */
    void onComplete(Attachment attachment);

    /**
     * The uploading fails
     * @param attachment
     * @param error
     */
    void onError(Attachment attachment, Throwable error);
  }

  protected static abstract class AbstractDownloadListener<T> {
    /**
     * Downloading starts
     */
    public void onStart() {

    }
    //void onProgress(Attachment attachment, long processedBytes);

    /**
     * Downloading finished and return the content
     * @param content
     */
    public abstract void onComplete(T content);

    /**
     * Downloading fails
     * @param error
     */
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
   * The stream should be read in a background thread
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

  public static final String META_FILE_ID = "metadata_file_id";

  public static final String MIME_TYPE_IMAGE = "image";
  public static final String MIME_TYPE_VIDEO = "video";
  public static final String TEXT_PLAIN = "text/plain";
  public static final String TEXT_HTML = "text/html";

  private static File defaultDownloadDir;

  protected transient Status mStatus = Status.INIT;
  protected ContentSourceType mSourceType;
  protected String mName;
  protected String mSummary;
  protected String mMimeType;
  protected long mLength = -1;
  private String charsetName;

  protected transient Object mContent;

  protected transient byte[] mData;
  /** The id to retrieve the attachment from server */
  protected String mAttachmentId;

  private String senderId;

  private transient Map<String, String> mMetaData;

  /**
   * Create from a {@link File}
   * @param content
   * @param mimeType
   */
  public Attachment(File content, String mimeType) {
    this(content, mimeType, null, null);
  }

  /**
   * Create from a file with name and summary
   * @param content
   * @param mimeType
   * @param name
   * @param summary
   */
  public Attachment(File content, String mimeType, String name, String summary) {
    if(null == content) {
      throw new IllegalArgumentException("content shouldn't be null");
    }
    if(!content.exists()) {
      throw new IllegalArgumentException("content file doesn't exist");
    }
    this.mLength = content.length();
    mSourceType = ContentSourceType.FILE;
    create(content, mimeType, name, summary);
  }

  /**
   * Create from a byte array
   * @param content
   * @param mimeType
   */
  public Attachment(byte[] content, String mimeType) {
    this(content, mimeType, null, null);
  }

  /**
   * Create from a byte array with name and summary
   * @param content
   * @param mimeType
   * @param name
   * @param summary
   */
  public Attachment(byte[] content, String mimeType, String name, String summary) {
    if(null == content || content.length == 0) {
      throw new IllegalArgumentException("content shouldn't be empty");
    }
    validateMimeType(mimeType);

    this.mLength = content.length;
    mSourceType = ContentSourceType.BYTE_ARRAY;
    mData = content;
    create(content, mimeType, name, summary);
  }

  /**
   * Create from a {@link InputStream}
   * @param content
   * @param mimeType
   */
  public Attachment(InputStream content, String mimeType) {
    this(content, mimeType, null, null);
  }

  /**
   * Create from a {@link InputStream} with name and summary
   * @param content
   * @param mimeType
   * @param name
   * @param summary
   */
  public Attachment(InputStream content, String mimeType, String name, String summary) {
    if(null == content) {
      throw new IllegalArgumentException("content shouldn't be null");
    }
    validateMimeType(mimeType);

    mSourceType = ContentSourceType.INPUT_STREAM;
    create(content, mimeType, name, summary);
  }

  /**
   * Create from a String
   * @param content
   * @param mimeType
   */
  public Attachment(String content, String mimeType) {
    this(content, mimeType, null, null);
  }

  /**
   * Create from a String with name and summary
   * @param content
   * @param mimeType
   * @param name
   * @param description
   */
  public Attachment(String content, String mimeType, /**String charsetName,**/ String name, String description) {
    if(StringUtil.isEmpty(content)) {
      throw new IllegalArgumentException("content shouldn't be empty");
    }
    validateMimeType(mimeType);

    this.mLength = content.length();
    mSourceType = ContentSourceType.TEXT;
    //this.charsetName = charsetName;
    create(content, mimeType, name, description);
  }

  public Attachment(Bitmap content, String mimeType) {
    if(null == content) {
      throw new IllegalArgumentException("content shouldn't be empty");
    }

    validateMimeType(mimeType);

    this.mLength = content.getByteCount();
    mSourceType = ContentSourceType.BITMAP;
    create(content, mimeType, null, null);
  }

  protected void create(Object content, String mimeType, String name, String description) {
    this.mContent = content;
    this.mName = name;
    this.mSummary = description;
    this.mMimeType = mimeType;
    this.mStatus = Status.INIT;
    this.senderId = User.getCurrentUserId();
  }

  private byte[] getAsBytes() {
    if(null == mData && null != mContent) {
      mData = convertToBytes();
      if(null != mData) {
        mLength = mData.length;
      }
    }

    return mData;
  }

  /**
   * The MimeType of the attachment
   * @return
   */
  public String getMimeType() {
    return mMimeType;
  }

  private Object getContent() {
    return mContent;
  }

  /**
   * The the id (returned from Max Server) of the attachment
   * @return
   */
  public String getAttachmentId() {
    return mAttachmentId;
  }

  /**
   * The URL to download the content directly
   * @return
   */
  public String getDownloadUrl() {
    checkIfContentAvailable();

    return createDownloadUrl(mAttachmentId, getSenderId());
  }

  /**
   * Get the status of the attachment
   * @return
   */
  public Status getStatus() {
    if(null == mStatus) {
      mStatus = Status.INIT;
    }
    return mStatus;
  }

  /**
   * Get the name of the attachment
   * @return
   */
  public String getName() {
    return mName;
  }

  /**
   * Get the summary fo the attachment
   * @return
   */
  public String getSummary() {
    return mSummary;
  }

  private ContentSourceType getSourceType() {
    return mSourceType;
  }

  private String getCharsetName() {
    return charsetName;
  }

  /**
   * Get the length of the content. Return -1 if the length it's not available
   * @return
   */
  public long getLength() {
    return mLength;
  }

  private String getSenderId() {
    return senderId;
  }

  /**
   * Upload the attachment to Max Server
   * @param listener
   */
  public void upload(final UploadListener listener) {
    if(StringUtil.isNotEmpty(mAttachmentId)) {
      // Already uploaded
      Log.d(TAG, "Already uploaded");
      if(null != listener) {
        listener.onComplete(this);
      }
      return;
    }

    if(mStatus == Status.INIT || mStatus == Status.ERROR) {
      if(null != listener) {
        listener.onStart(this);
      }

      final AtomicReference<Long> startTime = new AtomicReference<>();
      Callback<String> uploadCallback = new Callback<String>() {
        @Override public void onResponse(Response<String> response) {
          if (response.isSuccess()) {
            String result = response.body();
            if (StringUtil.isNotEmpty(result)) {
              mAttachmentId = result;
              mStatus = Status.COMPLETE;
              Log.d(TAG, "It took " + (System.currentTimeMillis() - startTime.get())/1000 + " seconds to upload attachment " + mAttachmentId);
              if (null != listener) {
                listener.onComplete(Attachment.this);
              }
            } else {
              handleError(new Exception("Can't mAttachmentId from response"));
            }
          } else {
            handleError(new Exception(response.message()));
          }
        }

        @Override public void onFailure(Throwable throwable) {
          handleError(throwable);
        }

        private void handleError(Throwable throwable) {
          mStatus = Status.ERROR;
          Log.d(TAG,
              "Failed to upload attachment " + mName, throwable);
          if (null != listener) {
            listener.onError(Attachment.this, throwable);
          }
        }
      };

      RequestBody requestBody = null;
      if(mSourceType == ContentSourceType.FILE) {
        requestBody = RequestBody.create(MediaType.parse(getMimeType()), (File) mContent);
      } else {
        requestBody = RequestBody.create(MediaType.parse(getMimeType()), getAsBytes());
      }
      startTime.set(System.currentTimeMillis());
      getAttachmentService().upload(mMetaData, requestBody, uploadCallback)
          .executeInBackground();

      mStatus = Status.TRANSFERING;
    } else if(mStatus == Status.TRANSFERING) {
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
    if(StringUtil.isEmpty(destinationFilePath)) {
      throw new IllegalArgumentException("destinationFilePath shouldn't be null");
    }
    download(new File(destinationFilePath), listener);
  }

  /**
   * Download the attachment to a specific file
   * @param destinationFile
   * @param listener
   */
  public void download(File destinationFile, DownloadAsFileListener listener) {
    if(null == destinationFile) {
      throw new IllegalArgumentException("destinationFile shouldn't be null");
    }

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

  /**
   * Added key-value pair meta data for the attachment which will be saved on server
   * @param key
   * @param value
   */
  public void addMetaData(String key, String value) {
    if(StringUtil.isEmpty(key)) {
      throw new IllegalArgumentException("Key shouldn't be null");
    }

    if(null == mMetaData) {
      mMetaData = new HashMap<>();
    }

    mMetaData.put(key, value);
  }

  public void setMetaData(Map<String, String> metaData) {
    if(null != metaData) {
      metaData.clear();
    }
    this.mMetaData = metaData;
  }

  /**
   * Compares this Attachment object with the specified object and indicates if they
   * are equal. Following properties are compared :
   * <p><ul>
   * <li>attachmentId
   * <li>name
   * <li>mimeType
   * <li>status
   * <li>length
   * <li>sourceType
   * </ul><p>
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if(!EqualityUtil.quickCheck(this, obj)) {
      return false;
    }

    Attachment theOther = (Attachment) obj;
    return StringUtil.isStringValueEqual(mAttachmentId, theOther.getAttachmentId()) &&
        StringUtil.isStringValueEqual(mName, theOther.getName()) &&
        StringUtil.isStringValueEqual(mMimeType, theOther.getMimeType()) &&
        mStatus == theOther.getStatus() &&
        mLength == theOther.getLength() &&
        mSourceType == theOther.getSourceType();
  }

  /**
   *  Returns an integer hash code for this object.
   *  @see #equals(Object) for the properties used for hash calculation.
   * @return
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().hash(mAttachmentId).hash(mName).hash(mMimeType)
        .hash(mStatus).hash(mLength).hash(mSourceType).hashCode();
  }

  @Override public String toString() {
    return new StringBuilder().append("{")
        .append("attachmentId = ").append(mAttachmentId).append(", ")
        .append("name = ").append(mName).append(", ")
        .append("status = ").append(mStatus).append(", ")
        .append("sourceType = ").append(mSourceType).append(", ")
        .append("mimeType = ").append(mMimeType).append(", ")
        .append("length = ").append(mLength).append(", ")
        .append("metaData = ").append(StringUtil.toString(mMetaData))
        .append("}")
        .toString();
  }

  public static String getMimeType(String fileName, String type) {
    if(StringUtil.isNotEmpty(fileName)) {
      int idx = fileName.lastIndexOf(".");
      if (idx >= 0 && idx < fileName.length() - 1) {
        String format = fileName.substring(idx + 1);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(format);
      }
    }
    return type + "/*";
  }

  public static String createDownloadUrl(String attachmentId, String ownerId) {
    if(null == ModuleManager.getUserToken()) {
      throw new IllegalStateException("User hasn't login");
    }

    String baseUrl = MaxCore.getConfig().getBaseUrl();
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    if(!baseUrl.endsWith("/")) {
      urlBuilder.append("/");
    }
    urlBuilder.append("com.magnet.server/file/download/").append(attachmentId)
        .append("?access_token=").append(ModuleManager.getUserToken().getAccessToken())
        .append("&").append("user_id=").append(ownerId);

    return urlBuilder.toString();
  }

  protected AttachmentService getAttachmentService() {
    return MaxCore.create(AttachmentService.class);
  }

  protected byte[] convertToBytes() {
    if(mSourceType == ContentSourceType.TEXT) {
      if(null != charsetName) {
        try {
          return ((String) mContent).getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      } else {
        return ((String) mContent).getBytes();
      }
    } else if(mSourceType == ContentSourceType.INPUT_STREAM) {
      return convertInputStreamToBytes((InputStream) mContent);
    } else if(mSourceType == ContentSourceType.BITMAP) {
      Bitmap bitmap = (Bitmap) mContent;
      ByteArrayOutputStream stream = new ByteArrayOutputStream();

      Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
      String imageType = mMimeType.substring(mMimeType.lastIndexOf("/") + 1);
      if("png".equalsIgnoreCase(imageType)) {
        compressFormat = Bitmap.CompressFormat.PNG;
      } else if("webp".equalsIgnoreCase(imageType)) {
        compressFormat = Bitmap.CompressFormat.WEBP;
      } else if(imageType.toLowerCase().endsWith("jpg")) {
        compressFormat = Bitmap.CompressFormat.JPEG;
      }

      try {
        bitmap.compress(compressFormat, 100, stream);
        return stream.toByteArray();
      } catch (Exception e) {
        Log.d(TAG, "Failed to convert bitmap to byte array");
      } finally {
        try {
          stream.close();
        } catch (IOException e) {

        }
      }
    } else if(mSourceType == ContentSourceType.FILE) {
      InputStream in = null;
      try {
        in = new FileInputStream((File)mContent);
        return convertInputStreamToBytes(new FileInputStream((File)mContent));
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
    if(StringUtil.isEmpty(mAttachmentId)) {
      throw new IllegalStateException("AttachmentId is not available");
    }
    if(StringUtil.isEmpty(senderId)) {
      throw new IllegalStateException("SenderId shouldn't be null");
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

  private void validateMimeType(String mimeType) {
    if(StringUtil.isEmpty(mimeType)) {
      throw new IllegalArgumentException("mimeType shouldn't be null");
    }
  }

  private abstract class AbstractDownloader {

    protected final AbstractDownloadListener listener;
    protected final ContentSourceType sourceType;
    protected long startTime;

    abstract protected void doDownload();

    public AbstractDownloader(AbstractDownloadListener listener, ContentSourceType sourceType) {
      this.listener = listener;
      this.sourceType = sourceType;
    }

    public void download() {
      Status currentStatus = getStatus();

      startTime = System.currentTimeMillis();

      if (currentStatus == Status.COMPLETE) {
        //TODO : content is not cached right now, always re-download
        //// Already downloaded
        //if (null != listener) {
        //  listener.onComplete(this);
        //}
        mStatus = Status.INIT;
        if (null != listener) {
          listener.onStart();
        }

        doDownload();

        mStatus = Status.TRANSFERING;
      } else if (currentStatus == Status.INIT) {
        if (null != listener) {
          listener.onStart();
        }

        doDownload();

        mStatus = Status.TRANSFERING;
      } else if(currentStatus == Status.INLINE) {

      } else if (currentStatus == Status.TRANSFERING) {
        throw new IllegalStateException("Attachment is downloading");
      }
    }

    protected void logTime() {
      Log.d(TAG, "It took " + (System.currentTimeMillis() - startTime)/1000 + " seconds to download attachment " + mAttachmentId);
    }
  }

  private class BytesDownloader extends AbstractDownloader {

    public BytesDownloader(AbstractDownloadListener listener) {
      super(listener, ContentSourceType.BYTE_ARRAY);
    }

    @Override protected void doDownload() {
      getAttachmentService().downloadAsBytes(mAttachmentId, senderId, new Callback<byte[]>() {
        @Override public void onResponse(Response<byte[]> response) {
          if (response.isSuccess()) {
            mData = response.body();
            mLength = mData.length;
            mStatus = Status.COMPLETE;
            logTime();
            if (null != listener) {
              listener.onComplete(mData);
            }
          } else {
            handleError(new Exception(response.message()));
          }
        }

        @Override public void onFailure(Throwable throwable) {
          handleError(throwable);
        }

        private void handleError(Throwable throwable) {
          mStatus = Status.ERROR;
          Log.d(TAG, "Failed to download attachment " + mName, throwable);
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
      getAttachmentService().downloadAsStream(mAttachmentId, senderId, new Callback<ResponseBody>() {
        @Override public void onResponse(final Response<ResponseBody> response) {
          if (response.isSuccess()) {
            // Read the InputStream in AsyncTask
            new AsyncTask<Void, Void, Exception>() {

              @Override protected Exception doInBackground(Void... params) {
                try {
                  writeInputStreamToFile(response.body().byteStream(), destinationFile);
                  mStatus = Attachment.Status.COMPLETE;
                  mLength = destinationFile.length();
                  logTime();
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
          mStatus = Status.ERROR;
          Log.d(TAG, "Failed to download attachment " + mName + " to file " + destinationFile.getAbsolutePath(), throwable);
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
      getAttachmentService().downloadAsStream(mAttachmentId, senderId, new Callback<ResponseBody>() {
        @Override public void onResponse(Response<ResponseBody> response) {
          if (response.isSuccess()) {
            mStatus = Status.COMPLETE;
            try {
              mLength = response.body().contentLength();
              logTime();
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
          mStatus = Status.ERROR;
          Log.d(TAG, "Failed to download attachment " + mName + " to stream ", throwable);
          if (null != listener) {
            listener.onError(throwable);
          }
        }
      }).executeInBackground();
    }
  }

  //----------------Parcelable Methods----------------

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.mStatus == null ? -1 : this.mStatus.ordinal());
    dest.writeInt(this.mSourceType == null ? -1 : this.mSourceType.ordinal());
    dest.writeString(this.mName);
    dest.writeString(this.mSummary);
    dest.writeString(this.mMimeType);
    dest.writeLong(this.mLength);
    dest.writeString(this.charsetName);
    dest.writeByteArray(this.mData);
    dest.writeString(this.mAttachmentId);
    dest.writeString(this.senderId);
    dest.writeBundle(ParcelableHelper.stringMapToBundle(this.mMetaData));
  }

  protected Attachment(Parcel in) {
    int tmpStatus = in.readInt();
    this.mStatus = tmpStatus == -1 ? null : Status.values()[tmpStatus];
    int tmpSourceType = in.readInt();
    this.mSourceType = tmpSourceType == -1 ? null : ContentSourceType.values()[tmpSourceType];
    this.mName = in.readString();
    this.mSummary = in.readString();
    this.mMimeType = in.readString();
    this.mLength = in.readLong();
    this.charsetName = in.readString();
    this.mContent = in.readParcelable(Object.class.getClassLoader());
    this.mData = in.createByteArray();
    this.mAttachmentId = in.readString();
    this.senderId = in.readString();
    this.mMetaData = ParcelableHelper.stringMapfromBundle(in.readBundle(getClass().getClassLoader()));
  }

  protected Attachment() {
  }

  public static final Parcelable.Creator<Attachment> CREATOR =
      new Parcelable.Creator<Attachment>() {
        public Attachment createFromParcel(Parcel source) {
          return new Attachment(source);
        }

        public Attachment[] newArray(int size) {
          return new Attachment[size];
        }
      };
}
