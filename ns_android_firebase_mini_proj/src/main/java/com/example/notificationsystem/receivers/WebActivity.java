package com.example.notificationsystem.receivers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.notificationsystem.R;

public class WebActivity extends AppCompatActivity {
    WebView brow;
    String URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        Intent intent=getIntent();
        URL= intent.getStringExtra("url");
        getSupportActionBar().setTitle(URL);
        brow=findViewById(R.id.webpage);
        brow.setWebViewClient(new MyBrowser());
        brow.getSettings().setJavaScriptEnabled(true);
        brow.getSettings().setBuiltInZoomControls(true);
        brow.getSettings().setUseWideViewPort(true);
        brow.loadUrl(URL);
    }

    private class MyBrowser extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }
}
