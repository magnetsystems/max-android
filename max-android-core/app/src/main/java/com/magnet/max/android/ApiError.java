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
package com.magnet.max.android;

public class ApiError extends RuntimeException {
  public static int API_ERROR_KIND_NETWORK = 1;
  public static int API_ERROR_HTTP = 2;
  public static int API_ERROR_UNEXPECTED = 100;

  private final int kind;

  public ApiError(String message) {
    this(message, API_ERROR_UNEXPECTED, null);
  }

  public ApiError(Throwable exception) {
    this(null, API_ERROR_UNEXPECTED, exception);
  }

  public ApiError(String message, int kind) {
    this(message, kind, null);
  }

  public ApiError(String message, int kind, Throwable exception) {
    super(message, exception);
    this.kind = kind;
  }

  public int getKind() {
    return this.kind;
  }
}
