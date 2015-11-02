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
package com.magnet.max.android.logging.appender;

import android.text.TextUtils;
import com.magnet.max.android.logging.LEVEL;
import com.magnet.max.android.logging.LogEntry;
import com.magnet.max.android.logging.SimpleLog;
import java.util.List;

public class AndroidLoggerAppender implements LogAppender {

  @Override public void append(LogEntry logEntry) {
    if(logEntry instanceof SimpleLog) {
      SimpleLog logText = (SimpleLog) logEntry;
      append(logText.getLevel(), logText.getTag(), logText.getMsg(), logText.getThr());
    }
  }

  @Override public void append(List<LogEntry> entries) {
    if(null != entries) {
      for(LogEntry entry : entries) {
        append(entry);
      }
    }
  }

  @Override public void flush() {
    // Do nothing
  }

  @Override public void stop() {
    // Do nothing
  }

  private void append(LEVEL level, String tag, String msg, Throwable thr) {
    switch (level) {
      case VERBOSE:
        if (thr == null) {
          android.util.Log.v(tag, msg);
        } else {
          android.util.Log.v(tag, msg, thr);
        }
        break;
      case DEBUG:
        if (thr == null) {
          android.util.Log.d(tag, msg);
        } else {
          android.util.Log.d(tag, msg, thr);
        }
        break;
      case INFO:
        if (thr == null) {
          android.util.Log.i(tag, msg);
        } else {
          android.util.Log.i(tag, msg, thr);
        }
        break;
      case WARN:
        if (thr == null) {
          android.util.Log.w(tag, msg);
        } else if (TextUtils.isEmpty(msg)) {
          android.util.Log.w(tag, thr);
        } else {
          android.util.Log.w(tag, msg, thr);
        }
        break;
      case ERROR:
        if (thr == null) {
          android.util.Log.e(tag, msg);
        } else {
          android.util.Log.e(tag, msg, thr);
        }
        break;
      case ASSERT:
        if (thr == null) {
          android.util.Log.wtf(tag, msg);
        } else if (TextUtils.isEmpty(msg)) {
          android.util.Log.wtf(tag, thr);
        } else {
          android.util.Log.wtf(tag, msg, thr);
        }
        break;
      default:
        break;
    }
  }
}
