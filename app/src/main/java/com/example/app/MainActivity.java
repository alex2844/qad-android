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
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JsResult;
import android.content.Intent;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.View;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
	private WebView mWebView;
	SharedPreferences sPref;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWebView = (WebView) findViewById(R.id.activity_main_webview);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setSupportZoom(false);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mWebView.addJavascriptInterface(new Qad(), "$$$");
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.setWebChromeClient(new JsWebChromeClient());
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
		public void sconfig(String key, String value) {
			sPref = getPreferences(MODE_PRIVATE);
			Editor ed = sPref.edit();
			ed.putString(key, value);
			ed.commit();
		}
		@android.webkit.JavascriptInterface
		public String lconfig(String key) {
			sPref = getPreferences(MODE_PRIVATE);
			return sPref.getString(key, "");
		}
		@android.webkit.JavascriptInterface
		public String sh(String exec, boolean su) {
			String data = "";
			try {
				Process process;
				if (su)
					process = Runtime.getRuntime().exec(new String[] {"su", "-c", exec});
				else
					process = Runtime.getRuntime().exec(exec);
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				int read;
				char[] buffer = new char[4096];
				StringBuffer output = new StringBuffer();
				while ((read = reader.read(buffer)) > 0)
					output.append(buffer, 0, read);
				reader.close();
				process.waitFor();
				data = output.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return data;
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
		else{
			sPref = getPreferences(MODE_PRIVATE);
			Editor ed = sPref.edit();
			ed.putString("state", "");
			ed.commit();
			super.onBackPressed();
		}
	}
	private class JsWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
			AlertDialog.Builder b = new AlertDialog.Builder(view.getContext())
				.setTitle(view.getTitle())
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				});
			b.show();
			return true;
		}
		@Override
		public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
			AlertDialog.Builder b = new AlertDialog.Builder(view.getContext())
				.setTitle(view.getTitle())
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.cancel();
					}
				});
			b.show();
			return true;
		}
	}
}
