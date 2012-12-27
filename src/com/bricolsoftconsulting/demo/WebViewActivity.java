/*
Copyright 2012 Bricolsoft Consulting

   ----------------------------------------------------------------------------

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   ----------------------------------------------------------------------------

   Sample usage for Android webview 17535 fix.
   
*/

package com.bricolsoftconsulting.demo;

import com.bricolsoftconsulting.webview.WebViewClientEx;
import com.bricolsoftconsulting.webview.WebViewEx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewActivity extends Activity
{
	// Members
	private WebViewEx mWebView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initWebView(savedInstanceState);
	}

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	public void initWebView(Bundle savedInstanceState)
	{
		// Find the webview
		mWebView = (WebViewEx)WebViewActivity.this.findViewById(R.id.webview);
        
		// Intercept URLs
		mWebView.setWebViewClient(new WebViewClientEx(WebViewActivity.this)
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				// Special handling for shouldOverrideUrlLoading
				// Make sure that we call the base class implementation and do not interfere
				// with the base class redirects
				boolean redirected = super.shouldOverrideUrlLoading(view, url);

				// Do your own redirects here and set the return flag
				if (!redirected)
				{
					// Redirect HTTP and HTTPS urls to the external browser
					if (url != null && URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url))
					{
						view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						redirected = true;
					}
				}

				return redirected;
			}

			@SuppressLint("NewApi")
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, String url)
			{
				// Special handling for shouldInterceptRequest
				// Make sure that we call the base class implementation
				WebResourceResponse wrr = super.shouldInterceptRequest(view, url);

				// Do your own resource replacements here
				if (wrr == null)
				{
					// wrr = new WebResourceResponse...
				}

				return wrr;
			}
		});
 
		// Enable JavaScript alert dialogs
		mWebView.setWebChromeClient(new WebChromeClient()
		{
			@Override  
			public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)  
			{
				if (!isFinishing())
				{
					new AlertDialog.Builder(WebViewActivity.this)  
											.setTitle(R.string.app_name)  
											.setMessage(message)  
											.setPositiveButton(android.R.string.ok,  
															new AlertDialog.OnClickListener()  
															{  
																public void onClick(DialogInterface dialog, int which)  
																{  
																	result.confirm();  
																}  
															})
											.setCancelable(false)  
											.create()  
											.show();  
				}

				return true;  
			};

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result)
			{
				if (!isFinishing())
				{
					new AlertDialog.Builder(WebViewActivity.this)
					.setTitle(R.string.app_name)
					.setMessage(message)
					.setPositiveButton(android.R.string.ok, 
									new DialogInterface.OnClickListener() 
									{
											public void onClick(DialogInterface dialog, int which) 
											{
												result.confirm();
											}
									})
					.setNegativeButton(android.R.string.cancel, 
									new DialogInterface.OnClickListener() 
									{
											public void onClick(DialogInterface dialog, int which) 
											{
												result.cancel();
											}
									})
					.create()
					.show();
				}

				return true;
			};

			@Override
			public void onReceivedTitle(WebView view, String title)
			{
				super.onReceivedTitle(view, title);
			}
		});

		// Enable JavaScript
		mWebView.getSettings().setJavaScriptEnabled(true);

		// Enable plugins
		mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);

		// Increase the priority of the rendering thread to high
		mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

		// Enable application caching
		mWebView.getSettings().setAppCacheEnabled(true);

		// Enable HTML5 local storage and make it persistent
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setDatabasePath("/data/data/" + WebViewActivity.this.getPackageName() + "/databases/");

		// Clear spurious cache data
		mWebView.clearHistory();
		mWebView.clearFormData();
		mWebView.clearCache(true);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		// Accept cookies
		CookieSyncManager.createInstance(WebViewActivity.this); 
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);

		// Make sure that the webview does not allocate blank space on the side for the scrollbars
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    
		// Load the URL
		String url = "file:///android_asset/page1.html";
		mWebView.loadUrl(url);
	}
}
