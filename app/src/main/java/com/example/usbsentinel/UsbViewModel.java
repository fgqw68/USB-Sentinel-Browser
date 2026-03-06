package com.example.usbsentinel;

import android.app.Application;
import android.content.Context;
import android.content.RestrictionsManager;
import android.content.SharedPreferences;
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

    private static final String PREFS_NAME = "usb_sentinel_prefs";
    private static final String KEY_START_PAGE = "start_page";
    private static final String DEFAULT_START_PAGE = "file:///android_asset/index.html";

    private final LiveData<String> usbDeviceJson;
    private final SharedPreferences sharedPreferences;

    public UsbViewModel(@NonNull Application application) {
        super(application);
        UsbRepository repository = UsbRepository.getInstance(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

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

    /**
     * Syncs the start_page restriction from RestrictionsManager to SharedPreferences.
     * Call this in MainActivity.onCreate and when restrictions change.
     * Handles both cases:
     * 1. Admin pushes profile when app is already running
     * 2. Admin pushes profile, then app is launched
     */
    public void syncRestrictions() {
        Application application = getApplication();
        RestrictionsManager restrictionsManager = (RestrictionsManager)
                application.getSystemService(Context.RESTRICTIONS_SERVICE);

        if (restrictionsManager == null) {
            return;
        }

        android.os.Bundle restrictions = restrictionsManager.getApplicationRestrictions();
        String startPage = null;

        if (restrictions != null) {
            startPage = restrictions.getString(KEY_START_PAGE);
        }

        // Persist to SharedPreferences for offline use
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (startPage != null && !startPage.isEmpty()) {
            editor.putString(KEY_START_PAGE, startPage);
        } else {
            // Remove if restriction is cleared, will fall back to default
            editor.remove(KEY_START_PAGE);
        }
        editor.apply();
    }

    /**
     * Gets the initial URL to load in the WebView.
     * Priority order:
     * 1. SharedPreferences (persisted from restrictions)
     * 2. RestrictionsManager (live from EMM)
     * 3. Default URL (file:///android_asset/index.html)
     *
     * @return The URL to load
     */
    public String getInitialUrl() {
        // 1. Check SharedPreferences first (persisted value)
        String persistedUrl = sharedPreferences.getString(KEY_START_PAGE, null);
        if (persistedUrl != null && !persistedUrl.isEmpty()) {
            return persistedUrl;
        }

        // 2. Check live RestrictionsManager
        Application application = getApplication();
        RestrictionsManager restrictionsManager = (RestrictionsManager)
                application.getSystemService(Context.RESTRICTIONS_SERVICE);

        if (restrictionsManager != null) {
            android.os.Bundle restrictions = restrictionsManager.getApplicationRestrictions();
            if (restrictions != null) {
                String restrictedUrl = restrictions.getString(KEY_START_PAGE);
                if (restrictedUrl != null && !restrictedUrl.isEmpty()) {
                    return restrictedUrl;
                }
            }
        }

        // 3. Default to local asset
        return DEFAULT_START_PAGE;
    }
}