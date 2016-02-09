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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.gson.annotations.SerializedName;
import com.magnet.max.android.auth.model.RenewTokenRequest;
import com.magnet.max.android.auth.model.UpdateProfileRequest;
import com.magnet.max.android.auth.model.UserLoginResponse;
import com.magnet.max.android.auth.model.UserRealm;
import com.magnet.max.android.auth.model.UserRegistrationInfo;
import com.magnet.max.android.auth.model.UserToken;
import com.magnet.max.android.util.AuthUtil;
import com.magnet.max.android.util.EqualityUtil;
import com.magnet.max.android.util.HashCodeBuilder;
import com.magnet.max.android.util.ParcelableHelper;
import com.magnet.max.android.util.StringUtil;
import java.io.File;
import java.util.Collections;
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
final public class User extends UserProfile {

  public enum SessionStatus {
    NotLoggedIn,
    LoggedIn,
    CanResume
  }

  private static final String TAG = "User";

  @SerializedName("email")
  private String mEmail;
  @SerializedName("roles")
  private String[] mRoles;
  //@SerializedName("userStatus")
  //private UserStatus mUserStatus;
  @SerializedName("userName")
  private String mUserName;
  @SerializedName("userRealm")
  private UserRealm mUserRealm;
  @SerializedName("tags")
  private String[] mTags;
  @SerializedName("userAccountData")
  private java.util.Map<String, String> mExtras;

  private static final AtomicReference<User> sCurrentUserRef = new AtomicReference<>();

  /**
   * Register a new user
   * @param userRegistrationInfo
   * @param callback
   */
  public static void register(final UserRegistrationInfo userRegistrationInfo, final ApiCallback<User> callback) {
    getUserService().register(userRegistrationInfo, new Callback<User>() {
      @Override public void onResponse(Response<User> response) {
         ApiCallbackHelper.executeCallback(callback, response);
      }

      @Override public void onFailure(Throwable throwable) {
        Log.e(TAG, "user register error : " + throwable.getMessage());
        ApiCallbackHelper.executeCallback(callback, throwable);
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
            if(response.isSuccess()) {
              Log.i(TAG, "userLogin success : ");
            } else {
              Log.e(TAG, "userLogin failed due to : " + response.message());
              ApiCallbackHelper.executeCallback(callback, response);
              return;
            }

            UserLoginResponse userLoginResponse = response.body();
            boolean result = false;

            if (null != userLoginResponse.getUser()) {
              sCurrentUserRef.set(userLoginResponse.getUser());
            }

            boolean isCallbackCalled = false;
            if (null != userLoginResponse.getAccessToken()) {
              isCallbackCalled = ModuleManager.onUserLogin(userLoginResponse.getUser().getUserIdentifier(),
                  new UserToken(userLoginResponse.getExpiresIn(), userLoginResponse.getAccessToken(),
                      userLoginResponse.getRefreshToken(), userLoginResponse.getTokenType()),
                  rememberMe, callback);

              result = true;
            }

            //Only call callback when it's not called by modules
            if(!isCallbackCalled) {
              ApiCallbackHelper.executeCallback(callback, Response.success(result));
            }
          }

          @Override public void onFailure(Throwable throwable) {
            Log.e(TAG, "userLogin error : " + throwable.getMessage());
            ApiCallbackHelper.executeCallback(callback, throwable);
          }
        });

    call.executeInBackground();
  }

