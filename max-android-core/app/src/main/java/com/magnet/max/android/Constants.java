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

public interface Constants {
  // Local BroadCast intent action for invalid app token
  String APP_AUTH_CHALLENGE_INTENT_ACTION = "com.magnet.android.APP_AUTH_CHALLENGE";
  // Local BroadCast intent action for invalid user token
  String USER_AUTH_CHALLENGE_INTENT_ACTION = "com.magnet.android.USER_AUTH_CHALLENGE";
}
