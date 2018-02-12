/*
=====================================================
Qad Make for Qad Framework (MainActivity.java)
-----------------------------------------------------
https://qwedl.com/
-----------------------------------------------------
Copyright (c) 2016-2018 Alex Smith
=====================================================
*/
package com.example.app;

import android.app.Activity;
import android.app.ActivityManager.TaskDescription;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutInfo.Builder;
import android.content.pm.ShortcutManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
	private WebView mWebView;
	private FrameLayout mFullScreenContainer;
	private View mFullScreenView;
	private ProgressDialog progressBar;
	private ValueCallback<Uri> FilePathKitkat;
	private ValueCallback<Uri[]> FilePathLollipop;
	private SharedPreferences Settings;
	private WebChromeClient.CustomViewCallback mFullscreenViewCallback;
	private String progressBarTitle = "Loading";
	private String local = "file:///android_asset/www/page/movies/index.html";
	private static final int FILE_SELECTED = 1;
	private static final int RESULT_SPEECH = 2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWebView = (WebView)findViewById(R.id.activity_main_webview);
		mFullScreenContainer = (FrameLayout)findViewById(R.id.fullscreen_container);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportMultipleWindows(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setDatabasePath("/data/data/"+getPackageName()+"/databases/");
		webSettings.setSupportZoom(false);
		webSettings.setMediaPlaybackRequiresUserGesture(false);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mWebView.addJavascriptInterface(new Qad(this), "$$$");
		mWebView.setWebViewClient(new JsWebViewClient());
		mWebView.setWebChromeClient(new JsWebChromeClient());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))
				WebView.setWebContentsDebuggingEnabled(true);
		}
		if (savedInstanceState == null)
			mWebView.loadUrl(local);
		progressBar = new ProgressDialog(this);
		progressBar.setIndeterminate(true);
		progressBar.setCancelable(false);
		progressBar.setMessage(progressBarTitle);
		log("Launch application");
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && data != null) {
			switch (requestCode) {
				case FILE_SELECTED: {
					if (FilePathKitkat != null) {
						FilePathKitkat.onReceiveValue(data.getData());
						FilePathKitkat = null;
					}else if (FilePathLollipop != null) {
						Uri[] results;
						try {
							results = new Uri[] {
								Uri.parse(data.getDataString())
							};
						} catch (Exception e) {
							results = null;
						}
						FilePathLollipop.onReceiveValue(results);
						FilePathLollipop = null;
					}
					break;
				}
				case RESULT_SPEECH: {
					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					callback(text.get(0));
					break;
				}
			}
		}else{
			switch (requestCode) {
				case FILE_SELECTED: {
					if (FilePathKitkat != null) {
						FilePathKitkat.onReceiveValue(null);
						FilePathKitkat = null;
					}else if (FilePathLollipop != null) {
						FilePathLollipop.onReceiveValue(null);
						FilePathLollipop = null;
					}
					break;
				}
				case RESULT_SPEECH: {
					callback("");
					break;
				}
			}
		}
	}
	@Override
	protected void onSaveInstanceState(Bundle outState ) {
		super.onSaveInstanceState(outState);
		log("onSaveInstanceState");
		mWebView.saveState(outState);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		log("onRestoreInstanceState");
		mWebView.restoreState(savedInstanceState);
	}
	private class Qad {
		Context mContext;
		Qad(Context c) {
			mContext = c;
		}
		@android.webkit.JavascriptInterface
		public void setSpinner(String text) {
			log("setSpinner");
			progressBarTitle = text;
		}
		@android.webkit.JavascriptInterface
		public void spinner(boolean show) {
			log("spinner");
			if (progressBar != null) {
				progressBar.dismiss();
				progressBar = null;
			}
			if (show) {
				progressBar = new ProgressDialog(mContext);
				progressBar.setIndeterminate(true);
				progressBar.setCancelable(false);
				progressBar.setMessage(progressBarTitle);
				progressBar.show();
			}
		}
		@android.webkit.JavascriptInterface
		public int sdk() {
			return Build.VERSION.SDK_INT;
		}
		@android.webkit.JavascriptInterface
		public String version() {
			log("version");
			String res = "";
			try {
				res = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (PackageManager.NameNotFoundException e) {}
			return res;
		}
		@android.webkit.JavascriptInterface
		public void addLog(String m) {
			log(m);
		}
		@android.webkit.JavascriptInterface
		public void setTask(String title, String color) {
			log("setTask");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getWindow().setStatusBarColor(Color.parseColor(color));
				setTaskDescription(new TaskDescription(title, null, Color.parseColor(color)));
			}
		}
		@android.webkit.JavascriptInterface
		public void notification(String title, String json) {
			log("newNotification");
			newNotification(title, json);
		}
		@android.webkit.JavascriptInterface
		public void shortcuts(String json) {
			log("setShortcuts");
			setShortcuts(json);
		}
		@android.webkit.JavascriptInterface
		public void shortcuts() {
			log("getShortcuts");
			getShortcuts();
		}
		@android.webkit.JavascriptInterface
		public void share(String title, String text) {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
			intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
			startActivity(Intent.createChooser(intent, "Share"));
		}
		@android.webkit.JavascriptInterface
		public void open(String url, String type) {
			log("open type");
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.setDataAndType(Uri.parse(url), type);
			startActivity(intent);
		}
		@android.webkit.JavascriptInterface
		public void open(String url) {
			log("open");
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		@android.webkit.JavascriptInterface
		public void speech_rec() {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			try {
				startActivityForResult(intent, RESULT_SPEECH);
			} catch (ActivityNotFoundException a) {
				Toast.makeText(getApplicationContext(), "Opps! Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
				callback("");
			}
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
				log("shell: "+e.getMessage());
			}
			return data;
		}
		@android.webkit.JavascriptInterface
		public String getHeader(String url, String key, String referer) {
			String result = "";
			try {
				URL parse = new URL(url);
				HttpURLConnection request;
				request = (HttpURLConnection) parse.openConnection();
				request.setInstanceFollowRedirects(false);
				request.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				request.addRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.97 Mobile Safari/537.36");
				request.addRequestProperty("Referer", referer);
				if (key == "")
					for (Map.Entry<String, List<String>> k : request.getHeaderFields().entrySet()) {
						log("getHeader: "+k.toString());
					}
				else if (request.getHeaderField(key) != null)
					result = request.getHeaderField(key);
			} catch (Exception e) {}
			return result;
		}
		@android.webkit.JavascriptInterface
		public String fetch(String url, String method) {
			String result = "";
			method = method.toUpperCase();
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
			} catch (Exception e) {}
			return result;
		}
	}
	@Override
	public void onBackPressed() {
		log("onBackPressed");
		if (mWebView.canGoBack())
			mWebView.goBack();
		else
			super.onBackPressed();
	}
	private Bitmap imageDownload(String url) {
		Bitmap bmImg = null;
        try {
			URL parse = new URL(url);
			HttpURLConnection request = (HttpURLConnection) parse.openConnection();
			bmImg = BitmapFactory.decodeStream(request.getInputStream());
		} catch(Exception e) {
			log("imageDownload: "+e.getMessage());
        }
		return bmImg;
    }
	private void newNotification(String title, String json) {
		try {
			JSONObject jsonObject = new JSONObject(json);
			String action;
			Notification.Builder notification = new Notification.Builder(this).setTicker(title).setContentTitle(title).setAutoCancel(true).setSmallIcon(R.drawable.ic_notification).setDefaults(Notification.DEFAULT_ALL);
			if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && !jsonObject.isNull("icon") && (jsonObject.getString("icon").contains("http://") || jsonObject.getString("icon").contains("https://")))
				notification.setLargeIcon(Icon.createWithBitmap(imageDownload(jsonObject.getString("icon"))));
			if (!jsonObject.isNull("body"))
				notification.setContentText(jsonObject.getString("body"));
			if (!jsonObject.isNull("actions")) {
				JSONArray jsonArray = jsonObject.getJSONArray("actions");
				for (int i=0; i < jsonArray.length(); i++) {
					JSONObject jsonObjectAction = jsonArray.getJSONObject(i);
					action = jsonObjectAction.getString("action");
					if (!action.contains(":"))
						action = getPackageName()+"://{\"action\":\""+action+"\"}";
					notification.addAction(0, jsonObjectAction.getString("title"), PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_VIEW, Uri.parse(action)), 0));
				}
			}else if (!jsonObject.isNull("action")) {
				action = jsonObject.getString("action");
				if (!action.contains(":"))
					action = getPackageName()+"://{\"action\":\""+action+"\"}";
				notification.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(Intent.ACTION_VIEW, Uri.parse(action)), PendingIntent.FLAG_CANCEL_CURRENT));
			}
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify((jsonObject.isNull("id") ? 101 : jsonObject.getInt("id")), notification.build());
		} catch (Exception e) {
			log("newNotification: "+e.getMessage());
		}
	}
	private void setShortcuts(String json) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
			Settings = getPreferences(MODE_PRIVATE);
			for (int i=0; i < 5; i++) {
				if (Settings.contains("shortcut_"+i))
					Settings.edit().remove("shortcut_"+i).commit();
			}
			ShortcutManager sM = getSystemService(ShortcutManager.class);
			sM.removeAllDynamicShortcuts();
			try {
				JSONArray jsonArray = new JSONArray(json);
				for (int i=0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					ShortcutInfo shortcut = new ShortcutInfo.Builder(this, jsonObject.getString("id"))
						.setShortLabel(jsonObject.getString("title"))
						.setIcon(Icon.createWithBitmap(imageDownload(jsonObject.getString("icon"))))
						.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(jsonObject.getString("id"))))
						.build();
					if (sM.addDynamicShortcuts(Arrays.asList(shortcut)))
						Settings.edit().putString("shortcut_"+i, jsonArray.getString(i)).commit();
				}
			} catch (Exception e) {
				log("setShortcuts: "+e.getMessage());
			}
		}
	}
	private void getShortcuts() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
			Settings = getPreferences(MODE_PRIVATE);
			for (int i=0; i < 5; i++) {
				if (Settings.contains("shortcut_"+i))
					log("getShortcuts: "+Settings.getString("shortcut_"+i, ""));
			}
		}
	}
	private void toggleFullscreen() {
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags ^= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attrs);
	}
	private void callback(String e) {
		log("callback: "+e);
		mWebView.loadUrl("javascript:if (window.callback) { window.callback('"+e+"'); delete window.callback; }");
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
	public void log(String m) {
		File f = new File("/sdcard/Android/"+getPackageName()+".log");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(f, true));
			buf.append("["+String.valueOf(System.currentTimeMillis() / 1000L)+"] "+m);
			buf.newLine();
			buf.close();
		} catch (Exception e) {
			e.printStackTrace();
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
		public void onPermissionRequest(final PermissionRequest request) {
			log("onPermissionRequest");
			request.grant(request.getResources());
		}
		@Override
		@SuppressWarnings("deprecation")
		public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
			onShowCustomView(view, callback);
		}
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			if (mFullScreenView != null) {
				callback.onCustomViewHidden();
				return;
			}
			mFullScreenView = view;
			mWebView.setVisibility(View.GONE);
			mFullScreenContainer.setVisibility(View.VISIBLE);
			mFullScreenContainer.addView(view);
			mFullscreenViewCallback = callback;
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			toggleFullscreen();
		}
		@Override
		public void onHideCustomView() {
			super.onHideCustomView();
			if (mFullScreenView == null)
				return;
			mWebView.setVisibility(View.VISIBLE);
			mFullScreenContainer.setVisibility(View.GONE);
			mFullScreenContainer.removeView(mFullScreenView);
			mFullscreenViewCallback.onCustomViewHidden();
			mFullScreenView = null;
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			toggleFullscreen();
		}
		@Override
		public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg) {
			WebView.HitTestResult result = view.getHitTestResult();
			String data = result.getExtra();
			Context context = view.getContext();
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
			context.startActivity(browserIntent);
			return false;
		}
		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
			AlertDialog.Builder b = new AlertDialog.Builder(view.getContext()).setTitle(view.getTitle()).setMessage(message)
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
			AlertDialog.Builder b = new AlertDialog.Builder(view.getContext()).setTitle(view.getTitle()).setMessage(message)
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
			data.setText(defaultValue);
			AlertDialog.Builder b = new AlertDialog.Builder(view.getContext()).setTitle(view.getTitle()).setView(data).setMessage(message)
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
		public void onPageFinished(WebView view, String url) {
			if (getIntent() != null && getIntent().getData() != null) {
				log("dataIntent: "+getIntent().getDataString());
				mWebView.loadUrl("javascript:if (window.onintent) { window.onintent('"+getIntent().getDataString()+"'); }");
				getIntent().setData(null);
				setIntent(null);
			}else
				log("Loading is complete");
		}
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			mWebView.loadData("<body style=\"background: #f1f1f1;margin-top: calc(50vh - 22px);\"><div style=\"background:#F44336;border-radius: 0.125rem;box-shadow: 0 2px 2px 0 rgba(0,0,0,.14),0 3px 1px -2px rgba(0,0,0,.2),0 1px 5px 0 rgba(0,0,0,.12);\"><h2 style=\"color: white;font-weight: 300;text-align: center;font-size: 18px;margin: 0;padding: 8px 0;line-height: 28px;\">Error conection</h2></div></body>", "text/html", "utf-8");
		}
	}
}
