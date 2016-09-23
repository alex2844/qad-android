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
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.widget.EditText;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity {
	private WebView mWebView;
	private ProgressDialog progressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWebView = (WebView)findViewById(R.id.activity_main_webview);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setDatabasePath("/data/data/"+getPackageName()+"/databases/");
		webSettings.setSupportZoom(false);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mWebView.addJavascriptInterface(new Qad(), "$$$");
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.setWebChromeClient(new JsWebChromeClient());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
				WebView.setWebContentsDebuggingEnabled(true);
			}
		if (savedInstanceState == null)
			mWebView.loadUrl("file:///android_asset/www/page/rbook/index.html");
		progressBar = new ProgressDialog(this);
		progressBar.setIndeterminate(true);
		progressBar.setCancelable(false);
		progressBar.setMessage("Loading");
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
		public void onload(boolean show) {
			if (show)
				progressBar.hide();
			else
				progressBar.show();
		}
		@android.webkit.JavascriptInterface
		public void open(String url) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		@android.webkit.JavascriptInterface
		public String shell(String exec, boolean su) {
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
		public String curl(String http) {
			String result = "";
			try {
				URL url = new URL(http);
				URLConnection conn = url.openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null)
					result = line;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	@Override
	public void onBackPressed() {
		if(mWebView.canGoBack())
			mWebView.goBack();
		else
			super.onBackPressed();
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
			result.cancel();
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
