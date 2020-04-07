package com.asdtechlabs.bloodbank;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ProgressBar;

public class WebView extends AppCompatActivity {
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        android.webkit.WebView mywebview = (android.webkit.WebView) findViewById(R.id.webView);
        progressBar = findViewById(R.id.progress_bar);
        mywebview.setWebViewClient(new WebViewController(progressBar));
        Intent intent = getIntent();
        String link = intent.getStringExtra("link");
        mywebview.getSettings().setJavaScriptEnabled(true);
        mywebview.getSettings().setLoadWithOverviewMode(true);
        mywebview.getSettings().setUseWideViewPort(true);
        boolean isNetworkAvailable = isNetworkAvailable();
        if(isNetworkAvailable) {
            mywebview.loadUrl(link);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(WebView.this);
            builder.setTitle("No Internet Connection");
            builder.setMessage("Please make sure you have an active internet connection");
            builder.setPositiveButton("Retry",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            finish();
                            startActivity(getIntent());
                        }
                    });
            builder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
