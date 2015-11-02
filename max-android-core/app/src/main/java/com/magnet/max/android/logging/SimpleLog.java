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

public class SimpleLog extends LogEntry {
  private LEVEL level;
  private String tag;
  private String msg;
  private Throwable thr;

  public SimpleLog(LEVEL level, String tag, String msg, Throwable thr) {
    this.level = level;
    this.tag = tag;
    this.msg = msg;
    this.thr = thr;
  }

  public LEVEL getLevel() {
    return level;
  }

  public void setLevel(LEVEL level) {
    this.level = level;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public Throwable getThr() {
    return thr;
  }

  public void setThr(Throwable thr) {
    this.thr = thr;
  }
}