  /**
   * Resume previous session without login.
   * Only applicable when rememberMe is set to true in @see #login
   * @param callback
   */
  public static void resumeSession(final ApiCallback<Boolean> callback) {
    if(null != getCurrentUser()) {
      if(null != callback) {
        callback.success(true);
      }
    } else {
      if(SessionStatus.CanResume == getSessionStatus()) {
        UserToken userToken = ModuleManager.getUserToken();
        final ApiCallback<Boolean> wrappedCallback = new ApiCallback<Boolean>() {
          @Override public void success(Boolean aBoolean) {
            if (aBoolean) {
              Log.d(TAG, "User session resumed");
            } else {
              Log.e(TAG, "User session failed to resume");
            }

            if (null != callback) {
              callback.success(aBoolean);
            }
          }

          @Override public void failure(ApiError error) {
            Log.e(TAG, "User session failed to resume due to " + error);
            if (null != callback) {
              callback.failure(error);
            }
          }
        };
        if(!userToken.isExpired()) {
          User.setCurrentUser(ModuleManager.getCachedUser());
          ModuleManager.onUserSessioinResume(wrappedCallback);
        } else {
          if(StringUtil.isNotEmpty(userToken.getRefreshToken())) {
            getUserService().renewToken(new RenewTokenRequest(userToken.getRefreshToken()), AuthUtil.generateOAuthToken(userToken.getRefreshToken()),
                new Callback<UserLoginResponse>() {
                  @Override public void onResponse(retrofit.Response<UserLoginResponse> response) {
                    if (response.isSuccess()) {
                      Log.i(TAG, "renewToken success : ");
                    } else {
                      handleUserTokenRefreshFailure("renewToken failed due to : " + response.message());
                      return;
                    }

                    UserLoginResponse userLoginResponse = response.body();
                    User.setCurrentUser(ModuleManager.getCachedUser());
                    if (null != userLoginResponse.getAccessToken()) {
                      ModuleManager.onUserTokenRefresh(userLoginResponse.getUser().getUserIdentifier(),
                          new UserToken(userLoginResponse.getExpiresIn(), userLoginResponse.getAccessToken(),
                              userLoginResponse.getRefreshToken(), userLoginResponse.getTokenType()), wrappedCallback);
                    } else {
                      handleUserTokenRefreshFailure("No access token returned from refresh token response");
                    }
                  }

                  @Override public void onFailure(Throwable throwable) {
                    handleUserTokenRefreshFailure("renewToken failed due to : " + throwable.getMessage());
                  }

                  private void handleUserTokenRefreshFailure(String errorMessage) {
                    Log.e(TAG, errorMessage);

                    ModuleManager.onUserTokenInvalid();

                    wrappedCallback.failure(new ApiError(errorMessage));
                  }
                }).executeInBackground();
          } else {
            if (null != callback) {
              callback.failure(new ApiError("Token has expired and refresh token is not available"));
            }
          }
        }
      } else {
        if(null != callback) {
          callback.failure(new ApiError("Session is not resumable"));
        }
      }
    }
  }

  /**
   * Get the status of session.
   * @return
   */
  public static SessionStatus getSessionStatus() {
    if(null != getCurrentUser()) {
      return SessionStatus.LoggedIn;
    } else {
      return (null != ModuleManager.getUserToken() && null != ModuleManager.getCachedUser()) ? SessionStatus.CanResume : SessionStatus.NotLoggedIn;
    }
  }

  /**
   * User logout without callback
   */
  public static void logout() {
    logout(null);
  }
  /**
   * User logout with callback
   * @param callback
   */
  public static void logout(final ApiCallback<Boolean> callback) {
    if(null == sCurrentUserRef.get()) {
      ApiCallbackHelper.executeCallback(callback, new ApiError("User has not login"));
      return;
    }

    // Only call server API when token is available
    if(null != ModuleManager.getUserToken()) {
      final String currentUserId = getCurrentUserId();

      // Unregister device
      Device.unRegister(null);

      getUserService().userLogout(new Callback<Boolean>() {
        @Override public void onResponse(Response<Boolean> response) {
          Log.e(TAG, "user logout successfully : " + currentUserId);

          ModuleManager.onUserLogout(currentUserId);

          ApiCallbackHelper.executeCallback(callback, response);
        }

        @Override public void onFailure(Throwable throwable) {
          Log.e(TAG, "user logout error : " + throwable.getMessage());

          ModuleManager.onUserLogout(currentUserId);

          ApiCallbackHelper.executeCallback(callback, throwable);
        }
      }).executeInBackground();
    } else {
      ApiCallbackHelper.executeCallback(callback, Response.success(true));
    }

    sCurrentUserRef.set(null);
  }

