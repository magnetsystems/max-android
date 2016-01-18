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

public enum  LEVEL {
  OFF(0, null),
  VERBOSE(2, "V"),
  DEBUG(3, "D"),
  INFO(4, "I"),
  WARN(5, "W"),
  ERROR(6, "E"),
  ASSERT(7, "A");

  final String levelString;
  final int level;

  //Supress default constructor for noninstantiability
  LEVEL() {
    throw new AssertionError();
  }

  LEVEL(int level, String levelString) {
    this.level = level;
    this.levelString = levelString;
  }

  public String getLevelString() {
    return this.levelString;
  }

  public int getLevel() {
    return this.level;
  }
}
