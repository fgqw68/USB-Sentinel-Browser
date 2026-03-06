package com.example.usbsentinel;

import android.content.Context;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

/**
 * MainActivity sets up the WebView and handles USB Sentinel functionality.
 * It loads either a configured start page from managed configurations or the local fallback page.
 */
public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private UsbViewModel usbViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        setupWebView();

        usbViewModel = new ViewModelProvider(this).get(UsbViewModel.class);
        usbViewModel.setWebView(webView);

        // Load the appropriate page
        loadPage();

        // Start monitoring USB devices
        usbViewModel.startMonitoring(this);
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
    }

    private void loadPage() {
        String startPage = getManagedStartPage();

        if (startPage != null && !startPage.isEmpty()) {
            webView.loadUrl(startPage);
        } else {
            // Load local fallback page
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    private String getManagedStartPage() {
        RestrictionsManager restrictionsManager = (RestrictionsManager) getSystemService(Context.RESTRICTIONS_SERVICE);
        if (restrictionsManager == null) {
            return null;
        }

        Bundle restrictions = restrictionsManager.getApplicationRestrictions();
        if (restrictions == null) {
            return null;
        }

        return restrictions.getString("start_page");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usbViewModel != null) {
            usbViewModel.stopMonitoring(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}