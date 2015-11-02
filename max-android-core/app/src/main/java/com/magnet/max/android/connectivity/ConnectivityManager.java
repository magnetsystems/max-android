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
package com.magnet.max.android.connectivity;

import android.content.Context;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public class ConnectivityManager {
  public static int TYPE_UNKNOWN = -1;
  public static int TYPE_NOT_CONNECTED = 0;
  public static int TYPE_WIFI = 1;
  public static int TYPE_MOBILE = 2;

  private volatile int lastKnownStatus;

  private final Context applicationContext;

  private static volatile ConnectivityManager _instance;

  private Set<WeakReference<ConnectivityListener>> listeners;

  private ConnectivityManager(Context applicationContext) {
    this.applicationContext = applicationContext;
    listeners = new HashSet<>();
  }

  public static synchronized void initialize(Context applicationContext) {
    if(null == _instance) {
      _instance = new ConnectivityManager(applicationContext);
    } else {
      //Clean up ?
    }
  }

  public static ConnectivityManager getInstance() {
    if(null == _instance) {
      throw new IllegalStateException("ConnectivityManager hasn't been initialized.");
    }
    return _instance;
  }

  public static ConnectivityManager getInstance(Context context) {
    if(null == _instance) {
      initialize(context.getApplicationContext());
    }
    return _instance;
  }

  /**
   * Get current network status
   * @return
   */
  public int getConnectivityStatus() {
    return getConnectivityStatus(false);
  }

  /**
   * Get current network status and optionally notify all listeners
   * @param toNotify
   * @return
   */
  private int getConnectivityStatus(boolean toNotify) {
    android.net.ConnectivityManager cm = (android.net.ConnectivityManager) applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    int result = TYPE_UNKNOWN;
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    if (null != activeNetwork) {
      if(activeNetwork.getType() == android.net.ConnectivityManager.TYPE_WIFI) {
        result = TYPE_WIFI;
      } else if(activeNetwork.getType() == android.net.ConnectivityManager.TYPE_MOBILE) {
        result = TYPE_MOBILE;
      } else if(!activeNetwork.isConnectedOrConnecting()) {
        result = TYPE_NOT_CONNECTED;
      }
    } else {
      result = TYPE_NOT_CONNECTED;
    }

    if(lastKnownStatus != result && toNotify) {
      notifyListeners(result);
    }

    lastKnownStatus = result;

    return result;
  }

  public synchronized void registerListener(ConnectivityListener l) {
    if(null == findListener(l)) {
      listeners.add(new WeakReference<ConnectivityListener>(l));
    }
  }

  public synchronized void removeListener(ConnectivityListener l) {
    WeakReference<ConnectivityListener> lr = findListener(l);
    if(null != lr) {
      listeners.remove(lr);
    }
  }

  public int getLastKnownConnectivityStatus() {
    return lastKnownStatus;
  }

  public String getConnectivityStatusString() {
    int conn = getConnectivityStatus();
    String status = null;
    if (conn == ConnectivityManager.TYPE_WIFI) {
      status = "Wifi enabled";
    } else if (conn == ConnectivityManager.TYPE_MOBILE) {
      status = "Mobile data enabled";
    } else if (conn == ConnectivityManager.TYPE_NOT_CONNECTED) {
      status = "Not connected to Internet";
    }
    return status;
  }

  private void notifyListeners(final int newStatus) {
//    new AsyncTask<Void, Void, Void>() {
//      @Override
//      protected Void doInBackground(Void... args) {
        for(WeakReference<ConnectivityListener> lr : listeners) {
          if(null != lr.get()) {
            lr.get().onConnectivityStatusChanged(lastKnownStatus, newStatus);
          }
        }
//
//        return null;
//      }
//    }.execute();
  }

  public void notifyListeners() {
    notifyListeners(getConnectivityStatus());
  }

  private WeakReference<ConnectivityListener> findListener(ConnectivityListener cl) {
    for(WeakReference<ConnectivityListener> lr : listeners) {
      if(cl == lr.get()) {
        return lr;
      }
    }

    return null;
  }
}
