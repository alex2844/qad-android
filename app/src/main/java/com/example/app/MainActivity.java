/*
=====================================================
Qad Android for Qad Framework (MainActivity.java)
-----------------------------------------------------
https://pcmasters.ml/
-----------------------------------------------------
Copyright (c) 2016 Alex Smith
=====================================================
*/
package com.example.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.content.Intent;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
	private WebView mWebView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWebView = (WebView) findViewById(R.id.activity_main_webview);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new Qad(), "$$$");
		mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.setWebViewClient(new WebViewClient());
		if (savedInstanceState == null)
			mWebView.loadUrl("");
	}
	@Override
	protected void onSaveInstanceState(Bundle outState ) {
		super.onSaveInstanceState(outState);
		mWebView.saveState(outState);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mWebView.restoreState(savedInstanceState);
	}
	private class Qad {
		@android.webkit.JavascriptInterface
		public void open(String url) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		@android.webkit.JavascriptInterface
		public String getParse(String http) {
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			String resultJson = "";
			try {
				URL url = new URL(http);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();
				InputStream inputStream = urlConnection.getInputStream();
				StringBuffer buffer = new StringBuffer();
				reader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}
				resultJson = buffer.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return resultJson;
		}
	}
	@Override
	public void onBackPressed() {
		if(mWebView.canGoBack())
			mWebView.goBack();
		else
			super.onBackPressed();
	}
}