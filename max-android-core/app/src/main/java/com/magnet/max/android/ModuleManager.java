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
import com.magnet.max.android.auth.model.ApplicationToken;
import com.magnet.max.android.auth.model.DeviceInfo;
import com.magnet.max.android.auth.model.UserToken;
import com.magnet.max.android.util.SecurePreferences;
import com.magnet.max.android.util.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**public**/ class ModuleManager {
  private static final String TAG = ModuleManager.class.getSimpleName();

  private static Map<String, List<ModuleInfo>> registeredModules = new HashMap<>();

  private static AtomicReference<ApplicationToken> appTokenRef = new AtomicReference<ApplicationToken>(null);
  private static AtomicReference<UserToken> userTokenRef = new AtomicReference<UserToken>(null);
  private static AtomicReference<String> userIdRef = new AtomicReference<String>(null);

  private static Map<String, String> configMap;

  private static TokenLocalStore tokenLocalStore;

  public static synchronized void init() {
    Log.i(TAG, "-----------ModuleManager init");

    if(null == tokenLocalStore) {
      tokenLocalStore = new TokenLocalStore();
      tokenLocalStore.loadCredentials();
    }

    if(null == configMap) {
      configMap = new HashMap<>();
    } else {
      configMap.clear();
    }
    if(null != MaxCore.getConfig().getAllConfigs()) {
      configMap.putAll(MaxCore.getConfig().getAllConfigs());
    }

    if(null != appTokenRef.get()) {
      onAppLogout(MaxCore.getConfig().getClientId());
    }

    if(null != userTokenRef.get()) {
      onUserLogout(User.getCurrentUserId());
    }

    if(null == registeredModules) {
      registeredModules = new HashMap<String, List<ModuleInfo>>();
    } else {
      registeredModules.clear();
    }
  }

  public static synchronized void deInit() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      s.getModule().deInitModule();
    }
    registeredModules.clear();
  }

  public static synchronized void register(MaxModule module, ApiCallback<Boolean> callback) {
    if(null == module) {
      throw new IllegalArgumentException("module shouldn't be null");
    }

    Log.d(TAG, "--------register module " + module.getName() + " : " + module);

    List<ModuleInfo> existingModules = registeredModules.get(module.getName());
    ModuleInfo moduleInfo = new ModuleInfo(module, callback);
    if(null != existingModules) {
      int index = findModule(existingModules, module);
      if(index == -1) {
        existingModules.add(moduleInfo);
      } else {
        Log.w(TAG, "MaxModule " + module + " has been registered");
      }
    } else {
      ArrayList<ModuleInfo> moduleInfos = new ArrayList<>();
      moduleInfos.add(moduleInfo);
      registeredModules.put(module.getName(), moduleInfos);
    }

    if(appTokenRef.get() != null) {
      Log.d(TAG, "--------appToken is availabe when register : " + appTokenRef.get());
      module.onInit(MaxCore.getApplicationContext(), configMap, callback);
      module.onAppTokenUpdate(appTokenRef.get().getAccessToken(), appTokenRef.get().getMmxAppId(),
          Device.getCurrentDeviceId());
    }
    if(userTokenRef.get() != null) {
      Log.d(TAG, "--------userToken is availabe when register : " + userTokenRef.get());
      module.onUserTokenUpdate(userTokenRef.get().getAccessToken(), userIdRef.get(),
          Device.getCurrentDeviceId());
    }
  }

  public static synchronized void deRegister(MaxModule module, ApiCallback<Boolean> callback) {
    if(null == module) {
      throw new IllegalArgumentException("module shouldn't be null");
    }

    Log.d(TAG, "--------deRegister module " + module.getName() + " : " + module);

    List<ModuleInfo> existingModules = registeredModules.get(module.getName());
    int existingIndex = findModule(existingModules, module);
    if(existingIndex >= 0) {
      existingModules.remove(existingIndex);
      module.deInitModule();
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
      appTokenRef.set(appToken);

      notifyAppTokenObservers();

      tokenLocalStore.saveAppToken();
    }

    // Update config
    if(null != serverConfig) {
      configMap.clear();
      if(null != MaxCore.getConfig().getAllConfigs()) {
        configMap.putAll(MaxCore.getConfig().getAllConfigs());
      }
      configMap.putAll(serverConfig);
    }

    notifyConfigObservers();
  }

  public static void onAppLogout(final String appId) {
    Log.i(TAG, "onAppLogout  : ");

    //if(null != appTokenRef.get()) {
      appTokenRef.set(null);

      notifyAppTokenObservers();

      tokenLocalStore.saveAppToken();
    //}
  }

  public static void onUserLogin(final String userId, UserToken token, boolean rememberMe) {
    if(null != token) {
      Log.i(TAG, "userLogin success : ");

      userTokenRef.set(token);

      userIdRef.set(userId);

      notifyUserTokenObservers();

      registerDevice();

      if(rememberMe) {
        tokenLocalStore.saveUserToken();
      }

      tokenLocalStore.saveRememberMe(rememberMe);
    } else {

    }
  }

  public static void onUserLogout(final String userId) {
    Log.i(TAG, "onUserLogout  : ");

    userTokenRef.set(null);

    userIdRef.set(null);

    notifyInvalidUserTokenObservers();

    tokenLocalStore.saveUserToken();
  }

  public static void onTokenInvalid(String token) {
    if (null != userTokenRef.get() && StringUtil.isStringValueEqual(token, userTokenRef.get().getAccessToken())) {
      Log.w(TAG, "Auth failed, it's user token");
      onUserTokenInvalid();
    } else if(null != appTokenRef.get() && StringUtil.isStringValueEqual(token, appTokenRef.get().getAccessToken())) {
      Log.w(TAG, "Auth failed, it's app token");
      onAppTokenInvalid();
    } else {
      Log.w(TAG, "Auth failed, token doesn't match current user or app token");
    }
  }

  public static void onUserTokenInvalid() {
    Log.w(TAG, "User token is invalid, notify modules");
    User.logout(null);
    notifyAuthFailure(Constants.USER_AUTH_CHALLENGE_INTENT_ACTION);
  }

  public static void onAppTokenInvalid() {
    notifyAuthFailure(Constants.APP_AUTH_CHALLENGE_INTENT_ACTION);
  }

  public static ApplicationToken getApplicationToken() {
    return appTokenRef.get();
  }

  /**
   * Send local broadcast for authentication challenge
   * @param action
   */
  private static void notifyAuthFailure(String action) {
    LocalBroadcastManager.getInstance(MaxCore.getApplicationContext()).sendBroadcast(new Intent(action));
  }

  private static List<ModuleInfo> getAllRegisteredModules() {
    List<ModuleInfo> result = new ArrayList<>();
    for(Map.Entry<String, List<ModuleInfo>> e : registeredModules.entrySet()) {
      List<ModuleInfo> services = e.getValue();
      if(null != services) {
        result.addAll(services);
      }
    }

    return result;
  }

  private static void notifyConfigObservers() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      Log.i(TAG, "notify onConfig for : " + s.getModule().getName());
      s.getModule().onInit(MaxCore.getApplicationContext(), configMap, s.getCallback());
    }
  }

  private static void notifyAppTokenObservers() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      Log.i(TAG, "notify onAppTokenUpdate for : " + s.getModule().getName());
      s.getModule().onAppTokenUpdate(
          null != appTokenRef.get() ? appTokenRef.get().getAccessToken() : null,
          MaxCore.getConfig().getClientId(), Device.getCurrentDeviceId());
    }
  }

  private static void notifyUserTokenObservers() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      Log.i(TAG, "notify onUserTokenUpdate for : " + s.getModule().getName());
      s.getModule()
          .onUserTokenUpdate(
              null != userTokenRef.get() ? userTokenRef.get().getAccessToken() : null,
              userIdRef.get(), Device.getCurrentDeviceId());
    }
  }

  private static void notifyInvalidUserTokenObservers() {
    for(ModuleInfo s : getAllRegisteredModules()) {
      Log.i(TAG, "notify notifyInvalidUserToken for : " + s.getModule().getName());
      s.getModule().onUserTokenInvalidate();
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

  private static class ModuleInfo {
    private MaxModule module;
    private ApiCallback callback;

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
  }

  private final static class TokenLocalStore {
    private final SharedPreferences credentialStore;
    private final Gson gson;

    public TokenLocalStore() {
      credentialStore = new SecurePreferences(MaxCore.getApplicationContext());
      gson = new Gson();
    }

    public void saveAppToken() {
      SharedPreferences.Editor editor = credentialStore.edit();
      if(null == appTokenRef.get()) {
        editor.remove("appToken");
      } else {
        //editor.putString("appToken", encryptor.encryptString(gson.toJson(appTokenRef.get())));
        editor.putString("appToken", gson.toJson(appTokenRef.get()));
      }
      editor.commit();

      //Log.d(TAG, "-------------updating userName = " + userIdRef.get());
      //Log.d(TAG, "-------------updating appToken = " + appTokenRef.get().getAccessToken());
    }

    public void saveUserToken() {
      SharedPreferences.Editor editor = credentialStore.edit();
      if(null == userIdRef.get()) {
        editor.remove("userId");
      } else {
        //editor.putString("userId", encryptor.encryptString(userIdRef.get()));
        editor.putString("userId", userIdRef.get());
      }

      if(null == userTokenRef.get()) {
        editor.remove("userToken");
      } else {
        //editor.putString("userToken", encryptor.encryptString(gson.toJson(userTokenRef.get())));
        editor.putString("userToken", gson.toJson(userTokenRef.get()));
      }
      editor.commit();

      //Log.d(TAG, "-------------updating userName = " + userIdRef.get());
      //Log.d(TAG, "-------------updating userToken = " + userTokenRef.get().getAccessToken());
    }

    public void saveRememberMe(boolean rememberMe) {
      SharedPreferences.Editor editor = credentialStore.edit();
      editor.putBoolean("rememberMe", rememberMe);
      editor.commit();

      //Log.d(TAG, "-------------updating userName = " + userIdRef.get());
      //Log.d(TAG, "-------------updating userToken = " + userTokenRef.get().getAccessToken());
    }

    public void loadCredentials() {
      if(credentialStore.getBoolean("rememberMe", false)) {
        userIdRef.set(credentialStore.getString("userId", null));
        Log.d(TAG, "-------------credentials reloaded from local, userName = " + userIdRef.get());

        String appTokenJson = credentialStore.getString("appToken", null);
        if (null != appTokenJson) {
          appTokenRef.set(gson.fromJson(appTokenJson, ApplicationToken.class));

          //Log.d(TAG, "-------------credentials reloaded from local appToken = " + appTokenRef.get()
          //    .getAccessToken());

          notifyAppTokenObservers();
        }

        String userTokenJson = credentialStore.getString("userToken", null);
        if (null != userTokenJson) {
          userTokenRef.set(gson.fromJson(userTokenJson, UserToken.class));

          //Log.d(TAG,
          //    "-------------credentials reloaded from local userToken = " + userTokenRef.get()
          //        .getAccessToken());

          notifyUserTokenObservers();
        }
      }
    }
  }

  private static int findModule(List<ModuleInfo> existingModules, MaxModule module) {
    if(null != existingModules) {
      for (int i = 0; i < existingModules.size(); i++) {
        if (existingModules.get(i).getModule() == module) {
          return i;
        }
      }
    }

    return -1;
  }
}
