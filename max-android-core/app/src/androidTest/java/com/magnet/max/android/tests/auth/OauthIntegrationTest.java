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
package com.magnet.max.android.tests.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import com.magnet.max.android.Constants;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.MaxModule;
import com.magnet.max.android.User;
import com.magnet.max.android.Attachment;
import com.magnet.max.android.auth.model.UpdateProfileRequest;
import com.magnet.max.android.auth.model.UserRealm;
import com.magnet.max.android.auth.model.UserRegistrationInfo;
import com.magnet.max.android.config.MaxAndroidConfig;
import com.magnet.max.android.tests.R;
import com.magnet.max.android.tests.utils.MaxAndroidJsonConfig;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class OauthIntegrationTest extends AndroidTestCase implements MaxModule {
  private static final String TAG = OauthIntegrationTest.class.getSimpleName();

  private static final int WAIT_TIME_SECONDS = 5;

  private CountDownLatch onInitSignal = new CountDownLatch(1);
  private CountDownLatch onAppTokenSignal = new CountDownLatch(1);
  private CountDownLatch onUserTokenSignal = new CountDownLatch(1);
  private CountDownLatch onUserTokenInvalidSignal = new CountDownLatch(1);
  private BroadcastReceiver appTokenBroadcastReceiver;
  private BroadcastReceiver userTokenBroadcastReceiver;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    MaxAndroidConfig config = new MaxAndroidJsonConfig(getContext(), R.raw.keys);
    //magnetServiceHttpAdapter = new MagnetServiceAdapter.Builder().config(config).applicationContext(getContext()).build();
    MaxCore.init(getContext(), config);

    appTokenBroadcastReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        fail(intent.getAction());
      }
    };
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(appTokenBroadcastReceiver, new IntentFilter(
        Constants.APP_AUTH_CHALLENGE_INTENT_ACTION));

    userTokenBroadcastReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        fail(intent.getAction());
      }
    };
    LocalBroadcastManager.getInstance(getContext()).registerReceiver(appTokenBroadcastReceiver, new IntentFilter(
        Constants.USER_AUTH_CHALLENGE_INTENT_ACTION));
  }

  @Override
  protected void tearDown() {
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(appTokenBroadcastReceiver);
    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(userTokenBroadcastReceiver);
  }

  @MediumTest
  public void testMagnetServiceCreation() throws InterruptedException {
    MaxModule service = this;

    final CountDownLatch initCallbackSignal = new CountDownLatch(1);
    MaxCore.initModule(service, new ApiCallback<Boolean>() {
      @Override public void success(Boolean aBoolean) {
        initCallbackSignal.countDown();
      }

      @Override public void failure(ApiError error) {
        initCallbackSignal.countDown();
      }
    });
    assertEquals(this.getClass().getSimpleName(), service.getName());
    initCallbackSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, initCallbackSignal.getCount());
    assertEquals(0, onInitSignal.getCount());

    onAppTokenSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, onAppTokenSignal.getCount());
    assertEquals(1, onUserTokenSignal.getCount());

    final CountDownLatch userRegSignal = new CountDownLatch(1);

    final String userName = "test" + System.currentTimeMillis();
    final String firstName = "JimTest";
    final String lastName = "Liu";
    UserRegistrationInfo newUser = new UserRegistrationInfo.Builder().userName(userName).email(
        userName + "@magnet.com").password("password")
        .firstName(firstName).lastName(lastName).userRealm(UserRealm.DB).build();
    User.register(newUser, new ApiCallback<User>() {
      @Override public void success(User response) {
        User user = response;
        assertNotNull(user);
        Log.i(TAG, "---------user registered : " + user);
        userRegSignal.countDown();
      }

      @Override public void failure(ApiError apiError) {
        Log.e(TAG, "---------user registered failed : " + apiError.getMessage());
        //fail("newUser failed : " + retrofitError.getMessage());
      }
    });

    userRegSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, userRegSignal.getCount());

    final CountDownLatch loginSignal = new CountDownLatch(1);
    assertNull(User.getCurrentUser());
    User.login(userName, "password", false, new ApiCallback<Boolean>() {
      @Override public void success(Boolean aBoolean) {
        assertTrue(aBoolean);
        assertNotNull(User.getCurrentUser());
        assertNotNull(User.getCurrentUser().getUserName());
        assertThat(User.getCurrentUser().getUserName()).isEqualTo(userName);
        assertThat(User.getCurrentUser().getFirstName()).isEqualTo(firstName);
        assertThat(User.getCurrentUser().getLastName()).isEqualTo(lastName);
        loginSignal.countDown();
      }

      @Override public void failure(ApiError apiError) {
        fail("login failed : " + apiError.getMessage());
      }
    });

    //onInitSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    //assertEquals(0, onInitSignal.getCount());
    //onAppTokenSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    //assertEquals(0, onAppTokenSignal.getCount());
    onUserTokenSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, onUserTokenSignal.getCount());

    loginSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, loginSignal.getCount());

    final CountDownLatch updateProfileSignal = new CountDownLatch(1);
    assertNotNull(User.getCurrentUser());
    final String updatedFirstName =  "JimTestUpdated";
    User.updateProfile(new UpdateProfileRequest.Builder().firstName(updatedFirstName).build(), new ApiCallback<User>() {
      @Override public void success(User user) {
        assertNotNull(User.getCurrentUser());
        assertThat(User.getCurrentUser().getFirstName()).isEqualTo(updatedFirstName);
        updateProfileSignal.countDown();
      }

      @Override public void failure(ApiError error) {
        fail("updateProfile failed : " + error.getMessage());
      }
    });
    updateProfileSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, updateProfileSignal.getCount());

    //final CountDownLatch userSearchSignal = new CountDownLatch(1);
    //User.search("lastName=Liu", null, null, null, new ApiCallback<List<User>>() {
    //  @Override public void success(List<User> users) {
    //    userRegSignal.countDown();
    //    assertNotNull(users);
    //    assertTrue(users.size() > 0);
    //  }
    //
    //  @Override public void failure(ApiError error) {
    //    fail("user search failed : " + error.getMessage());
    //  }
    //});
    //userSearchSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    //assertEquals(0, userRegSignal.getCount());

    //AttachmentService attachmentService = MaxCore.create(AttachmentService.class);
    Attachment attachment = new Attachment(getContext().getResources().openRawResource(
        R.raw.test_image), "image/jpeg");
    //Attachment attachment2 = new FileAttachment("image/jpeg", new File("path"));
    //Attachment attachment3 = new BytesAttachment("image/jpeg", new byte[] {});
    //Attachment attachment4 = new TextAttachment("text/html", "<html></html>");
    assertNull(attachment.getAttachmentId());
    assertEquals(-1, attachment.getLength());
    final CountDownLatch uploadSignal = new CountDownLatch(1);
    attachment.upload(new Attachment.AttachmentTransferLister() {
      @Override public void onStart(Attachment attachment) {

      }

      //@Override public void onProgress(Attachment attachment, long processedBytes) {
      //
      //}

      @Override public void onComplete(Attachment attachment) {
        uploadSignal.countDown();
      }

      @Override public void onError(Attachment attachment, Throwable error) {
        fail(error.getMessage());
        uploadSignal.countDown();
      }
    });
    uploadSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, uploadSignal.getCount());
    assertNotNull(attachment.getAttachmentId());
    assertTrue(attachment.getLength() > 0);
    assertEquals(Attachment.Status.COMPLETE, attachment.getStatus());

    //final CountDownLatch downloadSignal = new CountDownLatch(1);
    //InputStreamAttachment downloadAttachment = new InputStreamAttachment(attachment.getAttachmentId());
    //assertNull(downloadAttachment.getAsBytes());
    //downloadAttachment.download(new Attachment.AttachmentTrasferLister() {
    //  @Override public void onStart(Attachment attachment) {
    //
    //  }
    //
    //  @Override public void onProgress(Attachment attachment, long processedBytes) {
    //
    //  }
    //
    //  @Override public void onComplete(Attachment attachment) {
    //    downloadSignal.countDown();
    //  }
    //
    //  @Override public void onError(Attachment attachment, Throwable error) {
    //    fail(error.getMessage());
    //    downloadSignal.countDown();
    //  }
    //});
    //downloadSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    //assertEquals(0, downloadSignal.getCount());
    //assertNotNull(downloadAttachment.getAsBytes());
    //assertEquals(attachment.getLength(), downloadAttachment.getLength());

    final CountDownLatch logoutSignal = new CountDownLatch(1);
    User.logout(new ApiCallback<Boolean>() {
      @Override public void success(Boolean aBoolean) {
        logoutSignal.countDown();
      }

      @Override public void failure(ApiError error) {

      }
    });
    logoutSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, logoutSignal.getCount());

    onUserTokenInvalidSignal.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);
    assertEquals(0, onUserTokenInvalidSignal.getCount());

    //MagnetService2 service2 = getServiceAdapter().create(MagnetService2.class);
  }

  //------------methods of MagnetService------------------
  @Override public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override public void onInit(Context context, Map<String, String> map, ApiCallback<Boolean> callback) {
    Log.i(TAG, "-----------onInit called");
    printMap(map);
    Log.i(TAG, "-----------local config");
    printMap(MaxCore.getConfig().getAllConfigs());
    if(null != callback) {
      callback.success(true);
    }
    onInitSignal.countDown();
  }

  @Override public void onAppTokenUpdate(String s, String s1, String s2) {
    Log.i(TAG, "-----------onAppTokenUpdate called : " + s2);
    onAppTokenSignal.countDown();
  }

  @Override public void onUserTokenUpdate(String s, String userId, String s2) {
    Log.i(TAG, "-----------onUserTokenUpdate called : " + s2);
    onUserTokenSignal.countDown();
  }

  @Override public void onClose(boolean b) {

  }

  @Override public void onUserTokenInvalidate() {
    Log.i(TAG, "-----------onUserTokenInvalidate called");
    onUserTokenInvalidSignal.countDown();
  }

  @Override public void deInitModule() {
    Log.i(TAG, "-----------deInitModule called");
  }

  private void printMap(Map<String, String> map) {
    Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
    while(it.hasNext()) {
      Map.Entry<String,String> entry = it.next();
      Log.i(TAG, "--" + entry.getKey() + " : " + entry.getValue());
    }
  }
}