  /**
   * Search users
   * @param query see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/1.4/query-dsl-query-string-query.html#query-string-syntax">Elastic search query string syntax</a>
   * @param limit The number of records to retrieve
   * @param offset The offset to start from.
   * @param sort The sort criteria
   * @param callback
   */
  public static void search(String query, Integer limit, Integer offset, String sort, final ApiCallback<List<User>> callback) {
    getUserService().searchUsers(query, limit, offset, sort, new Callback<List<User>>() {
      @Override public void onResponse(Response<List<User>> response) {
        ApiCallbackHelper.executeCallback(callback, response);
      }

      @Override public void onFailure(Throwable throwable) {
        ApiCallbackHelper.executeCallback(callback, throwable);
      }
    }).executeInBackground();
  }

  /**
   * Get list of User by user names
   * @param userNames
   * @param callback
   */
  public static void getUsersByUserNames(List<String> userNames, final ApiCallback<List<User>> callback) {
    if(null == userNames || userNames.isEmpty()) {
      if(null != callback) {
        callback.success(Collections.EMPTY_LIST);
      }

      return;
    }

    getUserService().getUsersByUserNames(userNames, new Callback<List<User>>() {
      @Override public void onResponse(Response<List<User>> response) {
        ApiCallbackHelper.executeCallback(callback, response);
      }

      @Override public void onFailure(Throwable throwable) {
        ApiCallbackHelper.executeCallback(callback, throwable);
      }
    }).executeInBackground();
  }

