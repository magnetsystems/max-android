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

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import java.io.File;

@TargetApi(21)
public class LogUploadJobService extends JobService {
  private static final String TAG = LogUploadJobService.class.getSimpleName();

  private static int jobId = 0;

  private int completedFileCount = 0;
  private int successFileCount = 0;

  @Override public boolean onStartJob(final JobParameters params) {
    Log.d(TAG, "LogUploadJobService started");

    final File[] files = LogFileManager.listLogFiles(this);
    if(null == files || files.length == 0) {
      jobFinished(params, false);
      return false;
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
            Log.d(TAG, "LogUploadJobService finised with completed : "
                + completedFileCount
                + ", success : "
                + successFileCount);
            jobFinished(params, successFileCount < completedFileCount);
          }
        }
      });
    }

    return true;
  }

  @Override public boolean onStopJob(JobParameters params) {
    return false;
  }

  public static int getJobId() {
    return jobId++;
  }
}
