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
package com.magnet.max.android.logging.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.logging.LoggerOptions;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import retrofit.Callback;
import retrofit.MagnetCall;

/**public**/ class LogFileManager {
  private static final String TAG = LogFileManager.class.getSimpleName();

  private static String PREFIX = "mmlog_";
  private static SimpleDateFormat logFileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
  private static String PREF_KEY_LAST_LOG_CREATED_AT = "com.magnet.max.android.lastLogCreatedAt";

  private static File logsLocation;
  private static File currentLogFile;
  private static BufferedWriter fileWriter;
  private static Long currentLogFileCreatedAt;
  private static long rotatingTimeInMs;
  protected static WeakReference<LoggerOptions> loggerOptionsWeakReference;

  private static LogEventsCollectionService logCollectorService;
  private static SharedPreferences sharedPrefs;

  public static synchronized void init(LoggerOptions loggerOptions) {
    rotatingTimeInMs = loggerOptions.getRollingFileFrequencyInMinutes() * 60 * 1000;
    loggerOptionsWeakReference = new WeakReference<LoggerOptions>(loggerOptions);

    if(null == sharedPrefs) {
      sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MaxCore.getApplicationContext());
    } else {
      long savedLastCreatedAt = sharedPrefs.getLong(PREF_KEY_LAST_LOG_CREATED_AT, 0);
      if(0 != savedLastCreatedAt) {
        currentLogFileCreatedAt = savedLastCreatedAt;
      }
    }

    if(shouldRotateFile()) {
      rotateFile();
    }
  }

  public static synchronized void rotateFile() {
    if(fileWriter != null) {
      try {
        Log.d(TAG, "-----------rotating log file " + currentLogFile.getName());
        fileWriter.flush();
        fileWriter.close();
      } catch (Throwable e) {
        Log.e(TAG, "Error when flush file", e);
      } finally {
        fileWriter =null;
      }
    }

    currentLogFile = new File(getLogDir(), LogFileManager.getNewLogFileName());

    currentLogFileCreatedAt = System.currentTimeMillis();
    SharedPreferences.Editor editor = sharedPrefs.edit();
    editor.putLong(PREF_KEY_LAST_LOG_CREATED_AT, currentLogFileCreatedAt);
    editor.commit();

    if(fileWriter == null){
      try {
        fileWriter = new BufferedWriter( new FileWriter(currentLogFile, true));
      } catch (IOException e) {
        // e.printStackTrace();
      }
    }
  }

  public static void writeRecordsToFile(String jsonString) {
    if (fileWriter != null) {
      try {
        fileWriter.write(jsonString);
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.flush();
      } catch (Exception e) {
        // e.printStackTrace();
      }
    }
  }

  public static boolean shouldRotateFile() {
    return null == currentLogFile || isSizeToRotate() || isTimeToRotate();
  }

  public static File[] listLogFiles(){
    return listLogFiles(null);
  }

  public static File[] listLogFiles(Context context){
    File[] listofLogs = getLogDir(context).listFiles(new FilenameFilter() {
      @Override public boolean accept(File dir, String name) {
        return name.toLowerCase().startsWith(LogFileManager.PREFIX.toLowerCase());
      }
    });
    return listofLogs;
  }

  public static boolean uploadFiles(boolean includingCurrentFile){
    File[] files = listLogFiles();

    if(null == files || files.length == 0) {
      return false;
    }

    for(final File file : files) {
      if(isCurrentFile(file.getName())) {
        if(!includingCurrentFile) {
          continue;
        }
      }

      uploadFile(file, null);
    }

    return true;
  }

  public static synchronized void uploadFile(final File file, final ApiCallback<Boolean> callback){
    if(isCurrentFile(file.getName())) {
      rotateFile();
    }

    RequestBody fileBody = RequestBody.create(MediaType.parse("text/plain"), file);
    MagnetCall<Void> call = getLogCollectorService().addEventsFromFile(fileBody, new Callback<Void>() {
      @Override public void onResponse(retrofit.Response<Void> response) {
        if(response.isSuccess()) {
          Log.d(TAG, "Success uploading file..");
          file.delete();
          if(null != callback) {
            callback.success(true);
          }
        } else {
          if(null != callback) {
            Log.d(TAG, "Uploading file failed due to : " + response.message());
            callback.failure(new ApiError(response.message(), response.code()));
          }
        }
      }

      @Override public void onFailure(Throwable throwable) {
        Log.e(TAG, "Error uploading file.." + throwable.getMessage());
        if(null != callback) {
          callback.failure(new ApiError(throwable));
        }
      }
    });
    call.executeInBackground();
  }

  private static File getLogDir() {
    return getLogDir(null);
  }

  private static File getLogDir(Context context) {
    if(null == logsLocation) {
      Context theContext = null != context ? context : MaxCore.getApplicationContext();
      logsLocation = new File(theContext.getFilesDir() + "/logs/");
      if (!logsLocation.exists()) {
        logsLocation.mkdirs();
      }
    }

    return logsLocation;
  }

  private static LogEventsCollectionService getLogCollectorService() {
    if(null == logCollectorService) {
      logCollectorService = MaxCore.create(LogEventsCollectionService.class);
    }

    return logCollectorService;
  }

  private static String getNewLogFileName() {
    return PREFIX + logFileNameDateFormat.format(new Date());
  }

  private static boolean isSizeToRotate() {
    boolean result = currentLogFile != null && currentLogFile.length() >= loggerOptionsWeakReference.get().getMaximumFileSize();
    if(result) {
      Log.d(TAG, "------rotate file " + currentLogFile.getName()+ " based on size " + loggerOptionsWeakReference.get().getMaximumFileSize());
    }

    return result;
  }

  private static boolean isTimeToRotate() {
    boolean result = null != currentLogFileCreatedAt && (System.currentTimeMillis() - currentLogFileCreatedAt) >= rotatingTimeInMs;
    if(result) {
      Log.d(TAG, "------rotate file " + currentLogFile.getName()+ " based on time interval " + loggerOptionsWeakReference.get().getRollingFileFrequencyInMinutes());
    }

    return result;
  }

  private static boolean isCurrentFile(String fileName) {
    return null != currentLogFile ? currentLogFile.getName().equals(fileName) : false;
  }
}
