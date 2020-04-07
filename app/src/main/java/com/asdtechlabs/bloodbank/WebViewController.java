package com.asdtechlabs.bloodbank;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by Abhijeet on 7/20/2019.
 */

public class WebViewController extends WebViewClient {

    private ProgressBar progressBar;

    WebViewController(ProgressBar progressBar) {
        this.progressBar=progressBar;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        // TODO Auto-generated method stub
        super.onPageFinished(view, url);
        progressBar.setVisibility(View.GONE);
    }
}