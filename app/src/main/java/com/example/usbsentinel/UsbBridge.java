package com.example.usbsentinel;

import android.webkit.JavascriptInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * UsbBridge provides a JavaScript interface for the WebView to communicate
 * with the native Android USB functionality.
 */
public class UsbBridge {

    private UsbViewModel usbViewModel;

    public UsbBridge(UsbViewModel usbViewModel) {
        this.usbViewModel = usbViewModel;
    }

    /**
     * Register for USB connection notifications from JavaScript.
     * The callback will be invoked whenever the list of connected USB devices changes.
     *
     * @param callback The JavaScript callback function to invoke with device information
     */
    @JavascriptInterface
    public void registerForUsbConnectionNotification(String callback) {
        usbViewModel.registerJavascriptCallback(callback);
    }

    /**
     * Internal method to invoke the JavaScript callback with device information.
     * This is called by the ViewModel when the device list changes.
     *
     * @param devices List of connected USB device information
     */
    public void notifyDevicesChanged(List<Map<String, Object>> devices) {
        JSONArray devicesArray = new JSONArray();
        for (Map<String, Object> device : devices) {
            try {
                JSONObject deviceObj = new JSONObject();
                deviceObj.put("deviceName", device.get("deviceName"));
                deviceObj.put("vendorId", device.get("vendorId"));
                deviceObj.put("productId", device.get("productId"));
                deviceObj.put("deviceId", device.get("deviceId"));
                devicesArray.put(deviceObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        usbViewModel.invokeJavascriptCallback(devicesArray.toString());
    }
}