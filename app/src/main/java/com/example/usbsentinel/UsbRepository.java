package com.example.usbsentinel;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * UsbRepository is a singleton that manages USB device state.
 * Uses a Manifest-declared BroadcastReceiver to capture events even when app is in background.
 * LiveData holds the latest device snapshot for UI consumption.
 */
public class UsbRepository {

    private static volatile UsbRepository INSTANCE;

    private final UsbManager usbManager;
    private final MutableLiveData<List<UsbDevice>> usbDevices;

    /**
     * Private constructor for singleton pattern.
     *
     * @param context Application context
     */
    private UsbRepository(@NonNull Context context) {
        this.usbManager = (UsbManager) context.getApplicationContext().getSystemService(Context.USB_SERVICE);
        this.usbDevices = new MutableLiveData<>();
        // Initialize with current device list
        updateDeviceList();
    }

    /**
     * Gets the singleton instance of UsbRepository.
     *
     * @param context Application context
     * @return The singleton UsbRepository instance
     */
    public static UsbRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (UsbRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UsbRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Exposes the USB device list as LiveData.
     *
     * @return LiveData containing list of connected USB devices
     */
    public LiveData<List<UsbDevice>> getUsbDevices() {
        return usbDevices;
    }

    /**
     * Updates the USB device list by polling the UsbManager.
     * Called by the Manifest-declared BroadcastReceiver on USB events.
     */
    public void updateDeviceList() {
        if (usbManager == null) {
            usbDevices.postValue(Collections.emptyList());
            return;
        }

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        List<UsbDevice> devices = new ArrayList<>(deviceList.values());
        usbDevices.postValue(devices);
    }
}