package com.example.usbsentinel;

import android.webkit.JavascriptInterface;

/**
 * UsbBridge provides a JavaScript interface for the WebView to communicate
 * with the native Android USB functionality.
 * It stores the JavaScript callback function reference for use by MainActivity.
 */
public class UsbBridge {

    private String javascriptCallback;

    public UsbBridge() {
        // Default constructor for JavaScript interface
    }

    /**
     * Register for USB connection notifications from JavaScript.
     * Stores the callback function reference to be used by MainActivity's LiveData observer.
     *
     * @param callback The JavaScript callback function to invoke with device information
     */
    @JavascriptInterface
    public void registerForUsbConnectionNotification(String callback) {
        this.javascriptCallback = callback;
    }

    /**
     * Gets the registered JavaScript callback function.
     * Called by MainActivity to execute the callback when LiveData changes.
     *
     * @return The JavaScript callback function string, or null if not registered
     */
    public String getJavascriptCallback() {
        return javascriptCallback;
    }

    /**
     * Clears the registered callback to prevent memory leaks.
     */
    public void clearCallback() {
        this.javascriptCallback = null;
    }
}