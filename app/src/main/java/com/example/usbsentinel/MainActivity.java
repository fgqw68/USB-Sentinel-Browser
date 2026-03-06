package com.example.usbsentinel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private BroadcastReceiver restrictionsChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWebView();
        setupUsbBridge();
        setupViewModel();
        setupRestrictionsReceiver();

        // Sync restrictions before loading page (handles case where profile pushed before launch)
        usbViewModel.syncRestrictions();

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
     * Sets up the BroadcastReceiver for restriction changes.
     * This handles the case where admin pushes profile while app is already running.
     */
    private void setupRestrictionsReceiver() {
        restrictionsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED.equals(intent.getAction())) {
                    // Re-sync restrictions when admin pushes profile while app is running
                    usbViewModel.syncRestrictions();
                    // Reload page with new configuration
                    loadPage();
                }
            }
        };
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
     * Uses ViewModel's getInitialUrl() which checks:
     * 1. SharedPreferences (persisted from restrictions)
     * 2. RestrictionsManager (live from EMM)
     * 3. Default URL (file:///android_asset/index.html)
     */
    private void loadPage() {
        String startPage = usbViewModel.getInitialUrl();
        binding.webView.loadUrl(startPage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register BroadcastReceiver for restriction changes
        if (restrictionsChangedReceiver != null) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);
            registerReceiver(restrictionsChangedReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister BroadcastReceiver to avoid leaks
        if (restrictionsChangedReceiver != null) {
            try {
                unregisterReceiver(restrictionsChangedReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver was not registered, ignore
            }
        }
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