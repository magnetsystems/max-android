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

/**
 * This class is used to pass error to API caller
 */
public class ApiError extends RuntimeException {
  /** Network error */
  public static int API_ERROR_KIND_NETWORK = 1;
  /** Unexpected error */
  public static final int API_ERROR_UNDEFINED = 100;

  /**
   * The kind of error. Except predefined @see ApiError#API_ERROR_KIND_NETWORK and @see ApiError#API_ERROR_UNDEFINED,
   * it could be the HTTP status code returned from server
   */
  private final int kind;

  /**
   * Construct a ApiError with error message
   * @param message
   */
  public ApiError(String message) {
    this(message, API_ERROR_UNDEFINED, null);
  }

  /**
   * Construct a ApiError with throwable
   * @param exception
   */
  public ApiError(Throwable exception) {
    this(exception.getMessage(), API_ERROR_UNDEFINED, exception);
  }

  /**
   * Construct a ApiError with error message and kind
   * @param message
   * @param kind
   */
  public ApiError(String message, int kind) {
    this(message, kind, null);
  }

  /**
   * Construct a ApiError with error message, kind and throwable
   * @param message
   * @param exception
   */
  public ApiError(String message, Throwable exception) {
    this(message, API_ERROR_UNDEFINED, exception);
  }

  /**
   * Construct a ApiError with error message, kind and throwable
   * @param message
   * @param kind
   * @param exception
   */
  public ApiError(String message, int kind, Throwable exception) {
    super(message, exception);
    this.kind = kind;
  }

  /**
   * The kind of the error. Except predefined @see ApiError#API_ERROR_KIND_NETWORK and @see ApiError#API_ERROR_UNDEFINED,
   * it could be the HTTP status code returned from server
   * @return
   */
  public int getKind() {
    return this.kind;
  }
}
