/**
 * Copyright (c) 2012-2015 Magnet Systems. All rights reserved.
 */
package com.magnet.max.android;

import com.magnet.MagnetServiceException;
import retrofit.Response;

/**public*/ class ApiCallbackHelper {

  public static void executeCallback(ApiCallback callback, Response response) {
    if (null != callback && null != response) {
      if (response.isSuccess()) {
        callback.success(response.body());
      } else {
        callback.failure(new ApiError(response.message(), response.code()));
      }
    }
  }

  public static void executeCallback(ApiCallback callback, Throwable throwable) {
    if (null != callback && null != throwable) {
      if(MagnetServiceException.class.equals(throwable.getClass())) {
        MagnetServiceException magnetServiceException = (MagnetServiceException) throwable;
        callback.failure(new ApiError(magnetServiceException.getMessage(), magnetServiceException.getCode(), magnetServiceException));
      } else {
        callback.failure(new ApiError(throwable.getMessage()));
      }
    }
  }
}
