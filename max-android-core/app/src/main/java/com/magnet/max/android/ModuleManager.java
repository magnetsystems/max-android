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

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.magnet.max.android.auth.model.ApplicationToken;
import com.magnet.max.android.auth.model.BaseToken;
import com.magnet.max.android.auth.model.DeviceInfo;
import com.magnet.max.android.auth.model.UserToken;
import com.magnet.max.android.util.EqualityUtil;
import com.magnet.max.android.util.HashCodeBuilder;
import com.magnet.max.android.util.MagnetUtils;
import com.magnet.max.android.util.SecurePreferences;
import com.magnet.max.android.util.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**public**/ class ModuleManager {
  private static final String TAG = ModuleManager.class.getSimpleName();

  private static Map<String, Set<ModuleInfo>> mRegisteredModules = new HashMap<>();

  private volatile static AtomicReference<ApplicationToken> mAppTokenRef = new AtomicReference<ApplicationToken>(null);
  private volatile static AtomicReference<UserToken> mUserTokenRef = new AtomicReference<UserToken>(null);
  private volatile static AtomicReference<String> mUserIdRef = new AtomicReference<String>(null);
  private volatile static AtomicReference<User> mCachedUserRef = new AtomicReference<User>(null);
  private volatile static AtomicBoolean mToRememberMeRef = new AtomicBoolean(false);
  private volatile static AtomicReference<Map<String, String>> mServerConfigsRef = new AtomicReference<>(null);
  private static Map<String, String> mCachedServerConfig = new HashMap<>();

  private static TokenLocalStore mTokenLocalStore;

  public static synchronized void init() {
    Log.i(TAG, "-----------ModuleManager init");

    if(null == mTokenLocalStore) {
      mTokenLocalStore = new TokenLocalStore();
      mTokenLocalStore.loadCredentials();
    }

    if(null == mServerConfigsRef.get()) {
      mServerConfigsRef.set(MaxCore.getConfig().getAllConfigs());
    } else {
      mServerConfigsRef.get().putAll(MaxCore.getConfig().getAllConfigs());
    }

    //if(null != mAppTokenRef.get()) {
    //  onAppLogout(MaxCore.getConfig().getClientId());
    //}
    //
    //if(null != mUserTokenRef.get()) {
    //  onUserLogout(User.getCurrentUserId());
    //}

    if(null == mRegisteredModules) {
      mRegisteredModules = new HashMap<>();
    } else {
      mRegisteredModules.clear();
    }
  }

  public static synchronized void deInit() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      s.getModule().deInitModule(null);
    }
    mRegisteredModules.clear();
  }

  public static synchronized void register(MaxModule module, ApiCallback<Boolean> callback) {
    if(null == module) {
      throw new IllegalArgumentException("module shouldn't be null");
    }

    Log.d(TAG, "--------registering module " + module.getName() + " : " + module + "\n" + getAllRegisteredModules());

    boolean registered = true;
    Set<ModuleInfo> existingModules = mRegisteredModules.get(module.getName());
    ModuleInfo moduleInfo = new ModuleInfo(module, callback);
    if(null != existingModules) {
      if(!existingModules.contains(module)) {
        existingModules.add(moduleInfo);
      } else {
        Log.w(TAG, "MaxModule " + module + " has been registered");
        registered = false;
      }
    } else {
      Set<ModuleInfo> moduleInfos = new HashSet<>();
      moduleInfos.add(moduleInfo);
      mRegisteredModules.put(module.getName(), moduleInfos);
    }

    if(registered) {
      if (mAppTokenRef.get() != null) {
        Log.d(TAG, "--------appToken is available when register : " + mAppTokenRef.get());
        Log.d(TAG, "--------configs is available when register : " + Arrays.toString(
            mServerConfigsRef.get().entrySet().toArray()));
        module.onInit(MaxCore.getApplicationContext(), mServerConfigsRef.get(), callback);
        module.onAppTokenUpdate(mAppTokenRef.get().getAccessToken(), mAppTokenRef.get().getMmxAppId(),
            Device.getCurrentDeviceId(), callback);
      }
      if (mUserTokenRef.get() != null && (!mToRememberMeRef.get()) || (mToRememberMeRef.get() && null != User.getCurrentUser())) {
        Log.d(TAG, "--------userToken is availabe when register : " + mUserTokenRef.get());
        module.onUserTokenUpdate(mUserTokenRef.get().getAccessToken(), mUserIdRef.get(), Device.getCurrentDeviceId(), callback);
      }
    }
  }

  public static synchronized void deRegister(MaxModule module, ApiCallback<Boolean> callback) {
    if(null == module) {
      throw new IllegalArgumentException("module shouldn't be null");
    }

    Log.d(TAG, "--------deRegister module " + module.getName() + " : " + module);

    Set<ModuleInfo> existingModules = mRegisteredModules.get(module.getName());
    if(null != existingModules && existingModules.contains(module)) {
      existingModules.remove(module);
      module.deInitModule(null);
      Log.d(TAG, "deinit and remove module " + module.getName());

      if(null != callback) {
        callback.success(true);
      }

      return;
    }

    if(null != callback) {
      callback.failure(new ApiError("Module is not registered"));
    }
    Log.w(TAG, "Module " + module.getName() + " is not found when deinit and remove module ");
  }

  public static void onAppLogin(String appId, ApplicationToken appToken,
      Map<String, String> serverConfig) {
    if(null != appToken) {
      //boolean isSameToken = isTokenSame(null, mAppTokenRef.get(), null, appToken);
      //mAppTokenRef.set(appToken);
      //if(!isSameToken) {
      //  notifyAppTokenObservers();
      //} else {
      //  Log.d(TAG, "App token is the same, won't notifyAppTokenObservers");
      //}
      //
      //mTokenLocalStore.updateAppToken();
      mAppTokenRef.set(appToken);

      notifyAppTokenObservers();

      mTokenLocalStore.updateAppToken();
    }

    onServerConfig(serverConfig);
  }

  public static void onAppLogout(final String appId) {
    Log.i(TAG, "onAppLogout  : ");

    //if(null != mAppTokenRef.get()) {
      mAppTokenRef.set(null);

      mServerConfigsRef.set(new HashMap<String, String>());

      notifyAppTokenObservers();

      mTokenLocalStore.updateAppToken();
      mTokenLocalStore.updateServerConfigs();
    //}
  }

  public static boolean onUserLogin(final String userId, UserToken token, boolean rememberMe, ApiCallback<Boolean> callback) {
    boolean isCallbackCalled = false;
    if(null != token) {
      Log.i(TAG, "userLogin success : ");

      mToRememberMeRef.set(rememberMe);

      mUserTokenRef.set(token);

      mUserIdRef.set(userId);

      isCallbackCalled = notifyUserTokenObservers(callback);

      mTokenLocalStore.updateForUserLogin();

      registerDevice();
    }

    return isCallbackCalled;
  }

  public static boolean onUserSessioinResume(ApiCallback<Boolean> callback) {
    boolean isCallbackCalled = false;

    Log.i(TAG, "onUserSessioinResume : ");

    isCallbackCalled = notifyUserTokenObservers(callback);

    return isCallbackCalled;
  }

  public static void onUserTokenRefresh(final String userId, UserToken token, ApiCallback<Boolean> callback) {
    if(null != token) {
      Log.i(TAG, "refresh user token success : ");

      mUserTokenRef.set(token);

      mUserIdRef.set(userId);

      notifyUserTokenObservers(callback);

      if(mToRememberMeRef.get()) {
        mTokenLocalStore.updateUserToken();
      }
    } else {

    }
  }

  public static void onUserLogout(final String userId) {
    Log.i(TAG, "onUserLogout  : ");

    mUserTokenRef.set(null);

    mUserIdRef.set(null);

    notifyInvalidUserTokenObservers();

    mTokenLocalStore.updateForUserLogout();

    mToRememberMeRef.set(false);
  }

  public static void onTokenInvalid(String token) {
    if (null != mUserTokenRef.get() && StringUtil.isStringValueEqual(token, mUserTokenRef.get().getAccessToken())) {
      Log.w(TAG, "Auth failed, it's user token");
      onUserTokenInvalid();
    } else if(null != mAppTokenRef.get() && StringUtil.isStringValueEqual(token, mAppTokenRef.get().getAccessToken())) {
      Log.w(TAG, "Auth failed, it's app token");
      onAppTokenInvalid();
    } else {
      Log.w(TAG, "Auth failed, token doesn't match current user or app token");
    }
  }

  public static void onUserTokenInvalid() {
    Log.w(TAG, "User token is invalid, notify modules");
    // Clean up token first so User.logout won't call server APIs
    ModuleManager.onUserLogout(User.getCurrentUserId());
    User.logout(null);
    notifyAuthFailure(Constants.USER_AUTH_CHALLENGE_INTENT_ACTION);
  }

  public static void onAppTokenInvalid() {
    notifyAuthFailure(Constants.APP_AUTH_CHALLENGE_INTENT_ACTION);
  }

  private static void onServerConfig(Map<String, String> serverConfigs) {
    if(!MagnetUtils.isMapContainedIn(serverConfigs, mServerConfigsRef.get())) {
      refreshServerConfigs(serverConfigs);
      mTokenLocalStore.updateServerConfigs();
    }
    notifyConfigObservers();
  }

  public static ApplicationToken getApplicationToken() {
    return mAppTokenRef.get();
  }

  public static UserToken getUserToken() {
    return mUserTokenRef.get();
  }

  public static String getUserId() {
    return mUserIdRef.get();
  }

  public static Map<String, String> getServerConfigs() {
    return mServerConfigsRef.get();
  }

  public static Map<String, String> getCachedServerConfigs() {
    return mCachedServerConfig;
  }

  public static User getCachedUser() {
    return mCachedUserRef.get();
  }

  /**
   * Send local broadcast for authentication challenge
   * @param action
   */
  private static void notifyAuthFailure(String action) {
    Log.d(TAG, "No broadcast receiver registered for : " + action + ", throwing a runtime exception");
    StringBuilder messageBuilder = new StringBuilder("\n-------------------------------------------------------------------\n");
    if(Constants.USER_AUTH_CHALLENGE_INTENT_ACTION.equals(action)) {
      messageBuilder.append("User token is invalid, please re-login");
    } else {
      messageBuilder.append("Application login failed, please check network status, clientId/secret or update your app");
    }
    messageBuilder.append("\n-------------------------------------------------------------------\n");
    assert false :  messageBuilder.toString();

    LocalBroadcastManager.getInstance(MaxCore.getApplicationContext())
        .sendBroadcast(new Intent(action));
  }

  private static List<ModuleInfo> getAllRegisteredModules() {
    List<ModuleInfo> result = new ArrayList<>();
    for(Map.Entry<String, Set<ModuleInfo>> e : mRegisteredModules.entrySet()) {
      Set<ModuleInfo> services = e.getValue();
      if(null != services) {
        result.addAll(services);
      }
    }

    Log.d(TAG, "Registered modules : " + Arrays.toString(result.toArray()));

    return result;
  }

  private static void notifyConfigObservers() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      Log.i(TAG, "notify onConfig for : " + s.getModule());
      s.getModule().onInit(MaxCore.getApplicationContext(), mServerConfigsRef.get(), s.getCallback());
    }
  }

  private static void notifyAppTokenObservers() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      Log.i(TAG, "notify onAppTokenUpdate for : " + s.getModule());
      s.getModule().onAppTokenUpdate(
          null != mAppTokenRef.get() ? mAppTokenRef.get().getAccessToken() : null,
          MaxCore.getConfig().getClientId(), Device.getCurrentDeviceId(), null);
    }
  }

  private static boolean notifyUserTokenObservers(ApiCallback<Boolean> callback) {
    boolean isCallbackCalled = false;
    for(ModuleInfo s : getAllRegisteredModules()) {
      Log.i(TAG, "notify onUserTokenUpdate for : " + s.getModule());
      s.getModule()
          .onUserTokenUpdate(
              null != mUserTokenRef.get() ? mUserTokenRef.get().getAccessToken() : null,
              mUserIdRef.get(), Device.getCurrentDeviceId(), callback);
      //FIXME : assume callback is called here
      isCallbackCalled = true;
    }

    return isCallbackCalled;
  }

  private static void notifyInvalidUserTokenObservers() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      Log.i(TAG, "notify notifyInvalidUserToken for : " + s.getModule().getName());
      s.getModule().onUserTokenInvalidate(null);
    }
  }

  private static void registerDevice() {
    Device.register(new DeviceInfo.Builder().build(), new ApiCallback<Device>() {
      @Override public void success(Device response) {
        Log.i(TAG, "registerDevice success : ");
      }

      @Override public void failure(ApiError error) {
        Log.e(TAG, "registerDevice error : " + error.getMessage());
      }
    });
  }

  private static void refreshServerConfigs(Map<String, String> newConfigs) {
    if(null != newConfigs && !newConfigs.isEmpty()) {
      //mServerConfigsRef.get().clear();
      if(null != MaxCore.getConfig().getAllConfigs()) {
        mServerConfigsRef.get().putAll(MaxCore.getConfig().getAllConfigs());
      }
      if(null != mAppTokenRef.get()) {
        mServerConfigsRef.get().put("mmx-appId", mAppTokenRef.get().getMmxAppId());
      }
      mServerConfigsRef.get().putAll(newConfigs);
    }
  }

  private static boolean isTokenSame(String oldId, BaseToken oldToken, String newId, BaseToken newToken) {
    return !StringUtil.isStringValueEqual(oldId, newId) && isTokenEquals(oldToken, newToken);
  }

  private static boolean isTokenEquals(BaseToken token1, BaseToken token2) {
    if(null == token1) {
      return null == token2;
    }

    if(null == token2) {
      return null == token1;
    }

    return StringUtil.isStringValueEqual(token1.getAccessToken(), token2.getAccessToken());
  }

  private static class ModuleInfo {
    private final MaxModule module;
    private final ApiCallback callback;

    public ModuleInfo(MaxModule module, ApiCallback callback) {
      this.module = module;
      this.callback = callback;
    }

    public MaxModule getModule() {
      return module;
    }

    public ApiCallback getCallback() {
      return callback;
    }

    @Override public String toString() {
      return new StringBuilder().append("{ module = ").append(module).append(", callback = ").append(callback).append("}").toString();
    }

    @Override public boolean equals(Object obj) {
      if(!EqualityUtil.quickCheck(this, obj)) {
        return false;
      }

      ModuleInfo theOther = (ModuleInfo) obj;
      return module.equals(theOther.getModule());
    }

    @Override public int hashCode() {
      return new HashCodeBuilder().hash(module).hashCode();
    }
  }

  private final static class TokenLocalStore {
    public static final String KEY_APP_TOKEN = "appToken";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_TOKEN = "userToken";
    public static final String KEY_USER = "user";
    public static final String KEY_SERVER_CONFIGS = "serverConfigs";
    public static final String KEY_REMEMBER_ME = "rememberMe";

    private final SharedPreferences credentialStore;
    private final Gson gson;

    public TokenLocalStore() {
      credentialStore = new SecurePreferences(MaxCore.getApplicationContext());
      gson = new Gson();
    }

    public void updateForUserLogin() {
      update(null,
          mToRememberMeRef.get() ? new AtomicReference<String>(mUserIdRef.get()) : null,
          mToRememberMeRef.get() ? new AtomicReference<UserToken>(mUserTokenRef.get()) : null,
          mToRememberMeRef.get() ? new AtomicReference<User>(User.getCurrentUser()) : null,
          new AtomicReference<Boolean>(mToRememberMeRef.get()),
          null);
    }

    public void updateForUserLogout() {
      update(null,
          mToRememberMeRef.get() ? new AtomicReference<String>(null) : null,
          mToRememberMeRef.get() ? new AtomicReference<UserToken>(null) : null,
          mToRememberMeRef.get() ? new AtomicReference<User>(null) : null,
          new AtomicReference<Boolean>(null),
          null);
    }

    public void updateAppToken() {
      SharedPreferences.Editor editor = credentialStore.edit();
      if(null == mAppTokenRef.get()) {
        editor.remove(KEY_APP_TOKEN);
      } else {
        //editor.putString("appToken", encryptor.encryptString(gson.toJson(mAppTokenRef.get())));
        editor.putString(KEY_APP_TOKEN, gson.toJson(mAppTokenRef.get()));
      }
      editor.apply();

      //Log.d(TAG, "-------------updating userName = " + mUserIdRef.get());
      //Log.d(TAG, "-------------updating appToken = " + mAppTokenRef.get().getAccessToken());
    }

    public void updateUserToken() {
      SharedPreferences.Editor editor = credentialStore.edit();
      if(null == mUserTokenRef.get()) {
        editor.remove(KEY_USER_TOKEN);
      } else {
        //editor.putString("userToken", encryptor.encryptString(gson.toJson(mUserTokenRef.get())));
        editor.putString(KEY_USER_TOKEN, gson.toJson(mUserTokenRef.get()));
      }
      editor.apply();

      //Log.d(TAG, "-------------updating userName = " + mUserIdRef.get());
      //Log.d(TAG, "-------------updating userToken = " + mUserTokenRef.get().getAccessToken());
    }

    public void updateServerConfigs() {
      SharedPreferences.Editor editor = credentialStore.edit();
      if(null != mServerConfigsRef.get() && !mServerConfigsRef.get().isEmpty()) {
        editor.putString(KEY_SERVER_CONFIGS, gson.toJson(mServerConfigsRef.get()));
      } else {
        editor.remove(KEY_SERVER_CONFIGS);
      }
      editor.apply();
    }

    private void update(AtomicReference<ApplicationToken> applicationTokenRef,
        AtomicReference<String> userIdRef, AtomicReference<UserToken> userTokenRef, AtomicReference<User> userRef,
        AtomicReference<Boolean> rememberMeRef, AtomicReference<Map<String, String>> configsRef) {
      SharedPreferences.Editor editor = credentialStore.edit();
      if(null != applicationTokenRef) {
        if (null == applicationTokenRef.get()) {
          editor.remove(KEY_APP_TOKEN);
        } else {
          //editor.putString("appToken", encryptor.encryptString(gson.toJson(mAppTokenRef.get())));
          editor.putString(KEY_APP_TOKEN, gson.toJson(applicationTokenRef.get()));
        }
      }

      if(null != userIdRef) {
        if (null == userIdRef.get()) {
          editor.remove(KEY_USER_ID);
        } else {
          editor.putString(KEY_USER_ID, userIdRef.get());
        }
      }

      if(null != userTokenRef) {
        if (null == userTokenRef.get()) {
          editor.remove(KEY_USER_TOKEN);
        } else {
          //editor.putString("userToken", encryptor.encryptString(gson.toJson(mUserTokenRef.get())));
          editor.putString(KEY_USER_TOKEN, gson.toJson(userTokenRef.get()));
        }
      }

      if(null != userRef) {
        if (null != userRef.get()) {
          editor.putString(KEY_USER, gson.toJson(userRef.get()));
        } else {
          editor.remove(KEY_USER);
        }
      }

      if(null != rememberMeRef) {
        if(null != rememberMeRef.get()) {
          editor.putBoolean(KEY_REMEMBER_ME, rememberMeRef.get());
        } else {
          editor.remove(KEY_REMEMBER_ME);
        }
      }

      if(null != configsRef) {
        if(null != configsRef.get() && !configsRef.get().isEmpty()) {
          editor.putString(KEY_SERVER_CONFIGS, gson.toJson(configsRef.get()));
        } else {
          editor.remove(KEY_SERVER_CONFIGS);
        }
      }

      editor.apply();
    }

    public void loadCredentials() {
      Log.d(TAG, "-------------Loading from local cache------------- ");
      String appTokenJson = credentialStore.getString(KEY_APP_TOKEN, null);
      if (null != appTokenJson) {
        ApplicationToken applicationToken = gson.fromJson(appTokenJson, ApplicationToken.class);
        if(!applicationToken.isExpired()) {
          mAppTokenRef.set(applicationToken);

          Log.d(TAG, "-------------app token reloaded from local ");

          //notifyAppTokenObservers();
        } else {
          Log.d(TAG, "Cached app token expired");
        }
      } else {
        Log.d(TAG, "-------------app token couldn't be reloaded from local ");
      }

      String serverConfigsJson = credentialStore.getString(KEY_SERVER_CONFIGS, null);
      if(null != serverConfigsJson) {
        mServerConfigsRef.set((Map<String, String>) gson.fromJson(serverConfigsJson, new TypeToken<Map<String, String>>(){}.getType()));
        mCachedServerConfig.putAll(mServerConfigsRef.get());

        Log.d(TAG, "-------------server config reloaded from local : " + mServerConfigsRef.get());
      } else {
        Log.d(TAG, "-------------server config couldn't be reloaded from local");
      }

      boolean toRememberMe = credentialStore.getBoolean(KEY_REMEMBER_ME, false);
      if(toRememberMe) {
        mUserIdRef.set(credentialStore.getString(KEY_USER_ID, null));
        Log.d(TAG, "-------------rememberMe enabled, credentials reloaded from local, userName = " + mUserIdRef
            .get());
        String userTokenJson = credentialStore.getString(KEY_USER_TOKEN, null);
        if (null != userTokenJson) {
          UserToken cachedUserToken = gson.fromJson(userTokenJson, UserToken.class);
          mUserTokenRef.set(cachedUserToken);

          //Log.d(TAG,
          //    "-------------credentials reloaded from local userToken = " + mUserTokenRef.get()
          //        .getAccessToken());

          //Call it in resumeSession
          //notifyUserTokenObservers(null);
        }

        String userJson = credentialStore.getString(KEY_USER, null);
        if (null != userJson) {
          Log.d(TAG, "CurrentUser loaded from local cache");
          //User.setCurrentUser();
          mCachedUserRef.set(gson.fromJson(userJson, User.class));
        } else {
          Log.d(TAG, "CurrentUser couldn't be loaded from local cache");
        }
      } else {
        Log.d(TAG, "-------------rememberMe disabled");
      }
      mToRememberMeRef.set(toRememberMe);
    }
  }

}
