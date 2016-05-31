package com.example.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;

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
	}
	@Override
	public void onBackPressed() {
		if(mWebView.canGoBack())
			mWebView.goBack();
		else
			super.onBackPressed();
	}
}
