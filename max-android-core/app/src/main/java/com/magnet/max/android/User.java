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

import android.util.Log;
import com.google.gson.annotations.SerializedName;
import com.magnet.max.android.auth.model.UserLoginResponse;
import com.magnet.max.android.auth.model.UserRealm;
import com.magnet.max.android.auth.model.UserRegistrationInfo;
import com.magnet.max.android.auth.model.UserStatus;
import com.magnet.max.android.auth.model.UserToken;
import com.magnet.max.android.util.AuthUtil;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import retrofit.Callback;
import retrofit.MagnetCall;
import retrofit.Response;

/**
 * The User class is a local representation of a user in the MagnetMax platform.
 * This class provides various user specific methods, like authentication, signing up, and search.
 */
final public class User {
  private static final String TAG = "User";

  @SerializedName("userIdentifier")
  private String mUserIdentifier;
  @SerializedName("email")
  private String mEmail;
  @SerializedName("roles")
  private String[] mRoles;
  @SerializedName("userStatus")
  private UserStatus mUserStatus;
  @SerializedName("userName")
  private String mUserName;
  @SerializedName("userRealm")
  private UserRealm mUserRealm;
  @SerializedName("firstName")
  private String mFirstName;
  @SerializedName("lastName")
  private String mLastName;
  @SerializedName("tags")
  private String[] mTags;
  @SerializedName("userAccountData")
  private java.util.Map<String, String> mUserAccountData;

  private static AtomicReference<User> sCurrentUserRef = new AtomicReference<>();

  private static UserService sUserService;

  /**
   * Register a new user
   * @param userRegistrationInfo
   * @param callback
   */
  public static void register(final UserRegistrationInfo userRegistrationInfo, final ApiCallback<User> callback) {
    getUserService().register(userRegistrationInfo, new Callback<User>() {
      @Override public void onResponse(Response<User> response) {
        if (null != callback) {
          if (response.isSuccess()) {
            callback.success(response.body());
          } else {
            callback.failure(new ApiError(response.message(), response.code()));
          }
        }
      }

      @Override public void onFailure(Throwable throwable) {
        Log.e(TAG, "user register error : " + throwable.getMessage());
        if (null != callback) {
          callback.failure(new ApiError(throwable.getMessage()));
        }
      }
    }).executeInBackground();
  }

  /**
   * User login
   * @param userName
   * @param password
   * @param rememberMe
   * @param callback
   */
  public static void login(final String userName, final String password, final boolean rememberMe, final ApiCallback<Boolean> callback) {
    MagnetCall<UserLoginResponse> call = getUserService().userLogin("password", userName, password,
        MaxCore.getConfig().getClientId(), null, rememberMe, Device.getCurrentDeviceId(),
        AuthUtil.generateBasicAuthToken(userName, password),
        new retrofit.Callback<UserLoginResponse>() {
          @Override public void onResponse(retrofit.Response<UserLoginResponse> response) {
            Log.i(TAG, "userLogin success : ");
            UserLoginResponse userLoginResponse = response.body();
            boolean result = false;

            if (null != userLoginResponse.getUser()) {
              sCurrentUserRef.set(userLoginResponse.getUser());
            }

            if (null != userLoginResponse.getAccessToken()) {
              ModuleManager.onUserLogin(getCurrentUserId(),
                  new UserToken(userLoginResponse.getExpiresIn(),
                      userLoginResponse.getAccessToken(), userLoginResponse.getTokenType()),
                  rememberMe);

              result = true;
            }

            if (null != callback) {
              if (response.isSuccess()) {
                callback.success(result);
              } else {
                callback.failure(new ApiError(response.message(), response.code()));
              }
            }
          }

          @Override public void onFailure(Throwable throwable) {
            Log.e(TAG, "userLogin error : " + throwable.getMessage());
            if (null != callback) {
              callback.failure(new ApiError(throwable.getMessage()));
            }
          }
        });

    call.executeInBackground();
  }

