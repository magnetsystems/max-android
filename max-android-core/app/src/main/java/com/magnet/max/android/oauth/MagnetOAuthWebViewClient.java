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
package com.magnet.max.android.oauth;

import com.magnet.max.android.util.StringUtil;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import android.app.Activity;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * This WebViewClient is intended to be used with apps that
 * utilize the Magnet platform's built-in integration with
 * Facebook, LinkedIn, and Salesforce services.  When these
 * services need authentication (using OAuth), the SDK will
 * call startActivity with action.
 *
 * MagnetRestAuthHandler.ACTION_LAUNCH_OAUTH_FLOW
 *
 * The application should implement an activity with a WebView
 * that uses this implementation of the WebViewClient.
 *
 */
public class MagnetOAuthWebViewClient extends WebViewClient {
  private static final String TAG = MagnetOAuthWebViewClient.class.getSimpleName();
  private static final String OAUTH_DONE_URI_PARAMETER = "X-OAuth-Done-Uri";
  private static final String CLOSE_REDIRECT_URL = "http://close";

  private boolean mIsCompleted;
  private Activity mActivity;
  private Uri mUri;
  private WebView mWebView;

  /**
   * @param activity the activity containing the WebView
   * @param uri the uri for this OAuth request
   * @param webView the actual WebView instance
   */
  public MagnetOAuthWebViewClient(Activity activity, Uri uri, WebView webView) {
    super();
    mActivity = activity;
    //mConfig = connectionConfig;
    mUri = uri;
    //try {
    //  mRedirectUrl = new URL(mUri.getQueryParameter(OauthConstants.OAUTH_REDIRECT_URI_PARAMETER));
    //} catch (MalformedURLException e) {
    //  throw new IllegalArgumentException(e);
    //}
    mWebView = webView;
    WebSettings settings = mWebView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setSavePassword(false);
    settings.setSaveFormData(false);
  }

  @Override
  public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    Log.w(TAG, "Ignoring this SSL Error:" + error.toString());
    handler.proceed();
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    try {
      URL urlObject = new URL(url);
      if (url.startsWith(CLOSE_REDIRECT_URL)) {
        Log.d(TAG, "shouldOverrideUrlLoading(): found the close url, closing the flow");
        mActivity.finish();
        mIsCompleted = true;
        return true;
      } else if (isLocalRedirectUrl(urlObject)) {
        HashMap<String, String> mHeaders = new HashMap<String, String>();
        mHeaders.put(OAUTH_DONE_URI_PARAMETER, CLOSE_REDIRECT_URL);
        view.loadUrl(url, mHeaders);
        return true;
      }
      return super.shouldOverrideUrlLoading(view, url);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private boolean isLocalRedirectUrl(URL url) {
    //if this the redirect_uri pulled from the initial url matches this one
    String query = url.getQuery();
    if(StringUtil.isNotEmpty(query) && query.contains(OauthConstants.OAUTH_REDIRECT_URI_PARAMETER)) {
      return true;
    }
    return false;
  }

  private String buildLocalRedirectUrlWithClose(String url, String sessionId) {
    StringBuilder sb = new StringBuilder(url);
    if (sb.indexOf("?") == -1) {
      sb.append('?');
    } else {
      sb.append('&');
    }
    sb.append(OAUTH_DONE_URI_PARAMETER).append('=');
    try {
      sb.append(URLEncoder.encode(CLOSE_REDIRECT_URL, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      //coding error
      throw new RuntimeException(e);
    }

    //if (sessionId != null) {
    //  sb.append('&').append(MagnetRestConnectionService.Header.SESSION_ID).append('=').append(sessionId);
    //}
    return sb.toString();
  }
}
