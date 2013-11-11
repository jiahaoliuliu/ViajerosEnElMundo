package com.jiahaoliuliu.android.viajerosenelmundo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;

import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.ProgressBarShowListener;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.onErrorReceivedListener;

public class WebViewFragment extends Fragment {

	private static final String LOG_TAG = WebViewFragment.class.getSimpleName();
	
	private WebView webView;
	private onErrorReceivedListener onErrorReceivedListener;
	private ProgressBarShowListener progressBarShownListener;
	private String url;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onErrorReceivedListener = (onErrorReceivedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + 
					" must implement OnNewItemAddedListener");
		}
		
		try {
			progressBarShownListener = (ProgressBarShowListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + 
					" must implement ProgressBarShownListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.web_view_fragment, container, false);
		webView = (WebView)view.findViewById(R.id.webView);
		webView.setWebViewClient(new SampleWebClient());
		webView.setWebChromeClient(new WebChromeClient());

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setBuiltInZoomControls(true);
		//webSettings.setPluginState(PluginState.ON);

		if (url != null) {
			webView.loadUrl(url);
		}
		return view;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private class SampleWebClient extends WebViewClient {
		
		@Override
		public boolean shouldOverrideUrlLoading (WebView view, String newUrl) {
			if (url != null) {
				view.loadUrl(url);
			}
			return true;
		}
		
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	progressBarShownListener.showProgressBar();
        	view.setClickable(false);
        	view.setLongClickable(false);
        	view.setEnabled(false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
        	progressBarShownListener.hideProgressBar();
        	view.setClickable(true);
        	view.setLongClickable(true);
        	view.setEnabled(true);
        }

		@Override
		public void onReceivedError (WebView view,
				int errorCode,
				String description,
				String failingUrl) {
			Log.e(LOG_TAG, "Error received: \n" +
				"ErrorCode: " + errorCode + "\n" +
				"Descripction: " + description + "\n" +
				"Failing Url: " + failingUrl);
			
			String errorMessage = "Error desconocido. Por favor, intentadlo de nuevo más tarde";
			switch (errorCode) {
			case WebViewClient.ERROR_HOST_LOOKUP:
			case WebViewClient.ERROR_CONNECT:
				errorMessage = "Error de conexión con el servidor. Comprueba que tienes conexión a internet.";
				break;
			case WebViewClient.ERROR_TIMEOUT:
				errorMessage = "Lo sentimos. El tiempo de conexión se ha expirado. Intentadlo más tarde.";
				break;
			}

			progressBarShownListener.hideProgressBar();
			onErrorReceivedListener.onErrorReceived(errorCode, errorMessage);
		}
	}

	public boolean goesBack() {
		if (webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return false;
	}

	@Override
	public void onPause() {
	    super.onPause();

	    try {
	        Class.forName("android.webkit.WebView")
	                .getMethod("onPause", (Class[]) null)
	                            .invoke(webView, (Object[]) null);

	    } catch (Exception e) {
	    	Log.e(LOG_TAG, "Error pausing the web view ", e);
	    }
	}
    @Override
    public void onDetach() {
        super.onDetach();
        progressBarShownListener.hideProgressBar();
        webView.removeAllViews();
        webView.destroy();
    }
}