  /**
   * User logout
   * @param callback
   */
  public static void logout(final ApiCallback<Boolean> callback) {
    final String currentUserId = getCurrentUserId();
    getUserService().userLogout(new Callback<Boolean>() {
      @Override public void onResponse(Response<Boolean> response) {
        boolean logoutResult = response.body();
        if (logoutResult) {
          ModuleManager.onUserLogout(currentUserId);
        }

        // Unregister device
        Device.unRegister(null);

        if (null != callback) {
          if (response.isSuccess()) {
            callback.success(logoutResult);
          } else {
            callback.failure(new ApiError(response.message(), response.code()));
          }
        }
      }

      @Override public void onFailure(Throwable throwable) {
        Log.e(TAG, "user logout error : " + throwable.getMessage());
        if (null != callback) {
          callback.failure(new ApiError(throwable.getMessage()));
        }
      }
    }).executeInBackground();

    sCurrentUserRef.set(null);
  }

  /**
   * Search users
   * @param query see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/1.4/query-dsl-query-string-query.html#query-string-syntax">Elastic search query string syntax</a>
   * @param offset
   * @param size
   * @param sort
   * @param callback
   */
  public static void search(String query, Integer offset, Integer size, String sort, final ApiCallback<List<User>> callback) {
    getUserService().searchUsers(query, size, offset, sort, new Callback<List<User>>() {
      @Override public void onResponse(Response<List<User>> response) {
        if (null != callback) {
          if (response.isSuccess()) {
            callback.success(response.body());
          } else {
            callback.failure(new ApiError(response.message(), response.code()));
          }
        }
      }

      @Override public void onFailure(Throwable throwable) {
        if (null != callback) {
          callback.failure(new ApiError(throwable));
        }
      }
    }).executeInBackground();
  }

  /**
   * Get list of User by user names
   * @param userNames
   * @param callback
   */
  public static void getUsersByUserNames(List<String> userNames, final ApiCallback<List<User>> callback) {
    getUserService().getUsersByUserNames(userNames, new Callback<List<User>>() {
      @Override public void onResponse(Response<List<User>> response) {
        if (null != callback) {
          if (response.isSuccess()) {
            callback.success(response.body());
          } else {
            callback.failure(new ApiError(response.message(), response.code()));
          }
        }
      }

      @Override public void onFailure(Throwable throwable) {
        if (null != callback) {
          callback.failure(new ApiError(throwable));
        }
      }
    }).executeInBackground();
  }

  /**
   * Get list of User by ids
   * @param userIds
   * @param callback
   */
  public static void getUsersByUserIds(List<String> userIds, final ApiCallback<List<User>> callback) {
    getUserService().getUsersByUserIds(userIds, new Callback<List<User>>() {
      @Override public void onResponse(Response<List<User>> response) {
        if (null != callback) {
          if (response.isSuccess()) {
            callback.success(response.body());
          } else {
            callback.failure(new ApiError(response.message(), response.code()));
          }
        }
      }

      @Override public void onFailure(Throwable throwable) {
        if (null != callback) {
          callback.failure(new ApiError(throwable));
        }
      }
    }).executeInBackground();
  }

  /**
   * Get User who currently login
   * @return
   */
  public static User getCurrentUser() {
    return sCurrentUserRef.get();
  }

  /**
   * Get the id of currently login user
   * @return
   */
  public static String getCurrentUserId() {
    return null != sCurrentUserRef.get() ? sCurrentUserRef.get().getUserIdentifier() : null;
  }

  /**
   * The unique identifer for the user.
   */
  public String getUserIdentifier() {
    return mUserIdentifier;
  }

  /**
   * The email for the user.
   */
  public String getEmail() {
    return mEmail;
  }

  /**
   * The roles assigned to the user.
   */
  public String[] getRoles() {
    return mRoles;
  }

  /**
   * The status {@link UserStatus} for the user.
   */
  public UserStatus getUserStatus() {
    return mUserStatus;
  }

  /**
   * The username for the user.
   */
  public String getUserName() {
    return mUserName;
  }

  /**
   * The realm {@link UserRealm} for the user.
   */
  public UserRealm getUserRealm() {
    return mUserRealm;
  }

  /**
   * The firstName for the user.
   */
  public String getFirstName() {
    return mFirstName;
  }

  /**
   * The lastName for the user.
   */
  public String getLastName() {
    return mLastName;
  }

  /**
   * The tags associated with the user.
   */
  public String[] getTags() {
    return mTags;
  }

  /**
   * The additional key-value pairs associated with the user.
   */
  public Map<String, String> getUserAccountData() {
    return mUserAccountData;
  }

  //TODO : synchronization ?
  private static UserService getUserService() {
    if(null == sUserService) {
      sUserService = MaxCore.create(UserService.class);
    }

    return sUserService;
  }
}
