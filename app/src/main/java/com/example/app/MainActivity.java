/*
=====================================================
Qad Make for Qad Framework (MainActivity.java)
-----------------------------------------------------
https://pcmasters.ml/
-----------------------------------------------------
Copyright (c) 2016 Alex Smith
=====================================================
*/
package com.example.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
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
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;

public class MainActivity extends Activity {
	private WebView mWebView;
	private ProgressDialog progressBar;
	private ValueCallback<Uri> FilePathKitkat;
	private ValueCallback<Uri[]> FilePathLollipop;
	private final static int FILE_SELECTED = 1;
	private SharedPreferences Settings;
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
		mWebView.setWebViewClient(new JsWebViewClient());
		mWebView.setWebChromeClient(new JsWebChromeClient());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
				WebView.setWebContentsDebuggingEnabled(true);
			}
		if (savedInstanceState == null)
			mWebView.loadUrl("");
		progressBar = new ProgressDialog(this);
		progressBar.setIndeterminate(true);
		progressBar.setCancelable(false);
		progressBar.setMessage("Loading");
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == FILE_SELECTED) {
			if (resultCode == RESULT_OK) {
				if (intent != null) {
					if (FilePathKitkat != null) {
						FilePathKitkat.onReceiveValue(intent.getData());
						FilePathKitkat = null;
					}else if (FilePathLollipop != null) {
						Uri[] results;
						try {
							results = new Uri[] { Uri.parse(intent.getDataString()) };
						} catch (Exception e) {
							results = null;
						}
						FilePathLollipop.onReceiveValue(results);
						FilePathLollipop = null;
					}
				}
			}else{
				if (FilePathKitkat != null) {
					FilePathKitkat.onReceiveValue(null);
					FilePathKitkat = null;
				}else if (FilePathLollipop != null) {
					FilePathLollipop.onReceiveValue(null);
					FilePathLollipop = null;
				}
			}
		}
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
		public int sdk() {
			return Build.VERSION.SDK_INT;
		}
		@android.webkit.JavascriptInterface
		public void open(String url) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		@android.webkit.JavascriptInterface
		public String account() {
			String ac = "";
			try {
				AccountManager am = AccountManager.get(mContext);
				Account[] accounts = am.getAccounts();
				for (Account account : accounts) {
					ac += account.toString();
				}
			} catch (Exception e) {
			}
			return ac;
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
		public String curl(String method, String url) {
			String result = "";
			try {
				Settings = getPreferences(MODE_PRIVATE);
				URL parse = new URL(url);
				HttpURLConnection request;
				if (method.equals("POST")) {
					URL post = new URL(parse.getProtocol()+"://"+parse.getHost()+parse.getPath());
					String query = parse.getQuery();
					request = (HttpURLConnection) post.openConnection();
					if (Settings.contains("session"))
						request.setRequestProperty("Cookie", Settings.getString("session", ""));
					request.setRequestMethod("POST");
					request.setDoOutput(true);
					request.addRequestProperty("Content-Length", Integer.toString(query.length()));
					request.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					OutputStreamWriter writer = new OutputStreamWriter(request.getOutputStream());
					writer.write(query);
					writer.flush();
				}else{
					request = (HttpURLConnection) parse.openConnection();
					if (Settings.contains("session"))
						request.setRequestProperty("Cookie", Settings.getString("session", ""));
					request.setRequestMethod(method);
				}
				if (request.getHeaderField("Set-cookie") != null) {
					String session = request.getHeaderField("Set-cookie").split(";")[0];
					if (session.contains("PHPSESSID")) {
						Editor save = Settings.edit();
						save.putString("session", session);
						save.commit();
					}
				}
				BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
				String line;
				StringBuffer output = new StringBuffer();
				while ((line = reader.readLine()) != null)
					output.append(line);
				result = output.toString();
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
	private class CancelListener implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener {
		CancelListener(JsResult result) {
			mResult = result;
		}
		private final JsResult mResult;
		@Override
		public void onCancel(DialogInterface dialog) {
			mResult.cancel();
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mResult.cancel();
		}
	}
	private class JsWebChromeClient extends WebChromeClient {
		public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture){
			FilePathKitkat = filePathCallback;
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType((acceptType.length() == 0 ? "*/*" : acceptType));
			startActivityForResult(intent, FILE_SELECTED);
		}
		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
			FilePathLollipop = filePathCallback;
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			if (fileChooserParams != null && fileChooserParams.getAcceptTypes() != null && fileChooserParams.getAcceptTypes().length > 0)
				intent.setType(fileChooserParams.getAcceptTypes()[0]);
			else
				intent.setType("*/*");
			startActivityForResult(intent, FILE_SELECTED);
			return true;
		}
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
		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
			final EditText data = new EditText(view.getContext());
			AlertDialog.Builder b = new AlertDialog.Builder(view.getContext())
			.setTitle(view.getTitle())
			.setView(data)
			.setMessage(message)
			.setOnCancelListener(new CancelListener(result))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					result.confirm(data.getText().toString());
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
	private class JsWebViewClient extends WebViewClient {
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			mWebView.loadData("<body style=\"background: #f1f1f1;margin-top: calc(50vh - 22px);\"><div style=\"background:#F44336;border-radius: 0.125rem;box-shadow: 0 2px 2px 0 rgba(0,0,0,.14),0 3px 1px -2px rgba(0,0,0,.2),0 1px 5px 0 rgba(0,0,0,.12);\"><h2 style=\"color: white;font-weight: 300;text-align: center;font-size: 18px;margin: 0;padding: 8px 0;line-height: 28px;\">Error conection</h2></div></body>", "text/html", "utf-8");
		}
	}
}
