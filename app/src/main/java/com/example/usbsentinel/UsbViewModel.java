package com.example.usbsentinel;

import android.app.Application;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UsbViewModel manages USB device state following strict MVVM pattern.
 * It exposes USB device information via LiveData without any View references.
 */
public class UsbViewModel extends AndroidViewModel {

    private final UsbManager usbManager;
    private final MutableLiveData<String> usbDeviceJson;
    private Context monitoringContext;

    public UsbViewModel(@NonNull Application application) {
        super(application);
        usbManager = (UsbManager) application.getSystemService(Context.USB_SERVICE);
        usbDeviceJson = new MutableLiveData<>();
        // Set the static reference for the BroadcastReceiver
        UsbReceiver.setUsbViewModel(this);
        // Initialize with empty device list
        updateDeviceList();
    }

    /**
     * Exposes the USB device JSON as LiveData for observation.
     *
     * @return LiveData containing JSON string of connected USB devices
     */
    public LiveData<String> getUsbDeviceJson() {
        return usbDeviceJson;
    }

    /**
     * Starts monitoring USB device changes.
     * Since the BroadcastReceiver is declared in the manifest, it will receive
     * system broadcasts automatically. This method just ensures initial state.
     */
    public void startMonitoring(Context context) {
        this.monitoringContext = context.getApplicationContext();
        updateDeviceList();
    }

    /**
     * Stops monitoring USB device changes.
     * Unregisters the BroadcastReceiver to prevent memory leaks.
     */
    public void stopMonitoring() {
        this.monitoringContext = null;
        // The BroadcastReceiver is declared in the manifest, so we just clear
        // the static reference to prevent leaks
        UsbReceiver.setUsbViewModel(null);
    }

    /**
     * Called when USB device attachment/detachment events are received.
     * Updates the device list LiveData.
     */
    public void onUsbDeviceChanged() {
        updateDeviceList();
    }

    /**
     * Updates the USB device list and posts JSON to LiveData.
     */
    private void updateDeviceList() {
        if (usbManager == null) {
            usbDeviceJson.postValue("[]");
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

        // Convert to JSON string
        String json = devicesToJson(devices);
        usbDeviceJson.postValue(json);
    }

    /**
     * Converts a list of device maps to a JSON string.
     *
     * @param devices List of device information maps
     * @return JSON string representation
     */
    private String devicesToJson(List<Map<String, Object>> devices) {
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
        return devicesArray.toString();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Stop monitoring to prevent memory leaks
        stopMonitoring();
    }
}