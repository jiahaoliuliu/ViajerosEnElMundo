package com.jiahaoliuliu.android.viajerosenelmundo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new SampleWebClient());
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
			progressBarShownListener.hideProgressBar();
			onErrorReceivedListener.onErrorReceived(errorCode, description);
		}
	}

	public boolean goesBack() {
		if (webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return false;
	}

	/*
    @Override
    public void onDetach() {
        super.onDetach();
        webView.removeAllViews();
        webView.destroy();
    }*/
}
