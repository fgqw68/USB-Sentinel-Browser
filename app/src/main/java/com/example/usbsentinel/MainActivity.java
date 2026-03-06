package com.example.usbsentinel;

import android.content.Context;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.usbsentinel.databinding.ActivityMainBinding;

/**
 * MainActivity sets up the WebView and handles USB Sentinel functionality.
 * It follows strict MVVM pattern by observing LiveData from ViewModel.
 * The ViewModel has no reference to Views, preventing memory leaks.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UsbViewModel usbViewModel;
    private UsbBridge usbBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWebView();
        setupUsbBridge();
        setupViewModel();
        loadPage();
    }

    /**
     * Configures the WebView settings and client.
     */
    private void setupWebView() {
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
    }

    /**
     * Sets up the JavaScript bridge between WebView and native code.
     */
    private void setupUsbBridge() {
        usbBridge = new UsbBridge();
        binding.webView.addJavascriptInterface(usbBridge, "UsbBridge");
    }

    /**
     * Sets up the ViewModel and observes USB device JSON LiveData.
     */
    private void setupViewModel() {
        usbViewModel = new ViewModelProvider(this).get(UsbViewModel.class);

        // Observe USB device JSON changes
        usbViewModel.getUsbDeviceJson().observe(this, this::onUsbDevicesChanged);


    }

    /**
     * Called when USB device list changes.
     * Executes the JavaScript callback with the updated device JSON.
     *
     * @param deviceJson JSON string of connected USB devices
     */
    private void onUsbDevicesChanged(String deviceJson) {
        if (usbBridge == null) {
            return;
        }

        String callback = usbBridge.getJavascriptCallback();
        if (callback != null && !callback.isEmpty()) {
            // Execute JavaScript callback with device JSON
            String js = "(" + callback + ")('" + deviceJson.replace("'", "\\'") + "')";
            binding.webView.post(() -> binding.webView.evaluateJavascript(js, null));
        }
    }

    /**
     * Loads the appropriate page based on managed configuration.
     */
    private void loadPage() {
        String startPage = getManagedStartPage();

        if (startPage != null && !startPage.isEmpty()) {
            binding.webView.loadUrl(startPage);
        } else {
            // Load local fallback page
            binding.webView.loadUrl("file:///android_asset/index.html");
        }
    }

    /**
     * Gets the managed start page URL from device owner restrictions.
     *
     * @return The start page URL, or null if not configured
     */
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
        // Clear the bridge callback to prevent memory leaks
        if (usbBridge != null) {
            usbBridge.clearCallback();
            binding.webView.removeJavascriptInterface("UsbBridge");
        }
        // Clear binding reference
        binding = null;
    }

    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}