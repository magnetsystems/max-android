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

public class DefaultLoggerOptions implements LoggerOptions {

  private boolean consoleLoggingEnabled = false;

  private boolean remoteLoggingEnabled = true;

  private int rollingFileFrequencyInMinutes = 60 * 60 * 24;

  private int maximumFileSize = 2048 * 1024;

  private int maximumNumberOfLogFiles = 7;

  //private int logUploadingFrequencyInMinutes = 60 * 60 * 24;

  public boolean isConsoleLoggingEnabled() {
    return consoleLoggingEnabled;
  }

  public void setConsoleLoggingEnabled(boolean consoleLoggingEnabled) {
    this.consoleLoggingEnabled = consoleLoggingEnabled;
  }

  @Override public boolean isRemoteLoggingEnabled() {
    return remoteLoggingEnabled;
  }

  public void setRemoteLoggingEnabled(boolean remoteLoggingEnabled) {
    this.remoteLoggingEnabled = remoteLoggingEnabled;
  }

  @Override public int getRollingFileFrequencyInMinutes() {
    return rollingFileFrequencyInMinutes;
  }

  public void setRollingFileFrequencyInMinutes(int rollingFileFrequencyInMinutes) {
    this.rollingFileFrequencyInMinutes = rollingFileFrequencyInMinutes;
  }

  @Override public int getMaximumFileSize() {
    return maximumFileSize;
  }

  public void setMaximumFileSize(int maximumFileSize) {
    this.maximumFileSize = maximumFileSize;
  }

  @Override public int getMaximumNumberOfLogFiles() {
    return maximumNumberOfLogFiles;
  }

  //@Override public int getLogUploadingFrequencyInMinutes() {
  //  return logUploadingFrequencyInMinutes;
  //}

  //public void setLogUploadingFrequencyInMinutes(int logUploadingFrequencyInMinutes) {
  //  this.logUploadingFrequencyInMinutes = logUploadingFrequencyInMinutes;
  //}

  public void setMaximumNumberOfLogFiles(int maximumNumberOfLogFiles) {
    this.maximumNumberOfLogFiles = maximumNumberOfLogFiles;
  }
}