  /**
   * Get list of User by ids
   * @param userIds
   * @param callback
   */
  public static void getUsersByUserIds(List<String> userIds, final ApiCallback<List<User>> callback) {
    if(null == userIds || userIds.isEmpty()) {
      if(null != callback) {
        callback.success(Collections.EMPTY_LIST);
      }

      return;
    }

    getUserService().getUsersByUserIds(userIds, new Callback<List<User>>() {
      @Override public void onResponse(Response<List<User>> response) {
        ApiCallbackHelper.executeCallback(callback, response);
      }

      @Override public void onFailure(Throwable throwable) {
        ApiCallbackHelper.executeCallback(callback, throwable);
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
   * Only use internally for rememberme
   * @param user
   */
  static void setCurrentUser(User user) {
    sCurrentUserRef.set(user);
  }

  /**
   * Get the id of currently login user
   * @return
   */
  public static String getCurrentUserId() {
    return null != sCurrentUserRef.get() ? sCurrentUserRef.get().getUserIdentifier() : null;
  }

  /**
   * Update profile of current user
   * @param updateProfileRequest
   * @param callback
   */
  public static void updateProfile(UpdateProfileRequest updateProfileRequest, final ApiCallback<User> callback) {
    if(null == sCurrentUserRef.get()) {
      callback.failure(new ApiError("User has not login"));
      return;
    }

    MagnetCall<User> call = getUserService().updateProfile(updateProfileRequest, new Callback<User>() {
      @Override public void onResponse(Response<User> response) {
        if (response.isSuccess()) {
          sCurrentUserRef.set(response.body());
        }

        ApiCallbackHelper.executeCallback(callback, response);
      }

      @Override public void onFailure(Throwable throwable) {
        ApiCallbackHelper.executeCallback(callback, throwable);
      }
    });
    call.executeInBackground();
  }

  /**
   * Set a image file as user profile image
   * @param imageFile
   * @param listener
   */
  public void setAvatar(File imageFile, final ApiCallback<String> listener) {
    if(StringUtil.isStringValueEqual(mUserIdentifier, User.getCurrentUserId())) {
      if (null != imageFile) {
        Attachment attachment = new Attachment(imageFile,
            Attachment.getMimeType(imageFile.getName(), Attachment.MIME_TYPE_IMAGE));
        attachment.addMetaData(Attachment.META_FILE_ID, mUserIdentifier);
        attachment.upload(new Attachment.UploadListener() {
          @Override public void onStart(Attachment attachment) {

          }

          @Override public void onComplete(Attachment attachment) {
            if(null != listener) {
              listener.success(getAvatarUrl());
            }
          }

          @Override public void onError(Attachment attachment, Throwable error) {
            if(null != listener) {
              listener.failure(new ApiError(error));
            }
          }
        });
      } else {
        if(null != listener) {
          listener.failure(new ApiError("image should not be null"));
        }
      }
    } else {
      if(null != listener) {
        listener.failure(new ApiError("User can only set his/her own avatar"));
      }
    }
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

  ///**
  // * The status {@link UserStatus} for the user.
  // */
  //public UserStatus getUserStatus() {
  //  return mUserStatus;
  //}

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
   * The tags associated with the user.
   */
  public String[] getTags() {
    return mTags;
  }

  /**
   * The additional key-value pairs associated with the user.
   */
  public Map<String, String> getExtras() {
    return mExtras;
  }

  private static UserService getUserService() {
    return MaxCore.create(UserService.class);
  }

  /**
   * Compares this User object with the specified object and indicates if they
   * are equal. Following properties are compared :
   * <p><ul>
   * <li>userIdentifier
   * <li>email
   * <li>userName
   * <li>firstName
   * <li>lastName
   * <li>userRealm
   * </ul><p>
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if(!EqualityUtil.quickCheck(this, obj)) {
      return false;
    }

    User theOther = (User) obj;

    return StringUtil.isStringValueEqual(mUserIdentifier, theOther.getUserIdentifier()) &&
        StringUtil.isStringValueEqual(mEmail, theOther.getEmail()) &&
        StringUtil.isStringValueEqual(mUserName, theOther.getUserName()) &&
        StringUtil.isStringValueEqual(mFirstName, theOther.getFirstName()) &&
        StringUtil.isStringValueEqual(mLastName, theOther.getLastName()) &&
        (mUserRealm == theOther.getUserRealm());
  }

  /**
   *  Returns an integer hash code for this object.
   *  @see #equals(Object) for the properties used for hash calculation.
   * @return
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().hash(mUserIdentifier).hash(mEmail).hash(mUserName)
        .hash(mFirstName).hash(mLastName).hash(mUserRealm).hashCode();
  }

  @Override public String toString() {
    return new StringBuilder().append("{")
        .append("userIdentifier = ").append(mUserIdentifier).append(", ")
        .append("userName = ").append(mUserName).append(", ")
        .append("firstName = ").append(mFirstName).append(", ")
        .append("lastName = ").append(mLastName).append(", ")
        .append("email = ").append(mEmail).append(", ")
        .append("userRealm = ").append(mUserRealm).append(", ")
        .append("roles = ").append(StringUtil.toString(mRoles)).append(", ")
        .append("tags = ").append(StringUtil.toString(mTags)).append(", ")
        .append("extras = ").append(StringUtil.toString(mExtras))
        .append("}")
        .toString();
  }

  //----------------Parcelable Methods----------------

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(this.mEmail);
    dest.writeStringArray(this.mRoles);
    dest.writeString(this.mUserName);
    dest.writeInt(this.mUserRealm == null ? -1 : this.mUserRealm.ordinal());
    dest.writeStringArray(this.mTags);
    dest.writeBundle(ParcelableHelper.stringMapToBundle(this.mExtras));
  }

  protected User() {
  }

  protected User(Parcel in) {
    this.mUserIdentifier = in.readString();
    this.mFirstName = in.readString();
    this.mLastName = in.readString();
    this.mEmail = in.readString();
    this.mRoles = in.createStringArray();
    this.mUserName = in.readString();
    int tmpMUserRealm = in.readInt();
    this.mUserRealm = tmpMUserRealm == -1 ? null : UserRealm.values()[tmpMUserRealm];
    this.mTags = in.createStringArray();
    this.mExtras = ParcelableHelper.stringMapfromBundle(in.readBundle(getClass().getClassLoader()));
  }

  public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
    public User createFromParcel(Parcel source) {
      return new User(source);
    }

    public User[] newArray(int size) {
      return new User[size];
    }
  };
}
