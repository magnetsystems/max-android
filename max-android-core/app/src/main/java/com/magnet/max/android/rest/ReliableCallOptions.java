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
package com.magnet.max.android.rest;

import android.util.Log;
import com.google.gson.annotations.Expose;
import com.magnet.max.android.util.MagnetUtils;
import com.magnet.max.android.rest.qos.Condition;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReliableCallOptions {
  private static final String TAG = ReliableCallOptions.class.getSimpleName();
  private static final int DEFAULT_EXPIRE_TIME = 30 * 24 * 3600; //One month

  public static ReliableCallOptions DEFAULT = new Builder().expiresIn(DEFAULT_EXPIRE_TIME).build();

  // QoS controller
  private int expiresIn;
  @Expose
  private Set<Condition> conditions;

  private long createAt;

  private boolean useMock;

  /**
   * private constructor, alway use builder
   */
  private ReliableCallOptions() {
    createAt = System.currentTimeMillis();
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public Set<Condition> getConditions() {
    return conditions;
  }

  public Set<Condition> unsatisfiedConditions(boolean shortCircuit) {
    if(null == conditions || conditions.isEmpty()) {
      return Collections.EMPTY_SET;
    } else {
      Set<Condition> failedPrerequisites = new HashSet<>();
      for(Condition prerequisite : conditions) {
        if(!prerequisite.isMet()) {
          Log.i(TAG, "Prerequisite " + prerequisite + " is not met");
          failedPrerequisites.add(prerequisite);
          if(shortCircuit) {
            return failedPrerequisites;
          }
        }
      }

      return failedPrerequisites;
    }
  }

  public boolean evaluateConditions(boolean shortCircuit) {
    return unsatisfiedConditions(shortCircuit).isEmpty();
  }

  public boolean isExpired() {
    return expiresIn > 0 && System.currentTimeMillis() - createAt > expiresIn * 1000;
  }

  public boolean useMock() {
    return useMock;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ReliableCallOptions(");
    sb.append("expiresIn:").append(expiresIn).append(", ");
    sb.append("conditions:").append(MagnetUtils.setToString(conditions));
    sb.append(")");

    return sb.toString();
  }

  public static class Builder {
    private ReliableCallOptions toBuild = new ReliableCallOptions();

    public Builder expiresIn(int value) {
      toBuild.expiresIn = value;
      return this;
    }

//    public WriteOptionsBuilder isReliable(boolean value) {
//      toBuild.isReliable = value;
//      return this;
//    }

    public Builder conditions(Condition... prerequisite) {
      if(null == toBuild.conditions) {
        toBuild.conditions = new HashSet<>();
      }
      toBuild.conditions.addAll(Arrays.asList(prerequisite));

      return this;
    }

    public Builder useMock(Boolean value) {
      toBuild.useMock = value;
      return this;
    }

    public ReliableCallOptions build() {
      return toBuild;
    }
  }

}
