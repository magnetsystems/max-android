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
package com.magnet.max.android.logging;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.logging.appender.AndroidLoggerAppender;
import com.magnet.max.android.logging.appender.LogAppender;
import com.magnet.max.android.logging.remote.FileAndRemoteLogAppender;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class Logger {
  private static final String TAG = Logger.class.getSimpleName();

  private String category;

  private static final int BUFFER_SIZE = 10000;
  private static Map<String,Logger> mmLoggerMap = new ConcurrentHashMap<>();
  private static final String ROOT = "ROOT";
  private static ThreadLocal<String> correlationId = new ThreadLocal<>();

  //small buffer before write to file.
  private static BlockingQueue<LogEntry> logEventsQueue = new LinkedBlockingDeque<LogEntry>(BUFFER_SIZE);
  private static DefaultLoggerOptions loggerOptions;
  private static WeakReference<Context> contextWeakReference;
  private static Set<LogAppender> appenders;

  private static LoggerThread loggerThread;

  static{
    mmLoggerMap.put(ROOT, new Logger(ROOT));
  }

  private  Logger(String component){
    this.category = component;
  }

  public static Logger getInstance(Class clazz) {
    Logger logger = mmLoggerMap.get(clazz.getName());

    if(logger==null) {
      logger = new Logger(clazz.getName());
      mmLoggerMap.put(clazz.getName(), logger);
    }
    return  logger;
  }

  public static void startLogging(DefaultLoggerOptions options){
    contextWeakReference = new WeakReference<Context>(MaxCore.getApplicationContext());
    loggerOptions = options;

    appenders = new HashSet<>();

    if(options.isRemoteLoggingEnabled()) {
      appenders.add(new FileAndRemoteLogAppender(options));

      loggerThread = new LoggerThread("Magnet Logger- #1");
      loggerThread.start();
    }
    if(options.isConsoleLogggingEnabled()) {
      appenders.add(new AndroidLoggerAppender());
    }
  }

  public static void stopLogging() {
    flush();
    if(loggerOptions.isRemoteLoggingEnabled()) {
      loggerThread.quit();
    }
    for(LogAppender appender : appenders) {
      appender.stop();
    }
  }

  public static void flush() {
    drainLogQueue();
    for(LogAppender appender : appenders) {
      appender.flush();
    }
  }

  /**
   * Send a DEBUG log message.
   *
   * @param msg The message you would like logged.
   */
  public void d(String tag, String msg) {
    log(LEVEL.DEBUG, tag, msg, null);
  }

  /**
   * Send a DEBUG log message.
   */
  public void d(String msg) {
    log(LEVEL.DEBUG, null, msg, null);
  }

  /**
   * Send a DEBUG log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void d(String tag, String msg, Throwable thr) {
    log(LEVEL.DEBUG, tag, msg, thr);
  }

  /**
   * Send a DEBUG log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void d(String msg, Throwable thr) {
    log(LEVEL.DEBUG, null, msg, thr);
  }

  /**
   * Send a ERROR log message.
   *
   * @param msg The message you would like logged.
   */
  public void e(String tag, String msg) {
    log(LEVEL.ERROR, tag, msg, null);
  }

  /**
   * Send an ERROR log message.
   *
   * @param msg The message you would like logged.
   */
  public void e(String msg) {
    log(LEVEL.ERROR, null, msg, null);
  }

  /**
   * Send a ERROR log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void e(String tag, String msg, Throwable thr) {
    log(LEVEL.ERROR, tag, msg, thr);
  }

  /**
   * Send an ERROR log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void e(String msg, Throwable thr) {
    log(LEVEL.ERROR, null, msg, thr);
  }

  /**
   * Send a INFO log message.
   *
   * @param msg The message you would like logged.
   */
  public void i(String tag, String msg) {
    log(LEVEL.INFO, tag, msg, null);
  }

  /**
   * Send an INFO log message.
   *
   * @param msg The message you would like logged.
   */
  public void i(String msg) {
    log(LEVEL.INFO, null, msg, null);
  }

  /**
   * Send a INFO log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void i(String tag, String msg, Throwable thr) {
    log(LEVEL.INFO, tag, msg, thr);
  }

  /**
   * Send a INFO log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void i(String msg, Throwable thr) {
    log(LEVEL.INFO, null, msg, thr);
  }

  /**
   * Send a VERBOSE log message.
   *
   * @param msg The message you would like logged.
   */
  public void v(String tag, String msg) {
    log(LEVEL.VERBOSE, tag, msg, null);
  }

  /**
   * Send a VERBOSE log message.
   *
   * @param msg The message you would like logged.
   */
  public void v(String msg) {
    log(LEVEL.VERBOSE, null, msg, null);
  }

  /**
   * Send a VERBOSE log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void v(String tag, String msg, Throwable thr) {
    log(LEVEL.VERBOSE, tag, msg, thr);
  }

  /**
   * Send a VERBOSE log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void v(String msg, Throwable thr) {
    log(LEVEL.VERBOSE, null, msg, thr);
  }

  /**
   * Send an empty WARN log message and log the exception.
   *
   * @param thr An exception to log
   */
  public void w(Throwable thr) {
    log(LEVEL.WARN, null, null, thr);
  }

  /**
   * Send a WARN log message.
   *
   * @param msg The message you would like logged.
   */
  public void w(String tag, String msg) {
    log(LEVEL.WARN, tag, msg, null);
  }

  /**
   * Send a WARN log message
   *
   * @param msg The message you would like logged.
   */
  public void w(String msg) {
    log(LEVEL.WARN, null, msg, null);
  }

  /**
   * Send a WARN log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void w(String tag, String msg, Throwable thr) {
    log(LEVEL.WARN, tag, msg, thr);
  }

  /**
   * Send a WARN log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void w(String msg, Throwable thr) {
    log(LEVEL.WARN, null, msg, thr);
  }

  /**
   * Send an empty What a Terrible Failure log message and log the exception.
   *
   * @param thr An exception to log
   */
  public void wtf(Throwable thr) {
    log(LEVEL.ASSERT, null, null, thr);
  }

  /**
   * Send a What a Terrible Failure log message.
   *
   * @param msg The message you would like logged.
   */
  public void wtf(String tag, String msg) {
    log(LEVEL.ASSERT, tag, msg, null);
  }

  /**
   * Send a What a Terrible Failure log message
   *
   * @param msg The message you would like logged.
   */
  public void wtf(String msg) {
    log(LEVEL.ASSERT, null, msg, null);
  }

  /**
   * Send a What a Terrible Failure log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void wtf(String tag, String msg, Throwable thr) {
    log(LEVEL.ASSERT, tag, msg, thr);
  }

  /**
   * Send a What a Terrible Failure log message and log the exception.
   *
   * @param msg The message you would like logged.
   * @param thr An exception to log
   */
  public void wtf(String msg, Throwable thr) {
    log(LEVEL.ASSERT, null, msg, thr);
  }



  public void logEvent(EventLog event) {
    postEntry(event);
  }

  private void log(LEVEL level, String tag, String msg, Throwable tr) {
    postEntry(new SimpleLog(level, tag, msg, tr));
  }

  private void postEntry(LogEntry entry) {
    if(loggerOptions.isRemoteLoggingEnabled()) {
      try {
        logEventsQueue.put(entry);
        loggerThread.postTask(new LoggerTask());
      } catch (Throwable thr) {

      }
    } else {
      for(LogAppender appender : appenders) {
        appender.append(entry);
      }
    }
  }

  private static void drainLogQueue() {
    try {
      LogEntry record = logEventsQueue.poll(1000, TimeUnit.MILLISECONDS);
      if (record != null) {
        int qSize = logEventsQueue.size();
        List<LogEntry> batchRecords = new ArrayList<LogEntry>(qSize);
        batchRecords.add(record);

        if(qSize>1) {
          logEventsQueue.drainTo(batchRecords, BUFFER_SIZE);
          Log.d(TAG, "Drained  " + batchRecords.size());
        }

        for(LogAppender appender : appenders) {
          appender.append(batchRecords);
        }
      }
    } catch (InterruptedException e) {
    }
  }

  private static class LoggerTask implements Runnable {

    public LoggerTask(){

    }

    @Override
    public void run() {
      drainLogQueue();
    }
  }

  private static class LoggerThread extends HandlerThread {

    private Handler mWorkerHandler;

    public LoggerThread(String name) {
      super(name);
    }

    @Override
    protected void onLooperPrepared() {
      mWorkerHandler = new Handler(getLooper());
    }

    public void postTask(Runnable task){
      mWorkerHandler.post(task);
    }
  }

}
