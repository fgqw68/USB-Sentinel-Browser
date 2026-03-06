package com.example.usbsentinel;

import android.app.Application;
import android.hardware.usb.UsbDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * UsbViewModel manages USB device state following strict MVVM pattern.
 * Uses Transformations.map to reactively convert device list to JSON.
 * When app opens, ViewModel immediately sees latest snapshot from Repository.
 */
public class UsbViewModel extends AndroidViewModel {

    private final LiveData<String> usbDeviceJson;

    public UsbViewModel(@NonNull Application application) {
        super(application);
        UsbRepository repository = UsbRepository.getInstance(application);

        // Create reactive pipe: Repository LiveData -> JSON LiveData
        usbDeviceJson = Transformations.map(
                repository.getUsbDevices(),
                this::devicesToJson
        );
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
     * Converts a list of UsbDevice objects to a JSON string.
     *
     * @param devices List of USB devices
     * @return JSON string representation
     */
    private String devicesToJson(List<UsbDevice> devices) {
        JSONArray devicesArray = new JSONArray();
        if (devices != null) {
            for (UsbDevice device : devices) {
                try {
                    JSONObject deviceObj = new JSONObject();
                    deviceObj.put("deviceName", device.getDeviceName());
                    deviceObj.put("vendorId", device.getVendorId());
                    deviceObj.put("productId", device.getProductId());
                    deviceObj.put("deviceId", device.getDeviceName());
                    devicesArray.put(deviceObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return devicesArray.toString();
    }
}