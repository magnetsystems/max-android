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

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.connectivity.ConnectivityManager;
import com.magnet.max.android.logging.EventLog;
import com.magnet.max.android.logging.LogEntry;
import com.magnet.max.android.logging.LoggerOptions;
import com.magnet.max.android.logging.SimpleLog;
import com.magnet.max.android.logging.appender.AbstractLogAppender;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileAndRemoteLogAppender extends AbstractLogAppender {
  private static final String TAG = FileAndRemoteLogAppender.class.getSimpleName();

  private boolean mIsSDK21AndAbove;

  public FileAndRemoteLogAppender(LoggerOptions loggerOptions){
    super(MaxCore.getApplicationContext(), loggerOptions);

    LogFileManager.init(loggerOptions);

    mIsSDK21AndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    if(mIsSDK21AndAbove) {
      Log.d(TAG, "Use JobScheduler to trigger log file uploading.");
    } else {
      Log.d(TAG, "Use AlarmManager to trigger log file uploading directly.");
    }

    //TODO : enable periodic automatic uploading
    //scheduleLogUploading();
  }

  @Override public void append(LogEntry logEntry) {
    EventLog event = toLogEvent(logEntry);
    writeRecordToFile(event);
  }

  @Override public void flush() {
    LogFileManager.uploadFiles(true);
  }

  @Override public void stop() {
  }

  //private void scheduleLogUploading() {
  //  if(mIsSDK21AndAbove) {
  //    scheduleWithJobScheduler();
  //  } else {
  //    scheduleWithAlarmService();
  //  }
  //
  //  Log.d(TAG, "-----------scheduled periodic");
  //}
  //
  //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
  //private void scheduleWithJobScheduler() {
  //  JobInfo.Builder builder = new JobInfo.Builder(LogUploadJobService.getJobId(),
  //      new ComponentName( MaxCore.getApplicationContext().getPackageName(),
  //          LogUploadJobService.class.getName()));
  //  builder.setPeriodic(loggerOptionsWeakReference.get().getLogUploadingFrequencyInMinutes() * 60 * 1000)
  //      .setPersisted(true)
  //      .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
  //          //.setRequiresCharging(true)
  //      .setRequiresDeviceIdle(false);
  //  JobScheduler jobScheduler =
  //      (JobScheduler) MaxCore.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
  //
  //  jobScheduler.schedule(builder.build());
  //}
  //
  //private void scheduleWithAlarmService() {
  //  Intent intent = new Intent(MaxCore.getApplicationContext(), LogUploadAlarmService.class);
  //  PendingIntent pintent = PendingIntent.getService(MaxCore.getApplicationContext(), 0, intent, 0);
  //  AlarmManager alarm = (AlarmManager) MaxCore.getApplicationContext().getSystemService(
  //      Context.ALARM_SERVICE);
  //  alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, Calendar.getInstance().getTimeInMillis(),
  //      loggerOptionsWeakReference.get().getLogUploadingFrequencyInMinutes()* 1000 * 60,
  //      pintent);
  //}

  private void startFileUploadService() {
    //if(mIsSDK21AndAbove) {
    //  OneoffTask oneoff = new OneoffTask.Builder()
    //      //specify target service - must extend GcmTaskService
    //      .setService(LogUploadingService.class)
    //          //tag that is unique to this task (can be used to cancel task)
    //      .setTag(ONE_OFF_UPLOAD_TAG)
    //          //executed between 30 - 60s from now
    //      .setExecutionWindow(0, 60)
    //          //set required network state, this line is optional
    //      .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
    //          //request that charging must be connected, this line is optional
    //          //.setRequiresCharging(true)
    //          //if another task with same tag is already scheduled, replace it with this task
    //      .setUpdateCurrent(true).build();
    //  GcmNetworkManager.getInstance(contextWeakReference.get()).schedule(oneoff);
    //  Log.d(TAG, "-----------scheduled oneoff");
    //} else {
    //  LogFileManager.uploadFiles(true);
    //}

    LogFileManager.uploadFiles(true);
  }

  private EventLog toLogEvent(LogEntry entry) {
    if(entry instanceof SimpleLog) {
      SimpleLog logText = (SimpleLog) entry;

      Map<String,String> map = new HashMap<>(3);

      if(!TextUtils.isEmpty(logText.getMsg())) {
        map.put("__message", logText.getMsg());
      }

      if(null != logText.getThr()) {
        map.put("__stacktrace",Log.getStackTraceString(logText.getThr()));
      }

      return new EventLog.EventBuilder()
          .category(logText.getTag())
          .subCategory(logText.getLevel().toString())
          .correlationId(logText.getCorrelationId())
          .name(logText.getTag())
          .type("SYSTEM")
          .tags(Arrays.asList(new String[] { logText.getLevel().toString(), logText.getTag(), "SYSTEM" }))
          .payload(map).build();
    } else {
      return (EventLog) entry;
    }
  }

  private void writeRecordToFile(EventLog logEvent){
    Gson gson = new Gson();
    String json = gson.toJson(logEvent) + System.getProperty("line.separator");
    writeRecordsToFile(json);
  }

  private void writeRecordsToFile(String jsonString) {
    if(LogFileManager.shouldRotateFile()){
      LogFileManager.rotateFile();

      startFileUploadService();
    }

    //Log.d(MMLoggerThread.class.getName(), "Writing to File.." + currentLogFile.getAbsolutePath());
    LogFileManager.writeRecordsToFile(jsonString);
  }

  private void writeRecordsToFile(List<EventLog> logEvents){
    StringBuilder builder = new StringBuilder();
    Gson gson = new Gson();
    for(EventLog logEvent: logEvents) {
      String json = gson.toJson(logEvent);
      builder.append(json);
      builder.append(System.getProperty("line.separator"));
    }
    writeRecordsToFile(builder.toString());
  }

  private boolean shouldUploadFiles(){
    return ConnectivityManager.getInstance().getConnectivityStatus() != ConnectivityManager.TYPE_NOT_CONNECTED ;
  }
}
