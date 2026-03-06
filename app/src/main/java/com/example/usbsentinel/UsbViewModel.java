package com.example.usbsentinel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UsbViewModel manages USB device state and communicates with the JavaScript bridge.
 */
public class UsbViewModel extends AndroidViewModel {

    private UsbManager usbManager;
    private MutableLiveData<List<Map<String, Object>>> usbDevices;
    private String javascriptCallback;
    private WebView webView;
    private UsbBridge usbBridge;

    public UsbViewModel(@NonNull Application application) {
        super(application);
        usbManager = (UsbManager) application.getSystemService(Context.USB_SERVICE);
        usbDevices = new MutableLiveData<>();
        // Set the static reference for the BroadcastReceiver
        UsbReceiver.setUsbViewModel(this);
    }

    public LiveData<List<Map<String, Object>>> getUsbDevices() {
        return usbDevices;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
        this.usbBridge = new UsbBridge(this);
        webView.addJavascriptInterface(this.usbBridge, "UsbBridge");
    }

    public UsbBridge getUsbBridge() {
        return usbBridge;
    }

    public void registerJavascriptCallback(String callback) {
        this.javascriptCallback = callback;
        // Immediately notify with current devices
        updateDeviceList();
    }

    public void invokeJavascriptCallback(String deviceJson) {
        if (webView != null && javascriptCallback != null && !javascriptCallback.isEmpty()) {
            webView.post(() -> {
                String js = "(" + javascriptCallback + ")('" + deviceJson.replace("'", "\\'") + "')";
                webView.evaluateJavascript(js, null);
            });
        }
    }

    public void startMonitoring(Context context) {
        updateDeviceList();
    }

    public void stopMonitoring(Context context) {
        // No need to unregister since the receiver is in the manifest
    }

    public void onUsbDeviceChanged() {
        updateDeviceList();
    }

    private void updateDeviceList() {
        if (usbManager == null) {
            usbDevices.setValue(new ArrayList<>());
            return;
        }

        List<Map<String, Object>> devices = new ArrayList<>();
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        for (UsbDevice device : deviceList.values()) {
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceName", device.getDeviceName());
            deviceInfo.put("vendorId", device.getVendorId());
            deviceInfo.put("productId", device.getProductId());
            deviceInfo.put("deviceId", device.getDeviceName());
            devices.add(deviceInfo);
        }

        usbDevices.setValue(devices);

        // Notify the JavaScript bridge if a callback is registered
        if (usbBridge != null) {
            usbBridge.notifyDevicesChanged(devices);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clear the static reference
        UsbReceiver.setUsbViewModel(null);
        if (usbBridge != null && webView != null) {
            webView.removeJavascriptInterface("UsbBridge");
        }
    }
}