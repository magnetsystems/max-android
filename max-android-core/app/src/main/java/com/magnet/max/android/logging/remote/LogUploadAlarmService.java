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

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import java.io.File;

public class LogUploadAlarmService extends IntentService {
  private static final String TAG = LogUploadAlarmService.class.getSimpleName();

  private int completedFileCount = 0;
  private int successFileCount = 0;

  public LogUploadAlarmService() {
    super(TAG);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "LogUploadAlarmService started");

    final File[] files = LogFileManager.listLogFiles();
    if(null == files || files.length == 0) {
      stopSelf();
      return;
    }

    for(int i = 0; i < files.length; i++) {
      LogFileManager.uploadFile(files[i], new ApiCallback<Boolean>() {
        @Override public void success(Boolean aBoolean) {
          successFileCount++;
          handleResult();
        }

        @Override public void failure(ApiError error) {
          handleResult();
        }

        private void handleResult() {
          completedFileCount++;

          if(completedFileCount == files.length) {
            Log.d(TAG, "LogUploadAlarmService finised with completed : " + completedFileCount + ", success : " + successFileCount);
            stopSelf();
          }
        }
      });
    }
  }
}
