package com.example.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
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
		mWebView.setWebViewClient(new WebViewClient());
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new Qad(), "$$$");
		mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.loadUrl("");
	}
	private class Qad {
		@android.webkit.JavascriptInterface
		public String getGreeting() {
			return "Hello JavaScript!";
		}
		@android.webkit.JavascriptInterface
		public  String getParse(String http) {
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
